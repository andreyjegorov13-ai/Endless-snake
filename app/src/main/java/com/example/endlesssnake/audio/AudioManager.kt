package com.example.endlesssnake.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool
import com.example.endlesssnake.R

class AudioManager(private val context: Context) {
    private var soundPool: SoundPool? = null
    private var eatSoundId: Int = 0
    private var mediaPlayer: MediaPlayer? = null

    var soundVolume: Float = 0.8f
    var musicVolume: Float = 0.6f

    init {
        val attrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        soundPool = SoundPool.Builder()
            .setMaxStreams(3)
            .setAudioAttributes(attrs)
            .build()
        eatSoundId = soundPool!!.load(context, R.raw.cut, 1)
    }

    fun playEatSound() {
        if (soundVolume > 0f && eatSoundId != 0) {
            soundPool?.play(eatSoundId, soundVolume, soundVolume, 1, 0, 1.0f)
        }
    }

    fun startMusic() {
        if (musicVolume <= 0f) return
        try {
            if (mediaPlayer == null) {
                mediaPlayer = MediaPlayer.create(context, R.raw.music_snake)
                mediaPlayer?.isLooping = true
            }
            mediaPlayer?.setVolume(musicVolume, musicVolume)
            mediaPlayer?.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun updateMusicVolume(vol: Float) {
        musicVolume = vol
        try { mediaPlayer?.setVolume(vol, vol) } catch (e: Exception) {}
    }

    fun pauseMusic() {
        try {
            mediaPlayer?.let { if (it.isPlaying) it.pause() }
        } catch (e: Exception) {}
    }

    fun release() {
        try {
            mediaPlayer?.release()
            mediaPlayer = null
            soundPool?.release()
            soundPool = null
        } catch (e: Exception) {}
    }
}