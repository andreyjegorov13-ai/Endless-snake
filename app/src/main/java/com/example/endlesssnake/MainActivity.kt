package com.example.endlesssnake

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Space
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.TextViewCompat
import com.example.endlesssnake.data.SaveManager
import com.example.endlesssnake.data.SettingsRepository
import com.example.endlesssnake.util.LocaleHelper

class MainActivity : AppCompatActivity() {
    private lateinit var settings: SettingsRepository
    private lateinit var bestScoreView: TextView
    private var currentLang: String = ""
    private var saveExists: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        settings = SettingsRepository(this)
        currentLang = settings.language
        saveExists = SaveManager.hasSave(this)
        LocaleHelper.apply(this, currentLang)

        val dp = resources.displayMetrics.density
        fun dp(v: Int) = (v * dp).toInt()

        val scroll = android.widget.ScrollView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
            background = GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                intArrayOf(
                    Color.parseColor("#020617"),
                    Color.parseColor("#0F172A"),
                    Color.parseColor("#1E1B4B"),
                    Color.parseColor("#312E81"),
                    Color.parseColor("#0F172A"),
                    Color.parseColor("#020617")
                )
            )
            isFillViewport = true
        }

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(dp(32), dp(60), dp(32), dp(60))
        }

        val stars = TextView(this).apply {
            text = "✦    ✧    ✦"
            textSize = 22f
            setTextColor(Color.argb(120, 167, 139, 255))
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, dp(16))
        }

        val title = TextView(this).apply {
            text = getString(R.string.title)
            maxLines = 1
            setTextColor(Color.WHITE)
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            gravity = Gravity.CENTER
            paint.isAntiAlias = true
            setShadowLayer(30f, 0f, 0f, Color.parseColor("#10B981"))
            setLayerType(android.view.View.LAYER_TYPE_SOFTWARE, paint)
            setPadding(0, dp(8), 0, dp(8))
        }
        TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(
            title, 18, 40, 2, TypedValue.COMPLEX_UNIT_SP
        )

        val subtitle = TextView(this).apply {
            text = getString(R.string.subtitle)
            textSize = 15f
            setTextColor(Color.argb(180, 200, 200, 220))
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, dp(40))
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
            letterSpacing = 0.15f
        }

        val cardBg = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = dp(24).toFloat()
            setColor(Color.argb(25, 255, 255, 255))
            setStroke(1, Color.argb(80, 167, 139, 255))
        }
        val cardContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            background = cardBg
            setPadding(dp(40), dp(28), dp(40), dp(28))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(dp(16), 0, dp(16), dp(40)) }
        }
        bestScoreView = TextView(this).apply {
            text = getString(R.string.best_score, settings.bestScore)
            textSize = 32f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val bestLabel = TextView(this).apply {
            text = if (settings.language == "en") "HIGH SCORE" else "ЛУЧШИЙ РЕЗУЛЬТАТ"
            textSize = 12f
            setTextColor(Color.argb(180, 16, 185, 129))
            gravity = Gravity.CENTER
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            letterSpacing = 0.2f
        }
        cardContainer.addView(bestLabel)
        cardContainer.addView(bestScoreView)

        root.addView(stars)
        root.addView(title)
        root.addView(subtitle)
        root.addView(cardContainer)

        if (saveExists) {
            val btnContinue = makeGradientButton(getString(R.string.menu_continue),
                intArrayOf(Color.parseColor("#F59E0B"), Color.parseColor("#EF4444"))) {
                startActivity(Intent(this@MainActivity, GameActivity::class.java).putExtra("load_save", true))
            }
            root.addView(btnContinue)
            root.addView(Space(this).apply { layoutParams = LinearLayout.LayoutParams(0, dp(12)) })
        }

        val btnPlay = makeGradientButton(getString(R.string.play),
            intArrayOf(Color.parseColor("#10B981"), Color.parseColor("#059669"))) {
            startActivity(Intent(this@MainActivity, GameActivity::class.java))
        }
        val btnCatalog = makeGradientButton(getString(R.string.catalog),
            intArrayOf(Color.parseColor("#F59E0B"), Color.parseColor("#D97706"))) {
            startActivity(Intent(this@MainActivity, CatalogActivity::class.java))
        }
        val btnCredits = makeGradientButton(getString(R.string.credits),
            intArrayOf(Color.parseColor("#EC4899"), Color.parseColor("#BE185D"))) {
            startActivity(Intent(this@MainActivity, CreditsActivity::class.java))
        }
        val btnSettings = makeGradientButton(getString(R.string.settings),
            intArrayOf(Color.parseColor("#8B5CF6"), Color.parseColor("#6366F1"))) {
            startActivity(Intent(this@MainActivity, SettingsActivity::class.java))
        }
        val btnExit = makeGradientButton(getString(R.string.exit),
            intArrayOf(Color.parseColor("#64748B"), Color.parseColor("#475569"))) {
            finish()
        }

        root.addView(btnPlay)
        root.addView(Space(this).apply { layoutParams = LinearLayout.LayoutParams(0, dp(12)) })
        root.addView(btnCatalog)
        root.addView(Space(this).apply { layoutParams = LinearLayout.LayoutParams(0, dp(12)) })
        root.addView(btnCredits)
        root.addView(Space(this).apply { layoutParams = LinearLayout.LayoutParams(0, dp(12)) })
        root.addView(btnSettings)
        root.addView(Space(this).apply { layoutParams = LinearLayout.LayoutParams(0, dp(12)) })
        root.addView(btnExit)

        scroll.addView(root)
        setContentView(scroll)
    }

    override fun onResume() {
        super.onResume()
        if (settings.language != currentLang || SaveManager.hasSave(this) != saveExists) {
            recreate()
            return
        }
        if (::bestScoreView.isInitialized) {
            bestScoreView.text = getString(R.string.best_score, settings.bestScore)
        }
    }

    private fun makeGradientButton(text: String, colors: IntArray, onClick: () -> Unit): Button {
        val dp = resources.displayMetrics.density
        fun dp(v: Int) = (v * dp).toInt()
        val bg = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = dp(16).toFloat()
            this.colors = colors
            orientation = GradientDrawable.Orientation.LEFT_RIGHT
        }
        return Button(this).apply {
            this.text = text
            textSize = 17f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            setTextColor(Color.WHITE)
            background = bg
            elevation = dp(8).toFloat()
            stateListAnimator = null
            setPadding(dp(20), dp(20), dp(20), dp(20))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(62)
            ).apply { setMargins(dp(16), 0, dp(16), 0) }
            setOnClickListener { onClick() }
        }
    }
}