package ru.tzhack.facegame.bird.gameobj

import android.content.Context
import android.graphics.*
import ru.tzhack.facegame.R
import ru.tzhack.facegame.bird.Viewport
import ru.tzhack.facegame.bird.utils.Position
import kotlin.random.Random


class Block(
    private val posLeftTube: Position,
    private val posRightTube: Position
) {

    private var crashedLeft = false
    private var crashedRight = false

    companion object {
        private lateinit var bitmap: Bitmap

        private const val drawOutline = false

        private const val K_SPRITE = 1.5f
        private const val HEIGHT_SPRITE = 276f / K_SPRITE
        private const val WIDTH_SPRITE = 828f / K_SPRITE
        private const val OVERLAY_SPRITE = 120f / K_SPRITE
        private const val COLLISION_ALLOWED_SPRITE = 75f / K_SPRITE

        //генерация
        private const val minWidth = 100f
        private const val rangeRandomWidth = 600
        private const val spaceSize = 300f
        const val wallsSpacing = 300

        private const val startY = 1000f

        fun generate(context: Context, screenX: Float, size: Int): List<Block> {
            bitmap = Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(
                    context.resources,
                    R.drawable.pad1
                ),
                WIDTH_SPRITE.toInt(),
                HEIGHT_SPRITE.toInt(),
                false
            )

            val walls = ArrayList<Block>()
            var y = startY
            for (i in 0 until size) {
                val leftPos = 0f
                val leftWidth = minWidth + Random.nextInt(rangeRandomWidth)

                val rightPos = leftPos + leftWidth + spaceSize
                val rightWidth = screenX - rightPos

                walls += Block(
                    posLeftTube = Position(
                        leftPos,
                        y,
                        leftWidth,
                        HEIGHT_SPRITE
                    ),
                    posRightTube = Position(
                        rightPos,
                        y,
                        rightWidth,
                        HEIGHT_SPRITE
                    )
                )

                y += wallsSpacing + HEIGHT_SPRITE
            }

            return walls
        }
    }

    fun isInFront(position: Position): Boolean {
        if (posLeftTube.containsY(position, 0f) ||
            posRightTube.containsY(position, 0f)
        ) {
            return true
        }

        return false
    }

    fun collision(position: Position): Boolean {
        if ((!crashedLeft && posLeftTube.contains(
                position,
                COLLISION_ALLOWED_SPRITE
            )) ||
            (!crashedRight && posRightTube.contains(
                position,
                COLLISION_ALLOWED_SPRITE
            ))
        ) {
            return true
        }

        return false
    }

    fun checkCrashed(position: Position): Boolean {
        if (!crashedLeft && posLeftTube.contains(
                position,
                COLLISION_ALLOWED_SPRITE
            )) {
            crashedLeft = true
            return true
        }
        if (!crashedRight && posRightTube.contains(
                position,
                COLLISION_ALLOWED_SPRITE
            )) {
            crashedRight = true
            return true
        }

        return false
    }

    fun draw(canvas: Canvas, paint: Paint, viewport: Viewport) {
        if (!viewport.nowOnScreen(posLeftTube)) {
            return
        }

        val widthSpriteWithOverlay = WIDTH_SPRITE - OVERLAY_SPRITE

        if (!crashedLeft) {
            var x = posLeftTube.right - WIDTH_SPRITE
            while (x > posLeftTube.left - widthSpriteWithOverlay) {
                canvas.drawBitmap(
                    bitmap,
                    x,
                    viewport.worldToScreenPoint(posLeftTube.top),
                    paint
                )
                x -= widthSpriteWithOverlay
            }
        }

        if (!crashedRight) {
            var x = posRightTube.right - posRightTube.width % widthSpriteWithOverlay
            while (x >= posRightTube.left) {
                canvas.drawBitmap(
                    bitmap,
                    x,
                    viewport.worldToScreenPoint(posRightTube.top),
                    paint
                )
                x -= widthSpriteWithOverlay
            }
        }

        if (drawOutline) {
            paint.style = Paint.Style.STROKE
            paint.color = Color.BLACK
            if (!crashedLeft) {
                canvas.drawRect(
                    posLeftTube.left,
                    viewport.worldToScreenPoint(posLeftTube.top - COLLISION_ALLOWED_SPRITE),
                    posLeftTube.right,
                    viewport.worldToScreenPoint(posLeftTube.bottom + COLLISION_ALLOWED_SPRITE),
                    paint
                )
            }

            if (!crashedRight) {
                canvas.drawRect(
                    posRightTube.left,
                    viewport.worldToScreenPoint(posRightTube.top - COLLISION_ALLOWED_SPRITE),
                    posRightTube.right,
                    viewport.worldToScreenPoint(posRightTube.bottom + COLLISION_ALLOWED_SPRITE),
                    paint
                )
            }

        }
    }

    fun getTop(): Float {
        return posLeftTube.top
    }
}