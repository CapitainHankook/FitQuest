package ru.tzhack.facegame.bird.gameobj

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Point
import ru.tzhack.facegame.R
import ru.tzhack.facegame.bird.Viewport
import ru.tzhack.facegame.bird.utils.Position
import ru.tzhack.facegame.bird.utils.createBitmap
import kotlin.random.Random

private const val sideSprite = 400f / 3.5f

enum class BonusType {
    SPEED_UP,
    SPEED_DOWN,
    SHOT,
    TIME
}

class Bonus(
    private val position: Position,
    private val speed: Float,
    val type: BonusType
) {

    companion object {
        private val bitmaps = ArrayList<Bitmap>(4)

        private val SPEED_VERTICAL = 100..200

        private const val paddingHorizontal = 50
        private const val spaceY = 1000

        private var screenY: Int = 0
        private var leftMax: Int = 0

        var generateWhenPositionY = 700

        fun init(context: Context, screenSize: Point) {
            bitmaps.add(
                context.createBitmap(
                    R.drawable.bonus_speed_up,
                    sideSprite,
                    sideSprite
                )
            )
            bitmaps.add(
                context.createBitmap(
                    R.drawable.bonus_speed_down,
                    sideSprite,
                    sideSprite
                )
            )
            bitmaps.add(
                context.createBitmap(
                    R.drawable.bonus_shot,
                    sideSprite,
                    sideSprite
                )
            )
            bitmaps.add(
                context.createBitmap(
                    R.drawable.bonus_time,
                    sideSprite,
                    sideSprite
                )
            )

            screenY = screenSize.y
            leftMax = screenSize.x - paddingHorizontal - sideSprite.toInt()
        }

        fun generate(): Bonus {
            val left = Random.nextInt(
                paddingHorizontal,
                leftMax
            ).toFloat()
            generateWhenPositionY += spaceY
            return Bonus(
                Position(
                    left = left,
                    top = (generateWhenPositionY + screenY).toFloat(),
                    width = sideSprite,
                    height = sideSprite
                ),
                Random.nextInt(
                    SPEED_VERTICAL.first,
                    SPEED_VERTICAL.last
                ).toFloat(),
                BonusType.values()[Random.nextInt(BonusType.values().size)]
            )
        }
    }

    fun collision(pos: Position): Boolean {
        if (position.contains(pos)) {
            return true
        }

        return false
    }

    fun update(dt: Float) {
        position.top -= speed * dt
    }

    fun draw(canvas: Canvas, paint: Paint, viewport: Viewport) {
        if (!viewport.nowOnScreen(position)) {
            return
        }
        canvas.drawBitmap(
            bitmaps[type.ordinal],
            position.left,
            viewport.worldToScreenPoint(position.top),
            paint
        )
    }

}