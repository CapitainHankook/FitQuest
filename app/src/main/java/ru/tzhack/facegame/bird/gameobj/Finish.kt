package ru.tzhack.facegame.bird.gameobj

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import ru.tzhack.facegame.R
import ru.tzhack.facegame.bird.Viewport
import ru.tzhack.facegame.bird.utils.Position

private const val sideSprite = 300f / 4f

class Finish(positionY: Float, width: Float, context: Context) {

    val position = Position(
        left = 0f,
        top = positionY,
        width = width,
        height = sideSprite
    )

    private val bitmap: Bitmap = Bitmap.createScaledBitmap(
        BitmapFactory.decodeResource(
            context.resources,
            R.drawable.finish
        ),
        sideSprite.toInt(),
        sideSprite.toInt(),
        false
    )

    fun onDraw(canvas: Canvas, paint: Paint, viewport: Viewport) {
        if (!viewport.nowOnScreen(position)) {
            return
        }

        var x = position.left
        while (x < position.right) {
            canvas.drawBitmap(
                bitmap,
                x,
                viewport.worldToScreenPoint(position.top),
                paint
            )
            x += sideSprite
        }
    }
}