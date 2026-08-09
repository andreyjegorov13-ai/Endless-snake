package com.example.endlesssnake

import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
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

class CatalogActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val settings = SettingsRepository(this)
        LocaleHelper.apply(this, settings.language)

        val dp = resources.displayMetrics.density
        fun dp(v: Int) = (v * dp).toInt()

        val scroll = ScrollView(this).apply {
            background = GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                intArrayOf(Color.parseColor("#020617"), Color.parseColor("#0F172A"), Color.parseColor("#1E1B4B"))
            )
            isFillViewport = true
        }

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(24), dp(40), dp(24), dp(40))
        }

        val title = TextView(this).apply {
            text = getString(R.string.catalog_title)
            textSize = 28f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            paint.setShadowLayer(20f, 0f, 0f, Color.parseColor("#F59E0B"))
            setLayerType(View.LAYER_TYPE_SOFTWARE, paint)
            setPadding(0, 0, 0, dp(30))
        }
        root.addView(title)

        root.addView(makePixelCard(
            Color.parseColor("#FFFF4757"), "",
            getString(R.string.cat_red_title),
            getString(R.string.cat_red_desc)
        ))
        root.addView(makePixelCard(
            Color.parseColor("#FF00FF88"), "+",
            getString(R.string.cat_green_title),
            getString(R.string.cat_green_desc)
        ))
        root.addView(makePixelCard(
            Color.parseColor("#FF3B82F6"), "❄",
            getString(R.string.cat_blue_title),
            getString(R.string.cat_blue_desc)
        ))
        root.addView(makePixelCard(
            Color.parseColor("#FFFBBF24"), "⚡",
            getString(R.string.cat_yellow_title),
            getString(R.string.cat_yellow_desc)
        ))
        root.addView(makePixelCard(
            Color.parseColor("#FFA855F7"), "★",
            getString(R.string.cat_purple_title),
            getString(R.string.cat_purple_desc)
        ))

        val btnBack = Button(this).apply {
            text = getString(R.string.back)
            textSize = 17f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            setTextColor(Color.WHITE)
            val bg = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = dp(14).toFloat()
                colors = intArrayOf(Color.parseColor("#64748B"), Color.parseColor("#475569"))
                orientation = GradientDrawable.Orientation.LEFT_RIGHT
            }
            background = bg
            elevation = dp(6).toFloat()
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(56)
            ).apply { topMargin = dp(20) }
            setOnClickListener { finish() }
        }
        root.addView(btnBack)

        scroll.addView(root)
        setContentView(scroll)
    }

    private fun makePixelCard(color: Int, symbol: String, title: String, desc: String): LinearLayout {
        val dp = resources.displayMetrics.density
        fun dp(v: Int) = (v * dp).toInt()

        val card = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            val bg = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = dp(18).toFloat()
                setColor(Color.argb(30, 255, 255, 255))
                setStroke(1, Color.argb(80, 255, 255, 255))
            }
            background = bg
            setPadding(dp(20), dp(20), dp(20), dp(20))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = dp(14) }
        }

        val pixelContainer = LinearLayout(this).apply {
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(dp(70), dp(70))
        }
        // УБРАЛИ setShadowLayer — у GradientDrawable его не существует
        val pixelBg = GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(color)
            setStroke(2, Color.argb(120, 255, 255, 255))
        }
        val pixelView = TextView(this).apply {
            text = symbol
            textSize = 28f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            typeface = Typeface.DEFAULT_BOLD
            background = pixelBg
            layoutParams = LinearLayout.LayoutParams(dp(60), dp(60))
        }
        pixelContainer.addView(pixelView)

        val textContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f
            ).apply { leftMargin = dp(16) }
        }
        val titleView = TextView(this).apply {
            text = title
            textSize = 19f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            setTextColor(Color.WHITE)
            setPadding(0, 0, 0, dp(6))
        }
        val descView = TextView(this).apply {
            text = desc
            textSize = 14f
            setTextColor(Color.argb(200, 220, 220, 230))
            setLineSpacing(0f, 1.3f)
        }
        textContainer.addView(titleView)
        textContainer.addView(descView)

        card.addView(pixelContainer)
        card.addView(textContainer)
        return card
    }
}