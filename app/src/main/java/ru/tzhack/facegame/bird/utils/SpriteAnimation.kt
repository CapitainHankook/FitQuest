package ru.tzhack.facegame.bird.utils

import android.graphics.Bitmap

/**
 * Отвечает за спрайтовую анимацию, обеспечивает переключение фреймов
 */
class SpriteAnimation(
    private val frames: Array<Bitmap>,
    cycleSec: Float
) {

    private var frame = 0
    private var currentFrameTime = 0f
    private val frameTime = cycleSec / frames.size
    var cycleCount = 0

    fun update(dt: Float) {
        currentFrameTime += dt
        if (currentFrameTime > frameTime) {
            frame++
            currentFrameTime = 0f
            if (frame >= frames.size) {
                frame = 0
                cycleCount++
            }
        }
    }

    fun getFrame(): Bitmap {
        return frames[frame]
    }

    fun reset() {
        frame = 0
        currentFrameTime = 0f
        cycleCount = 0
    }

}