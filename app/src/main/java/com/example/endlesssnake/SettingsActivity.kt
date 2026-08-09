package com.example.endlesssnake

import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.SeekBar
import android.widget.Space
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.endlesssnake.data.Difficulty
import com.example.endlesssnake.data.SettingsRepository
import com.example.endlesssnake.util.LocaleHelper

class SettingsActivity : AppCompatActivity() {
    private lateinit var settings: SettingsRepository
    private var currentLang: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        settings = SettingsRepository(this)
        currentLang = settings.language
        LocaleHelper.apply(this, currentLang)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(60, 80, 60, 80)
            background = GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                intArrayOf(
                    Color.parseColor("#0F2027"),
                    Color.parseColor("#203A43"),
                    Color.parseColor("#2C5364")
                )
            )
        }

        val title = TextView(this).apply {
            text = getString(R.string.settings_title)
            textSize = 32f
            setTextColor(Color.WHITE)
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 40)
        }

        // === Громкость эффектов (SeekBar) ===
        val soundVolumeLabel = TextView(this).apply {
            text = getString(R.string.sound_volume, settings.soundVolume)
            textSize = 18f
            setTextColor(Color.WHITE)
            setPadding(0, 20, 0, 10)
        }
        val soundVolumeSeekBar = SeekBar(this).apply {
            max = 100
            progress = settings.soundVolume
            setPadding(0, 0, 0, 20)
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(sb: SeekBar?, progress: Int, fromUser: Boolean) {
                    soundVolumeLabel.text = getString(R.string.sound_volume, progress)
                }
                override fun onStartTrackingTouch(sb: SeekBar?) {}
                override fun onStopTrackingTouch(sb: SeekBar?) {}
            })
        }

        // === Громкость музыки (SeekBar) ===
        val musicVolumeLabel = TextView(this).apply {
            text = getString(R.string.music_volume, settings.musicVolume)
            textSize = 18f
            setTextColor(Color.WHITE)
            setPadding(0, 10, 0, 10)
        }
        val musicVolumeSeekBar = SeekBar(this).apply {
            max = 100
            progress = settings.musicVolume
            setPadding(0, 0, 0, 20)
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(sb: SeekBar?, progress: Int, fromUser: Boolean) {
                    musicVolumeLabel.text = getString(R.string.music_volume, progress)
                }
                override fun onStartTrackingTouch(sb: SeekBar?) {}
                override fun onStopTrackingTouch(sb: SeekBar?) {}
            })
        }

        // === Сложность ===
        val diffLabel = TextView(this).apply {
            text = getString(R.string.difficulty)
            textSize = 20f
            setTextColor(Color.WHITE)
            typeface = Typeface.DEFAULT_BOLD
            setPadding(0, 30, 0, 10)
        }
        val diffGroup = RadioGroup(this).apply { orientation = RadioGroup.VERTICAL }
        val rbEasy = RadioButton(this).apply { text = getString(R.string.easy); textSize = 17f; setTextColor(Color.WHITE); id = 1 }
        val rbNormal = RadioButton(this).apply { text = getString(R.string.normal); textSize = 17f; setTextColor(Color.WHITE); id = 2 }
        val rbHard = RadioButton(this).apply { text = getString(R.string.hard); textSize = 17f; setTextColor(Color.WHITE); id = 3 }
        diffGroup.addView(rbEasy)
        diffGroup.addView(rbNormal)
        diffGroup.addView(rbHard)
        when (settings.difficulty) {
            Difficulty.EASY -> diffGroup.check(1)
            Difficulty.NORMAL -> diffGroup.check(2)
            Difficulty.HARD -> diffGroup.check(3)
        }

        // === Управление ===
        val ctrlLabel = TextView(this).apply {
            text = getString(R.string.controls)
            textSize = 20f
            setTextColor(Color.WHITE)
            typeface = Typeface.DEFAULT_BOLD
            setPadding(0, 30, 0, 10)
        }
        val ctrlGroup = RadioGroup(this).apply { orientation = RadioGroup.VERTICAL }
        val rbSwipe = RadioButton(this).apply { text = getString(R.string.swipe); textSize = 17f; setTextColor(Color.WHITE); id = 1 }
        val rbButtons = RadioButton(this).apply { text = getString(R.string.buttons); textSize = 17f; setTextColor(Color.WHITE); id = 2 }
        ctrlGroup.addView(rbSwipe)
        ctrlGroup.addView(rbButtons)
        if (settings.useButtons) ctrlGroup.check(2) else ctrlGroup.check(1)

        // === Язык ===
        val langLabel = TextView(this).apply {
            text = getString(R.string.language)
            textSize = 20f
            setTextColor(Color.WHITE)
            typeface = Typeface.DEFAULT_BOLD
            setPadding(0, 30, 0, 10)
        }
        val langGroup = RadioGroup(this).apply { orientation = RadioGroup.VERTICAL }
        val rbRu = RadioButton(this).apply { text = getString(R.string.lang_ru); textSize = 17f; setTextColor(Color.WHITE); id = 1 }
        val rbEn = RadioButton(this).apply { text = getString(R.string.lang_en); textSize = 17f; setTextColor(Color.WHITE); id = 2 }
        langGroup.addView(rbRu)
        langGroup.addView(rbEn)
        if (settings.language == "en") langGroup.check(2) else langGroup.check(1)

        // === Кнопка сохранить ===
        val btnBg = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 50f
            colors = intArrayOf(Color.parseColor("#43E97B"), Color.parseColor("#38F9D7"))
            orientation = GradientDrawable.Orientation.LEFT_RIGHT
        }
        val btnSave = Button(this).apply {
            text = getString(R.string.save_back)
            textSize = 20f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.WHITE)
            background = btnBg
            elevation = 12f
            setPadding(40, 50, 40, 50)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = 60 }
            setOnClickListener {
                settings.soundVolume = soundVolumeSeekBar.progress
                settings.musicVolume = musicVolumeSeekBar.progress
                settings.difficulty = when (diffGroup.checkedRadioButtonId) {
                    1 -> Difficulty.EASY
                    3 -> Difficulty.HARD
                    else -> Difficulty.NORMAL
                }
                settings.useButtons = ctrlGroup.checkedRadioButtonId == 2
                val newLang = if (langGroup.checkedRadioButtonId == 2) "en" else "ru"
                settings.language = newLang
                if (newLang != currentLang) {
                    LocaleHelper.apply(this@SettingsActivity, newLang)
                }
                finish()
            }
        }

        root.addView(title)
        root.addView(soundVolumeLabel)
        root.addView(soundVolumeSeekBar)
        root.addView(musicVolumeLabel)
        root.addView(musicVolumeSeekBar)
        root.addView(diffLabel)
        root.addView(diffGroup)
        root.addView(ctrlLabel)
        root.addView(ctrlGroup)
        root.addView(langLabel)
        root.addView(langGroup)
        root.addView(btnSave)

        setContentView(root)
    }
}