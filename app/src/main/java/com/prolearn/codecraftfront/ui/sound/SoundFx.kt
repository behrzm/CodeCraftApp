package com.prolearn.codecraftfront.ui.sound

import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Handler
import android.os.Looper

/**
 * Lightweight UI sound feedback via [ToneGenerator]. No asset files required.
 *
 * Each effect maps to a short tone (or short sequence) suitable for button taps,
 * mission successes/failures and XP gain. Safe to call on any thread; failures
 * are swallowed so that emulators or devices without tone support stay silent.
 */
object SoundFx {

    enum class Effect { Success, Failure }

    @Volatile
    private var enabled: Boolean = true

    @Volatile
    private var generator: ToneGenerator? = null

    private val mainHandler = Handler(Looper.getMainLooper())

    fun setEnabled(value: Boolean) {
        enabled = value
        if (!value) release()
    }

    fun play(effect: Effect) {
        if (!enabled) return
        runCatching {
            val gen = generator ?: ToneGenerator(AudioManager.STREAM_MUSIC, 60).also {
                generator = it
            }
            when (effect) {
                Effect.Failure -> gen.startTone(ToneGenerator.TONE_PROP_NACK, 240)
                Effect.Success -> playSuccessChord(gen)
            }
        }
    }

    private fun playSuccessChord(gen: ToneGenerator) {
        gen.startTone(ToneGenerator.TONE_DTMF_1, 120)
        mainHandler.postDelayed({
            runCatching { gen.startTone(ToneGenerator.TONE_DTMF_5, 120) }
        }, 130)
        mainHandler.postDelayed({
            runCatching { gen.startTone(ToneGenerator.TONE_DTMF_9, 180) }
        }, 270)
    }

    fun release() {
        runCatching { generator?.release() }
        generator = null
    }
}
