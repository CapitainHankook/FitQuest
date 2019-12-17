package ru.tzhack.facegame.bird.gameobj

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import ru.tzhack.facegame.R
import ru.tzhack.facegame.bird.Viewport
import ru.tzhack.facegame.bird.utils.Position
import ru.tzhack.facegame.bird.utils.createBitmap

class Bullet(val position: Position) {

    companion object {
        const val shotDebounce = 2000

        private const val SPEED = 600
        private const val MAX_DISTANCE = 600

        private const val K_SPRITE = 2f
        private const val widthFly = 89f / K_SPRITE
        private const val heightFly = 360f / K_SPRITE
        private lateinit var bitmapFly: Bitmap
        private const val sideCrashed = 224f
        private lateinit var bitmapCrashed: Bitmap
        private const val CRASHED_MAX_TIME = 0.5f

        fun init(context: Context) {
            bitmapFly = context.createBitmap(
                R.drawable.bullet_flies,
                widthFly,
                heightFly
            )
            bitmapCrashed = context.createBitmap(
                R.drawable.bullet_crashed,
                sideCrashed,
                sideCrashed
            )
        }

        fun create(birdPosition: Position): Bullet {
            return Bullet(
                Position(
                    left = birdPosition.left - widthFly / 2 + birdPosition.width / 2,
                    top = birdPosition.top + heightFly,
                    width = widthFly,
                    height = heightFly
                )
            )
        }
    }

    private var crashed = false
    private val startTopPosition = position.top
    private var crashedTime = 0f

    fun update(dt: Float) {
        if (!crashed) {
            position.top += SPEED * dt
        } else {
            crashedTime += dt
        }
    }

    fun isCleared(): Boolean {
        return if (crashed) {
            crashedTime >= CRASHED_MAX_TIME
        } else {
            position.top - startTopPosition >= MAX_DISTANCE
        }
    }

    fun setCrashed() {
        crashed = true
        val offset = sideCrashed / 2f
        position.left -= offset
        position.top += offset
        if (position.left < 0f) {
            position.left = 0f
        }
    }

    fun draw(canvas: Canvas, paint: Paint, viewport: Viewport) {
        canvas.drawBitmap(
            if (crashed) bitmapCrashed else bitmapFly,
            position.left,
            viewport.worldToScreenPoint(position.top),
            paint
        )
    }
}