package com.example.endlesssnake

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.endlesssnake.data.SettingsRepository
import com.example.endlesssnake.util.LocaleHelper

class CreditsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val settings = SettingsRepository(this)
        LocaleHelper.apply(this, settings.language)

        val dp = resources.displayMetrics.density
        fun dp(v: Int) = (v * dp).toInt()

        val scroll = ScrollView(this).apply {
            background = GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                intArrayOf(Color.parseColor("#020617"), Color.parseColor("#1E1B4B"), Color.parseColor("#312E81"))
            )
            isFillViewport = true
        }

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(dp(24), dp(40), dp(24), dp(40))
        }

        val title = TextView(this).apply {
            text = getString(R.string.credits_title)
            textSize = 28f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            setShadowLayer(20f, 0f, 0f, Color.parseColor("#EC4899"))
            setLayerType(View.LAYER_TYPE_SOFTWARE, paint)
            setPadding(0, 0, 0, dp(30))
        }
        root.addView(title)

        // === Карточка Андрея: Дзен + ВК в одну строку, ниже большая кнопка ===
        root.addView(makeDevCard(
            gradient = intArrayOf(Color.parseColor("#10B981"), Color.parseColor("#059669")),
            name = getString(R.string.dev1_name),
            role = getString(R.string.dev1_role),
            rowLinks = listOf(
                getString(R.string.link_dzen) to "https://dzen.ru/easyanalitic",
                getString(R.string.link_vk) to "https://vk.ru/andreyjegorov"
            ),
            fullLinks = listOf(
                getString(R.string.link_us) to "https://www.spilornis.com/andreyegorov"
            )
        ))

        // === Карточка Елены: одна большая кнопка ===
        root.addView(makeDevCard(
            gradient = intArrayOf(Color.parseColor("#EC4899"), Color.parseColor("#BE185D")),
            name = getString(R.string.dev2_name),
            role = getString(R.string.dev2_role),
            rowLinks = emptyList(),
            fullLinks = listOf(
                getString(R.string.link_us) to "https://www.spilornis.com/elenabazhenova/"
            )
        ))

        // === Баннер о приватности ===
        val privacyBanner = TextView(this).apply {
            text = getString(R.string.privacy_note)
            textSize = 13f
            setTextColor(Color.argb(210, 190, 210, 255))
            gravity = Gravity.CENTER
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = dp(14).toFloat()
                setColor(Color.argb(25, 120, 160, 255))
                setStroke(1, Color.argb(70, 120, 160, 255))
            }
            setPadding(dp(16), dp(12), dp(16), dp(12))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = dp(4); bottomMargin = dp(16) }
        }
        root.addView(privacyBanner)

        // === Кнопка назад ===
        val btnBack = Button(this).apply {
            text = getString(R.string.back)
            textSize = 17f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            setTextColor(Color.WHITE)
            isAllCaps = false
            val bg = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = dp(14).toFloat()
                colors = intArrayOf(Color.parseColor("#64748B"), Color.parseColor("#475569"))
                orientation = GradientDrawable.Orientation.LEFT_RIGHT
            }
            background = bg
            elevation = dp(6).toFloat()
            stateListAnimator = null
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(56)
            )
            setOnClickListener { finish() }
        }
        root.addView(btnBack)

        scroll.addView(root)
        setContentView(scroll)
    }

    private fun makeDevCard(
        gradient: IntArray,
        name: String,
        role: String,
        rowLinks: List<Pair<String, String>>,
        fullLinks: List<Pair<String, String>>
    ): LinearLayout {
        val dp = resources.displayMetrics.density
        fun dp(v: Int) = (v * dp).toInt()

        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            val bg = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = dp(22).toFloat()
                setColor(Color.argb(35, 255, 255, 255))
                setStroke(2, gradient[0])
            }
            background = bg
            setPadding(dp(24), dp(28), dp(24), dp(16))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = dp(20) }
        }

        val nameView = TextView(this).apply {
            text = name
            textSize = 20f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, dp(4))
        }

        val roleView = TextView(this).apply {
            text = role
            textSize = 14f
            setTextColor(Color.argb(200, 167, 139, 255))
            gravity = Gravity.CENTER
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
            setPadding(0, 0, 0, dp(18))
        }

        card.addView(nameView)
        card.addView(roleView)

        val linksContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        // Ссылки в одну строку (Дзен + ВК)
        if (rowLinks.isNotEmpty()) {
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { bottomMargin = dp(8) }
            }
            rowLinks.forEachIndexed { i, pair ->
                val btn = makeLinkButton(gradient, pair.first, pair.second)
                val lp = LinearLayout.LayoutParams(0, dp(46), 1f)
                if (i > 0) lp.marginStart = dp(6)
                if (i < rowLinks.size - 1) lp.marginEnd = dp(6)
                btn.layoutParams = lp
                row.addView(btn)
            }
            linksContainer.addView(row)
        }

        // Большие кнопки во всю ширину (Университет Спилорниса)
        fullLinks.forEach { pair ->
            val btn = makeLinkButton(gradient, pair.first, pair.second)
            btn.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(46)
            ).apply { bottomMargin = dp(8) }
            linksContainer.addView(btn)
        }

        card.addView(linksContainer)
        return card
    }

    private fun makeLinkButton(gradient: IntArray, label: String, url: String): Button {
        val dp = resources.displayMetrics.density
        fun dp(v: Int) = (v * dp).toInt()
        return Button(this).apply {
            text = label
            textSize = 14f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            setTextColor(Color.WHITE)
            isAllCaps = false
            val btnBg = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = dp(12).toFloat()
                colors = gradient
                orientation = GradientDrawable.Orientation.LEFT_RIGHT
            }
            background = btnBg
            elevation = dp(4).toFloat()
            stateListAnimator = null
            setPadding(dp(8), dp(12), dp(8), dp(12))
            setOnClickListener {
                try {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    startActivity(intent)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
}