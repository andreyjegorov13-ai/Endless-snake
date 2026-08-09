package com.example.endlesssnake.core

import com.example.endlesssnake.core.math.Direction
import com.example.endlesssnake.core.math.WorldPoint
import com.example.endlesssnake.core.snake.Snake
import com.example.endlesssnake.core.world.World
import com.example.endlesssnake.data.Difficulty
import kotlin.math.max

class GameEngine(var difficulty: Difficulty = Difficulty.NORMAL) {
    val world = World()
    var snake = Snake(WorldPoint(0, 0))
    var food = WorldPoint(5, 0)
    var bonuses: MutableList<Bonus> = mutableListOf()
    var score = 0
    var lives = 1
    var lastDirection: Direction = Direction.RIGHT

    var baseTickMs: Long = startTickFor(difficulty)
    var tickMs: Long = baseTickMs
    var phase = GamePhase.RUNNING
    var justDied = false

    private var speedBoostTicks = 0
    private var slowTicks = 0

    private val minTickMs: Long get() = when (difficulty) {
        Difficulty.EASY -> 180L
        Difficulty.NORMAL -> 80L
        Difficulty.HARD -> 55L
    }

    val foodScoreMultiplier: Int get() = when (difficulty) {
        Difficulty.EASY -> 1
        Difficulty.NORMAL -> 2
        Difficulty.HARD -> 3
    }

    companion object {
        fun startTickFor(d: Difficulty): Long = when (d) {
            Difficulty.EASY -> 400L
            Difficulty.NORMAL -> 250L
            Difficulty.HARD -> 180L
        }
    }

    fun update(dir: Direction?) {
        if (phase != GamePhase.RUNNING) return
        dir?.let {
            snake.turn(it)
            lastDirection = it
        }
        snake.move()
        val head = snake.head
        val eating = head == food

        // ФИКС БАГА 2: если не растём — хвост уходит, его клетка безопасна
        val selfHit = if (eating) {
            snake.body.drop(1).contains(head)
        } else {
            snake.body.drop(1).dropLast(1).contains(head)
        }

        if (world.hasObstacle(head, score, difficulty) || selfHit) {
            lives--
            justDied = true
            if (lives <= 0) {
                phase = GamePhase.GAME_OVER
            } else {
                respawn() // ФИКС БАГА 1: возрождение в безопасной точке
            }
            return
        }

        val eatenBonus = bonuses.firstOrNull { it.pos == head }
        if (eatenBonus != null) {
            bonuses.remove(eatenBonus)
            when (eatenBonus.type) {
                FoodType.GREEN -> lives++
                FoodType.BLUE -> slowTicks = 40
                FoodType.YELLOW -> {
                    speedBoostTicks = 30
                    score += foodScoreMultiplier * 2
                }
                FoodType.PURPLE -> score += 10
                FoodType.RED -> {}
            }
        }

        if (eating) {
            snake.feed()
            score += foodScoreMultiplier
            spawnFood()
            updateDifficulty()
            val r = Math.random()
            when {
                r < 0.05 -> spawnBonusOfType(FoodType.GREEN)
                r < 0.10 -> spawnBonusOfType(FoodType.BLUE)
                r < 0.15 -> spawnBonusOfType(FoodType.YELLOW)
                r < 0.17 -> spawnBonusOfType(FoodType.PURPLE)
            }
        }

        if (speedBoostTicks > 0) speedBoostTicks--
        if (slowTicks > 0) slowTicks--
        updateEffectiveTick()
    }

    // Возрождение: ближайшая свободная клетка + расчистка места вокруг
    private fun respawn() {
        val base = snake.head
        var spot: WorldPoint? = null
        var r = 0
        while (spot == null && r <= 8) {
            for (dx in -r..r) {
                for (dy in -r..r) {
                    val p = WorldPoint(base.x + dx, base.y + dy)
                    if (!world.hasObstacle(p, score, difficulty)) { spot = p; break }
                }
                if (spot != null) break
            }
            r++
        }
        val s = spot ?: base
        for (dx in -2..2) for (dy in -2..2) {
            world.clearObstacle(WorldPoint(s.x + dx, s.y + dy), score, difficulty)
        }
        snake = Snake(s)
        snake.direction = lastDirection
    }

    private fun updateEffectiveTick() {
        var effective = baseTickMs
        if (speedBoostTicks > 0) effective = max(minTickMs, effective - 60)
        if (slowTicks > 0) effective += 80
        tickMs = effective
    }

    private fun updateDifficulty() {
        val step = when (difficulty) {
            Difficulty.EASY -> 1L
            Difficulty.NORMAL -> 3L
            Difficulty.HARD -> 5L
        }
        baseTickMs = max(minTickMs, baseTickMs - step)
        updateEffectiveTick()
    }

    private fun spawnFood() {
        var attempts = 0
        while (attempts < 100) {
            val dx = (Math.random() * 20 - 10).toInt()
            val dy = (Math.random() * 20 - 10).toInt()
            val p = WorldPoint(snake.head.x + dx, snake.head.y + dy)
            if (canSpawnAt(p)) { food = p; return }
            attempts++
        }
    }

    private fun spawnBonusOfType(type: FoodType) {
        var attempts = 0
        while (attempts < 50) {
            val dx = (Math.random() * 24 - 12).toInt()
            val dy = (Math.random() * 24 - 12).toInt()
            val p = WorldPoint(snake.head.x + dx, snake.head.y + dy)
            if (canSpawnAt(p) && p != food) {
                bonuses.add(Bonus(p, type))
                return
            }
            attempts++
        }
    }

    private fun canSpawnAt(p: WorldPoint): Boolean {
        if (snake.contains(p)) return false
        if (world.hasObstacle(p, score, difficulty)) return false
        if (bonuses.any { it.pos == p }) return false
        return true
    }

    fun snapshot(): GameSnapshot = GameSnapshot(
        score = score,
        lives = lives,
        baseTickMs = baseTickMs,
        difficulty = difficulty,
        direction = snake.direction,
        snake = snake.body.toList(),
        food = food,
        bonuses = bonuses.toList(),
        chunks = world.exportChunks()
    )

    fun restore(s: GameSnapshot) {
        difficulty = s.difficulty
        score = s.score
        lives = s.lives
        baseTickMs = s.baseTickMs
        food = s.food
        bonuses.clear()
        bonuses.addAll(s.bonuses)
        val first = s.snake.firstOrNull() ?: WorldPoint(0, 0)
        snake = Snake(first)
        snake.body.clear()
        s.snake.forEach { snake.body.addLast(it) }
        snake.direction = s.direction
        lastDirection = s.direction
        world.importChunks(s.chunks)
        speedBoostTicks = 0
        slowTicks = 0
        updateEffectiveTick()
        phase = GamePhase.RUNNING
        justDied = false
    }

    fun restart() {
        snake = Snake(WorldPoint(0, 0))
        food = WorldPoint(5, 0)
        bonuses.clear()
        score = 0
        lives = 1
        baseTickMs = startTickFor(difficulty)
        tickMs = baseTickMs
        speedBoostTicks = 0
        slowTicks = 0
        phase = GamePhase.RUNNING
        justDied = false
        lastDirection = Direction.RIGHT
    }
}