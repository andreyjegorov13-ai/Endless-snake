package com.example.endlesssnake.android

import android.app.Activity
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.example.endlesssnake.R
import com.example.endlesssnake.audio.AudioManager
import com.example.endlesssnake.core.FoodType
import com.example.endlesssnake.core.GameEngine
import com.example.endlesssnake.core.GamePhase
import com.example.endlesssnake.core.math.Direction
import com.example.endlesssnake.core.math.WorldPoint
import com.example.endlesssnake.data.SaveManager
import com.example.endlesssnake.data.SettingsRepository
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

class GameSurfaceView(context: Context, private val loadSave: Boolean = false) :
    SurfaceView(context), SurfaceHolder.Callback, Runnable {

    @Volatile private var thread: Thread? = null
    @Volatile private var isRunning = false
    @Volatile private var paused = false

    private val settings = SettingsRepository(context)
    private val audio = AudioManager(context)
    private val engine = GameEngine(settings.difficulty)

    private val inputQueue = ConcurrentLinkedQueue<Direction>()

    private val paintBg = Paint().apply { color = context.getColor(R.color.bg) }
    private val paintObstacle = Paint().apply { color = context.getColor(R.color.obstacle); isAntiAlias = true }
    private val paintText = Paint().apply { color = Color.WHITE; textSize = 50f; isAntiAlias = true; typeface = Typeface.DEFAULT_BOLD }
    private val paintSmallText = Paint().apply { color = Color.WHITE; textSize = 30f; isAntiAlias = true }
    private val paintBtn = Paint().apply { color = Color.argb(80, 255, 255, 255); isAntiAlias = true }
    private val paintBtnPressed = Paint().apply { color = Color.argb(160, 255, 255, 255); isAntiAlias = true }
    private val paintArrow = Paint().apply { color = Color.WHITE; style = Paint.Style.FILL; isAntiAlias = true }
    private val paintCompass = Paint().apply { color = Color.parseColor("#FFFF4757"); style = Paint.Style.FILL; isAntiAlias = true }
    private val paintCompassGlow = Paint().apply { color = Color.parseColor("#FFFF4757"); alpha = 80; isAntiAlias = true }

    private val paintFood = mutableMapOf<FoodType, Paint>().apply {
        FoodType.values().forEach { t ->
            put(t, Paint().apply { color = Color.parseColor(t.colorHex); isAntiAlias = true })
        }
    }
    private val paintFoodGlow = mutableMapOf<FoodType, Paint>().apply {
        FoodType.values().forEach { t ->
            put(t, Paint().apply { color = Color.parseColor(t.colorHex); alpha = t.glowAlpha; isAntiAlias = true })
        }
    }

    private val paintSnakeHead = Paint().apply { isAntiAlias = true }
    private val paintSnakeBody = Paint().apply { isAntiAlias = true }
    private val paintSnakeGlow = Paint().apply { color = Color.parseColor("#4ADE80"); alpha = 60; isAntiAlias = true }
    private val paintEyeWhite = Paint().apply { color = Color.WHITE; isAntiAlias = true }
    private val paintEyePupil = Paint().apply { color = Color.BLACK; isAntiAlias = true }

    private var cellSize = 0f
    private var offsetX = 0f
    private var offsetY = 0f
    private var cols = 0
    private var rows = 0
    private var touchStartX = 0f
    private var touchStartY = 0f
    private var useButtons = settings.useButtons

    private var prevSnakePositions: List<WorldPoint> = emptyList()
    private var interpProgress: Float = 0f
    private var cameraX: Float = 0f
    private var cameraY: Float = 0f
    private val cameraSmoothing = 0.12f

    private var btnSize = 0f
    private var btnUpX = 0f; private var btnUpY = 0f
    private var btnDownX = 0f; private var btnDownY = 0f
    private var btnLeftX = 0f; private var btnLeftY = 0f
    private var btnRightX = 0f; private var btnRightY = 0f
    @Volatile private var pressedBtn: Direction? = null

    private var pulseAnim = 0f
    private var highScoreSaved = false
    private var savedToastUntil = 0L

    private var pauseRect = RectF()
    private var menuPanelRect = RectF()
    private var btnSaveRect = RectF()
    private var btnContinueRect = RectF()
    private var btnQuitRect = RectF()

    init {
        holder.addCallback(this)
        isFocusable = true
        audio.soundVolume = if (settings.soundEnabled) settings.soundVolume / 100f else 0f
        audio.musicVolume = if (settings.musicEnabled) settings.musicVolume / 100f else 0f
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        val w = width.toFloat()
        val h = height.toFloat()
        cellSize = w / 20f
        cols = (w / cellSize).toInt()
        rows = (h / cellSize).toInt()
        offsetX = (w - cols * cellSize) / 2f
        offsetY = (h - rows * cellSize) / 2f

        btnSize = w / 6f
        val centerX = w / 2f
        val centerY = h - btnSize * 1.8f
        btnUpX = centerX; btnUpY = centerY - btnSize
        btnDownX = centerX; btnDownY = centerY + btnSize
        btnLeftX = centerX - btnSize; btnLeftY = centerY
        btnRightX = centerX + btnSize; btnRightY = centerY

        // Кнопка паузы в правом верхнем углу
        pauseRect = RectF(w - 130f, 30f, w - 30f, 130f)

        // Меню паузы
        val mw = min(w * 0.75f, 700f)
        val mx = (w - mw) / 2f
        val mh = 120f
        val gap = 36f
        val top = h / 2f - (mh * 3 + gap * 2) / 2f + 60f
        btnSaveRect = RectF(mx, top, mx + mw, top + mh)
        btnContinueRect = RectF(mx, top + mh + gap, mx + mw, top + 2 * mh + gap)
        btnQuitRect = RectF(mx, top + 2 * (mh + gap), mx + mw, top + 3 * mh + 2 * gap)
        menuPanelRect = RectF(mx - 40f, top - 170f, mx + mw + 40f, btnQuitRect.bottom + 120f)

        // Загрузка сохранения
        if (loadSave) {
            SaveManager.load(context)?.let { snap -> engine.restore(snap) }
        }

        prevSnakePositions = engine.snake.body.toList()
        cameraX = engine.snake.head.x.toFloat()
        cameraY = engine.snake.head.y.toFloat()
        resume()
    }

    fun resume() {
        if (isRunning) return
        isRunning = true
        val t = Thread(this, "GameLoop")
        thread = t
        t.start()
        audio.startMusic()
    }

    fun pause() {
        if (!isRunning) return
        isRunning = false
        audio.pauseMusic()
        try { thread?.join(1000) } catch (e: InterruptedException) {}
        thread = null
    }

    fun destroy() { audio.release() }

    override fun run() {
        var lastTime = System.nanoTime()
        var accumulator = 0L
        while (isRunning) {
            val now = System.nanoTime()
            val delta = (now - lastTime) / 1_000_000L
            lastTime = now

            if (!paused && engine.phase == GamePhase.RUNNING) {
                accumulator += delta
                interpProgress = (accumulator.toFloat() / engine.tickMs.toFloat()).coerceIn(0f, 1f)

                while (accumulator >= engine.tickMs) {
                    prevSnakePositions = engine.snake.body.toList()
                    val prevScore = engine.score
                    engine.update(inputQueue.poll())
                    if (engine.score > prevScore) audio.playEatSound()

                    if (engine.justDied) {
                        engine.justDied = false
                        if (engine.lives <= 0) {
                            if (!highScoreSaved) {
                                if (engine.score > settings.bestScore) settings.bestScore = engine.score
                                highScoreSaved = true
                            }
                            SaveManager.deleteSave(context)
                        }
                        prevSnakePositions = engine.snake.body.toList()
                        cameraX = engine.snake.head.x.toFloat()
                        cameraY = engine.snake.head.y.toFloat()
                    }
                    accumulator -= engine.tickMs
                    interpProgress = 0f
                }

                val targetCamX = engine.snake.head.x.toFloat()
                val targetCamY = engine.snake.head.y.toFloat()
                cameraX += (targetCamX - cameraX) * cameraSmoothing
                cameraY += (targetCamY - cameraY) * cameraSmoothing
            } else {
                accumulator = 0L
                interpProgress = 0f
            }

            pulseAnim += delta * 0.003f
            if (pulseAnim > 2 * Math.PI) pulseAnim = 0f

            draw()
            try { Thread.sleep(16) } catch (e: InterruptedException) {}
        }
    }

    private fun lerp(a: Float, b: Float, t: Float): Float = a + (b - a) * t

    private fun draw() {
        if (!holder.surface.isValid) return
        val canvas: Canvas = holder.lockCanvas() ?: return
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paintBg)

        val halfCols = cols / 2
        val halfRows = rows / 2
        val startWorldX = cameraX - halfCols.toFloat()
        val startWorldY = cameraY - halfRows.toFloat()

        val startGridX = startWorldX.toInt() - 1
        val startGridY = startWorldY.toInt() - 1
        for (r in 0 until rows + 2) {
            for (c in 0 until cols + 2) {
                val worldX = startGridX + c
                val worldY = startGridY + r
                val screenX = offsetX + (worldX - startWorldX) * cellSize
                val screenY = offsetY + (worldY - startWorldY) * cellSize
                if (engine.world.hasObstacle(WorldPoint(worldX, worldY), engine.score, engine.difficulty)) {
                    canvas.drawRect(screenX + 1f, screenY + 1f, screenX + cellSize - 1f, screenY + cellSize - 1f, paintObstacle)
                }
            }
        }

        val body = engine.snake.body.toList()
        val prev = prevSnakePositions
        val t = interpProgress

        drawFood(canvas, engine.food, FoodType.RED, startWorldX, startWorldY)
        engine.bonuses.toList().forEach { bonus ->
            drawFood(canvas, bonus.pos, bonus.type, startWorldX, startWorldY)
        }

        body.firstOrNull()?.let { head ->
            val prevP = if (prev.isNotEmpty()) prev[0] else head
            val ix = lerp(prevP.x.toFloat(), head.x.toFloat(), t)
            val iy = lerp(prevP.y.toFloat(), head.y.toFloat(), t)
            val hx = offsetX + (ix - startWorldX) * cellSize + cellSize / 2
            val hy = offsetY + (iy - startWorldY) * cellSize + cellSize / 2
            canvas.drawCircle(hx, hy, cellSize * 1.1f, paintSnakeGlow)
        }

        for (index in body.indices.reversed()) {
            val p = body[index]
            val prevP = if (index < prev.size) prev[index] else p
            val interpX = lerp(prevP.x.toFloat(), p.x.toFloat(), t)
            val interpY = lerp(prevP.y.toFloat(), p.y.toFloat(), t)
            val sx = offsetX + (interpX - startWorldX) * cellSize
            val sy = offsetY + (interpY - startWorldY) * cellSize
            if (index == 0) drawSnakeHead(canvas, sx, sy)
            else drawSnakeSegment(canvas, sx, sy, index, body.size)
        }

        // HUD: счёт слева сверху, жизни под счётом
        canvas.drawText(context.getString(R.string.score, engine.score), 40f, 80f, paintText)
        canvas.drawText(context.getString(R.string.lives, engine.lives), 40f, 140f, paintSmallText)

        val foodScreenX = offsetX + (engine.food.x - startWorldX) * cellSize
        val foodScreenY = offsetY + (engine.food.y - startWorldY) * cellSize
        val foodCx = foodScreenX + cellSize / 2
        val foodCy = foodScreenY + cellSize / 2
        val foodOnScreen = foodScreenX >= 0 && foodScreenX <= width - cellSize &&
                           foodScreenY >= 0 && foodScreenY <= height - cellSize
        if (!foodOnScreen && engine.phase == GamePhase.RUNNING && !paused) {
            drawFoodCompass(canvas, foodCx, foodCy)
        }

        if (useButtons && engine.phase == GamePhase.RUNNING && !paused) {
            drawButton(canvas, btnUpX, btnUpY, Direction.UP)
            drawButton(canvas, btnDownX, btnDownY, Direction.DOWN)
            drawButton(canvas, btnLeftX, btnLeftY, Direction.LEFT)
            drawButton(canvas, btnRightX, btnRightY, Direction.RIGHT)
        }

        // Кнопка паузы в уголке
        if (!paused && engine.phase == GamePhase.RUNNING) {
            val pb = Paint().apply { color = Color.argb(90, 255, 255, 255); isAntiAlias = true }
            canvas.drawRoundRect(pauseRect, 24f, 24f, pb)
            val bar = Paint().apply { color = Color.WHITE; isAntiAlias = true }
            val cx = pauseRect.centerX()
            canvas.drawRect(cx - 22f, pauseRect.top + 25f, cx - 8f, pauseRect.bottom - 25f, bar)
            canvas.drawRect(cx + 8f, pauseRect.top + 25f, cx + 22f, pauseRect.bottom - 25f, bar)
        }

        if (paused) {
            drawPauseMenu(canvas)
        }

        if (engine.phase == GamePhase.GAME_OVER) {
            val darken = Paint().apply { color = Color.argb(180, 0, 0, 0) }
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), darken)
            paintText.textSize = 100f
            val g = context.getString(R.string.game_over)
            canvas.drawText(g, width / 2f - paintText.measureText(g) / 2f, height / 2f - 50f, paintText)
            paintText.textSize = 50f
            val s = context.getString(R.string.score, engine.score)
            canvas.drawText(s, width / 2f - paintText.measureText(s) / 2f, height / 2f + 30f, paintText)
            paintSmallText.textSize = 30f
            val r = context.getString(R.string.tap_restart)
            canvas.drawText(r, width / 2f - paintSmallText.measureText(r) / 2f, height / 2f + 100f, paintSmallText)
            if (engine.score == settings.bestScore && engine.score > 0) {
                paintSmallText.color = Color.parseColor("#FFD700")
                paintSmallText.textSize = 32f
                val nr = if (settings.language == "en") "NEW RECORD!" else "НОВЫЙ РЕКОРД!"
                canvas.drawText(nr, width / 2f - paintSmallText.measureText(nr) / 2f, height / 2f + 150f, paintSmallText)
                paintSmallText.color = Color.WHITE
            }
        }
        holder.unlockCanvasAndPost(canvas)
    }

    private fun drawPauseMenu(canvas: Canvas) {
        val darken = Paint().apply { color = Color.argb(200, 0, 0, 0) }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), darken)

        val panel = Paint().apply { color = Color.argb(235, 26, 31, 58); isAntiAlias = true }
        canvas.drawRoundRect(menuPanelRect, 40f, 40f, panel)
        val stroke = Paint().apply { color = Color.argb(120, 167, 139, 255); style = Paint.Style.STROKE; strokeWidth = 3f; isAntiAlias = true }
        canvas.drawRoundRect(menuPanelRect, 40f, 40f, stroke)

        val tp = Paint().apply { color = Color.WHITE; isAntiAlias = true; typeface = Typeface.DEFAULT_BOLD; textAlign = Paint.Align.CENTER }
        tp.textSize = 64f
        canvas.drawText(context.getString(R.string.pause_title), width / 2f, menuPanelRect.top + 110f, tp)

        tp.textSize = 44f
        drawMenuButton(canvas, btnSaveRect, context.getString(R.string.pause_save), Color.parseColor("#10B981"), tp)
        drawMenuButton(canvas, btnContinueRect, context.getString(R.string.pause_continue), Color.parseColor("#3B82F6"), tp)
        drawMenuButton(canvas, btnQuitRect, context.getString(R.string.pause_quit), Color.parseColor("#EF4444"), tp)

        if (savedToastUntil > System.currentTimeMillis()) {
            tp.textSize = 36f
            tp.color = Color.parseColor("#FFD700")
            canvas.drawText(context.getString(R.string.pause_saved), width / 2f, btnQuitRect.bottom + 80f, tp)
            tp.color = Color.WHITE
        }
    }

    private fun drawMenuButton(canvas: Canvas, rect: RectF, label: String, color: Int, tp: Paint) {
        val bg = Paint().apply { this.color = color; isAntiAlias = true }
        canvas.drawRoundRect(rect, 30f, 30f, bg)
        canvas.drawText(label, rect.centerX(), rect.centerY() + 15f, tp)
    }

    private fun drawFood(canvas: Canvas, pos: WorldPoint, type: FoodType, startWorldX: Float, startWorldY: Float) {
        val screenX = offsetX + (pos.x - startWorldX) * cellSize
        val screenY = offsetY + (pos.y - startWorldY) * cellSize
        val cx = screenX + cellSize / 2
        val cy = screenY + cellSize / 2
        val pulseSpeed = when (type) {
            FoodType.RED -> 1f
            FoodType.GREEN -> 1.5f
            FoodType.BLUE -> 0.7f
            FoodType.YELLOW -> 2f
            FoodType.PURPLE -> 2.5f
        }
        val pulse = 1f + 0.3f * sin(pulseAnim * pulseSpeed).toFloat()
        canvas.drawCircle(cx, cy, cellSize * 1.4f * pulse, paintFoodGlow[type]!!)
        canvas.drawCircle(cx, cy, cellSize * 0.45f, paintFood[type]!!)
        val symPaint = Paint().apply { color = Color.WHITE; textSize = cellSize * 0.6f; isAntiAlias = true; typeface = Typeface.DEFAULT_BOLD; textAlign = Paint.Align.CENTER }
        val symbol = when (type) {
            FoodType.RED -> ""
            FoodType.GREEN -> "+"
            FoodType.BLUE -> "❄"
            FoodType.YELLOW -> "⚡"
            FoodType.PURPLE -> "★"
        }
        if (symbol.isNotEmpty()) {
            canvas.drawText(symbol, cx, cy + cellSize * 0.2f, symPaint)
        }
    }

    private fun drawSnakeHead(canvas: Canvas, sx: Float, sy: Float) {
        val inset = 1.5f
        val rect = RectF(sx + inset, sy + inset, sx + cellSize - inset, sy + cellSize - inset)
        paintSnakeHead.shader = LinearGradient(
            sx, sy, sx + cellSize, sy + cellSize,
            Color.parseColor("#86EFAC"), Color.parseColor("#16A34A"),
            Shader.TileMode.CLAMP
        )
        canvas.drawRoundRect(rect, cellSize * 0.35f, cellSize * 0.35f, paintSnakeHead)

        val dir = engine.lastDirection
        val eyeSize = cellSize * 0.15f
        val pupilSize = cellSize * 0.08f
        val offset = cellSize * 0.22f
        val centerOffset = cellSize * 0.18f

        val (dx, dy, perpX, perpY) = when (dir) {
            Direction.UP -> floatArrayOf(0f, -offset, 1f, 0f)
            Direction.DOWN -> floatArrayOf(0f, offset, 1f, 0f)
            Direction.LEFT -> floatArrayOf(-offset, 0f, 0f, 1f)
            Direction.RIGHT -> floatArrayOf(offset, 0f, 0f, 1f)
        }
        val cx = sx + cellSize / 2 + dx
        val cy = sy + cellSize / 2 + dy
        val eye1X = cx + perpX * centerOffset
        val eye1Y = cy + perpY * centerOffset
        val eye2X = cx - perpX * centerOffset
        val eye2Y = cy - perpY * centerOffset

        canvas.drawCircle(eye1X, eye1Y, eyeSize, paintEyeWhite)
        canvas.drawCircle(eye2X, eye2Y, eyeSize, paintEyeWhite)

        val pupilShift = cellSize * 0.04f
        val pupilDx = when (dir) { Direction.RIGHT -> pupilShift; Direction.LEFT -> -pupilShift; else -> 0f }
        val pupilDy = when (dir) { Direction.DOWN -> pupilShift; Direction.UP -> -pupilShift; else -> 0f }
        canvas.drawCircle(eye1X + pupilDx, eye1Y + pupilDy, pupilSize, paintEyePupil)
        canvas.drawCircle(eye2X + pupilDx, eye2Y + pupilDy, pupilSize, paintEyePupil)
    }

    private fun drawSnakeSegment(canvas: Canvas, sx: Float, sy: Float, index: Int, totalSize: Int) {
        val progress = index.toFloat() / totalSize.toFloat()
        val sizeShrink = 1f - progress * 0.35f
        val inset = cellSize * (1f - sizeShrink) / 2f + 1f
        val rect = RectF(sx + inset, sy + inset, sx + cellSize - inset, sy + cellSize - inset)
        val startColor = Color.parseColor("#22C55E")
        val endColor = Color.parseColor("#14532D")
        val r = (Color.red(startColor) + (Color.red(endColor) - Color.red(startColor)) * progress).toInt()
        val g = (Color.green(startColor) + (Color.green(endColor) - Color.green(startColor)) * progress).toInt()
        val b = (Color.blue(startColor) + (Color.blue(endColor) - Color.blue(startColor)) * progress).toInt()
        paintSnakeBody.color = Color.rgb(r, g, b)
        val corner = cellSize * 0.3f * sizeShrink
        canvas.drawRoundRect(rect, corner, corner, paintSnakeBody)
    }

    private fun drawFoodCompass(canvas: Canvas, foodCx: Float, foodCy: Float) {
        val centerX = width / 2f
        val centerY = height / 2f
        val dx = foodCx - centerX
        val dy = foodCy - centerY
        val angle = atan2(dy, dx)
        val margin = 60f
        val maxDist = min(centerX - margin, centerY - margin)
        val edgeX = centerX + cos(angle) * maxDist
        val edgeY = centerY + sin(angle) * maxDist
        canvas.drawCircle(edgeX, edgeY, 35f, paintCompassGlow)
        val arrowSize = 22f
        val path = Path()
        val tipX = edgeX + cos(angle) * arrowSize
        val tipY = edgeY + sin(angle) * arrowSize
        val b1 = angle + Math.PI.toFloat() * 0.75f
        val b2 = angle - Math.PI.toFloat() * 0.75f
        path.moveTo(tipX, tipY)
        path.lineTo(edgeX + cos(b1) * arrowSize * 0.7f, edgeY + sin(b1) * arrowSize * 0.7f)
        path.lineTo(edgeX + cos(b2) * arrowSize * 0.7f, edgeY + sin(b2) * arrowSize * 0.7f)
        path.close()
        canvas.drawPath(path, paintCompass)
        val dist = sqrt(dx * dx + dy * dy) / cellSize
        val distText = dist.toInt().toString() + "m"
        paintSmallText.color = Color.parseColor("#FFFF4757")
        paintSmallText.textSize = 22f
        canvas.drawText(distText, edgeX - paintSmallText.measureText(distText) / 2f, edgeY - 30f, paintSmallText)
        paintSmallText.color = Color.WHITE
    }

    private fun drawButton(canvas: Canvas, cx: Float, cy: Float, dir: Direction) {
        val paint = if (pressedBtn == dir) paintBtnPressed else paintBtn
        canvas.drawCircle(cx, cy, btnSize / 2f, paint)
        val arrowSize = btnSize / 3f
        val path = Path()
        when (dir) {
            Direction.UP -> { path.moveTo(cx, cy - arrowSize); path.lineTo(cx - arrowSize, cy + arrowSize / 2); path.lineTo(cx + arrowSize, cy + arrowSize / 2) }
            Direction.DOWN -> { path.moveTo(cx, cy + arrowSize); path.lineTo(cx - arrowSize, cy - arrowSize / 2); path.lineTo(cx + arrowSize, cy - arrowSize / 2) }
            Direction.LEFT -> { path.moveTo(cx - arrowSize, cy); path.lineTo(cx + arrowSize / 2, cy - arrowSize); path.lineTo(cx + arrowSize / 2, cy + arrowSize) }
            Direction.RIGHT -> { path.moveTo(cx + arrowSize, cy); path.lineTo(cx - arrowSize / 2, cy - arrowSize); path.lineTo(cx - arrowSize / 2, cy + arrowSize) }
        }
        path.close()
        canvas.drawPath(path, paintArrow)
    }

    private fun isInsideBtn(x: Float, y: Float, cx: Float, cy: Float): Boolean {
        val dx = x - cx
        val dy = y - cy
        return dx * dx + dy * dy <= (btnSize / 2f) * (btnSize / 2f)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                touchStartX = event.x
                touchStartY = event.y

                if (paused) {
                    when {
                        btnSaveRect.contains(event.x, event.y) -> {
                            SaveManager.save(context, engine.snapshot())
                            savedToastUntil = System.currentTimeMillis() + 2000
                        }
                        btnContinueRect.contains(event.x, event.y) -> paused = false
                        btnQuitRect.contains(event.x, event.y) -> (context as? Activity)?.finish()
                    }
                    return true
                }

                if (engine.phase == GamePhase.GAME_OVER) {
                    highScoreSaved = false
                    engine.restart()
                    prevSnakePositions = engine.snake.body.toList()
                    cameraX = engine.snake.head.x.toFloat()
                    cameraY = engine.snake.head.y.toFloat()
                    return true
                }

                if (engine.phase == GamePhase.RUNNING && pauseRect.contains(event.x, event.y)) {
                    paused = true
                    inputQueue.clear()
                    return true
                }

                if (useButtons) {
                    when {
                        isInsideBtn(event.x, event.y, btnUpX, btnUpY) -> { pressedBtn = Direction.UP; inputQueue.add(Direction.UP) }
                        isInsideBtn(event.x, event.y, btnDownX, btnDownY) -> { pressedBtn = Direction.DOWN; inputQueue.add(Direction.DOWN) }
                        isInsideBtn(event.x, event.y, btnLeftX, btnLeftY) -> { pressedBtn = Direction.LEFT; inputQueue.add(Direction.LEFT) }
                        isInsideBtn(event.x, event.y, btnRightX, btnRightY) -> { pressedBtn = Direction.RIGHT; inputQueue.add(Direction.RIGHT) }
                    }
                }
                return true
            }
            MotionEvent.ACTION_UP -> {
                pressedBtn = null
                if (!paused && !useButtons && engine.phase == GamePhase.RUNNING) {
                    val dx = event.x - touchStartX
                    val dy = event.y - touchStartY
                    val newDir = if (abs(dx) > abs(dy)) {
                        if (dx > 30) Direction.RIGHT else if (dx < -30) Direction.LEFT else null
                    } else {
                        if (dy > 30) Direction.DOWN else if (dy < -30) Direction.UP else null
                    }
                    newDir?.let { inputQueue.add(it) }
                }
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}
    override fun surfaceDestroyed(holder: SurfaceHolder) { pause() }
}