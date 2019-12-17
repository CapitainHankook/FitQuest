package ru.tzhack.facegame.bird.gameobj

import android.content.Context
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import ru.tzhack.facegame.R
import ru.tzhack.facegame.bird.Movement
import ru.tzhack.facegame.bird.Viewport
import ru.tzhack.facegame.bird.utils.Position
import ru.tzhack.facegame.bird.utils.SpriteAnimation
import ru.tzhack.facegame.bird.utils.createBitmaps

class Bird(
    context: Context,
    private val screenX: Float
) {
    companion object {

        private const val K_SPRITE = 3f
        private const val WIDTH_SPRITE = 524f / K_SPRITE
        private const val HEIGHT_SPRITE = 616f / K_SPRITE

        private const val SPEED_VERTICAL_DEFAULT = 200
        private const val SPEED_VERTICAL_STEP = 70
        private const val SPEED_BONUS_MAX_TIME = 5
        private const val MAX_SPEED_HORIZONTAL = 500
    }

    private var speedVertical = SPEED_VERTICAL_DEFAULT
    private var movement: Movement = Movement.Stopped
    private var shotState = false

    private var speedBonusType: BonusType? = null
    private var speedBonusTime = 0f

    val position = Position(
        left = screenX / 2f - WIDTH_SPRITE / 2f,
        top = 0f,
        width = WIDTH_SPRITE,
        height = HEIGHT_SPRITE
    )

    private val spriteAnimation = SpriteAnimation(
        context.createBitmaps(
            WIDTH_SPRITE,
            HEIGHT_SPRITE,
            R.drawable.a1,
            R.drawable.a2,
            R.drawable.a3,
            R.drawable.a4,
            R.drawable.a5,
            R.drawable.a6,
            R.drawable.a7,
            R.drawable.a8
        ),
        0.5f
    )

    private val darkSpriteAnimation = SpriteAnimation(
        context.createBitmaps(
            WIDTH_SPRITE,
            HEIGHT_SPRITE,
            R.drawable.an1,
            R.drawable.an2,
            R.drawable.an3,
            R.drawable.an4,
            R.drawable.an5,
            R.drawable.an6,
            R.drawable.an7,
            R.drawable.an8
        ),
        0.5f
    )

    private val shotSpriteAnimation = SpriteAnimation(
        context.createBitmaps(
            WIDTH_SPRITE,
            HEIGHT_SPRITE,
            R.drawable.as1,
            R.drawable.as2,
            R.drawable.as3,
            R.drawable.as4
        ),
        0.5f
    )

    fun update(dt: Float, frontYBlock: Block?) {
        if (speedBonusType != null) {
            speedBonusTime += dt
            if (speedBonusTime > SPEED_BONUS_MAX_TIME) {
                speedVertical = SPEED_VERTICAL_DEFAULT
                speedBonusType = null
                speedBonusTime = 0f
            }
        }

        if (shotState && shotSpriteAnimation.cycleCount > 0) {
            shotSpriteAnimation.reset()
            spriteAnimation.reset()
            shotState = false
        }

        if (shotState) {
            shotSpriteAnimation.update(dt)
        } else {
            spriteAnimation.update(dt)
        }
        darkSpriteAnimation.update(dt)

        val newTop = position.top + speedVertical * dt
        if (frontYBlock?.collision(position.copy(top = newTop)) != true) {
            position.top = newTop
        }

        when (val state = movement) {
            is Movement.Left  -> {
                if (position.left > 0) {
                    val newLeft = position.left - MAX_SPEED_HORIZONTAL * dt * state.speedRatio
                    if (frontYBlock?.collision(position.copy(left = newLeft)) != true) {
                        position.left = newLeft
                    }
                }
            }
            is Movement.Right -> {
                if (position.left < screenX - position.width) {
                    val newLeft = position.left + MAX_SPEED_HORIZONTAL * dt * state.speedRatio
                    if (frontYBlock?.collision(position.copy(left = newLeft)) != true) {
                        position.left = newLeft
                    }
                }
            }
        }
    }

    fun draw(canvas: Canvas, paint: Paint, viewport: Viewport, nightMode: Boolean) {
        val matrix = Matrix()
        val values = FloatArray(9) { 0f }
        matrix.getValues(values)
        var offsetY = 0f

        when (movement) {
            is Movement.Left  -> {
                matrix.setRotate(-25f)
                offsetY = -50f
            }
            is Movement.Right -> {
                matrix.setRotate(25f)
            }
        }

        val matrixX = values[Matrix.MTRANS_X]
        val matrixY = values[Matrix.MTRANS_Y]
        matrix.postTranslate(position.left - matrixX, viewport.worldToScreenPoint(position.top) - matrixY - offsetY)

        canvas.drawBitmap(
            if (nightMode) {
                darkSpriteAnimation.getFrame()
            } else {
                if (shotState) shotSpriteAnimation.getFrame() else spriteAnimation.getFrame()
            },
            matrix,
            paint
        )
    }

    fun setMovementState(movement: Movement) {
        this.movement = movement
    }

    fun setShotState() {
        shotState = true
    }

    fun setSpeedBonus(bonusType: BonusType) {
        speedBonusType = bonusType
        if (bonusType == BonusType.SPEED_DOWN) {
            speedVertical = SPEED_VERTICAL_DEFAULT - SPEED_VERTICAL_STEP
        } else if (bonusType == BonusType.SPEED_UP) {
            speedVertical = SPEED_VERTICAL_DEFAULT + SPEED_VERTICAL_STEP
        }
    }

    fun getY(): Float {
        return position.top
    }

}