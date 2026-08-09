package com.example.endlesssnake

import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.example.endlesssnake.android.GameSurfaceView
import com.example.endlesssnake.data.SettingsRepository
import com.example.endlesssnake.util.LocaleHelper

class GameActivity : AppCompatActivity() {
    private lateinit var surfaceView: GameSurfaceView
    private lateinit var settings: SettingsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        settings = SettingsRepository(this)
        LocaleHelper.apply(this, settings.language)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        val loadSave = intent.getBooleanExtra("load_save", false)
        surfaceView = GameSurfaceView(this, loadSave)
        setContentView(surfaceView)
    }

    override fun onPause() {
        super.onPause()
        surfaceView.pause()
    }

    override fun onResume() {
        super.onResume()
        surfaceView.resume()
    }

    override fun onDestroy() {
        super.onDestroy()
        surfaceView.destroy()
    }
}