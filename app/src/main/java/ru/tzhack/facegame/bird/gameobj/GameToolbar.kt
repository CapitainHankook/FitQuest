package ru.tzhack.facegame.bird.gameobj

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import ru.tzhack.facegame.R
import ru.tzhack.facegame.bird.utils.createBitmap


class GameToolbar(context: Context, widthScreen : Float) {

    private val sideSprite = 70f
    var time = 60f
    var countShots = 3

    private val imageShots = context.createBitmap(R.drawable.bonus_shot, sideSprite, sideSprite)

    fun update(dt: Float) {
        time -= dt
    }

    companion object {
        private const val yTopIndent = 55f
    }

    private val startPositionTime = widthScreen - 285f

    private val startPositionShotImg = widthScreen - 140f

    private val startPositionCountShot = widthScreen - 45f

    fun draw(canvas: Canvas, paint: Paint) {
        paint.color = Color.BLACK
        paint.style = Paint.Style.FILL
        paint.textSize = 50f

        canvas.drawText(timeToText(), startPositionTime,  yTopIndent, paint)
        canvas.drawBitmap(imageShots, startPositionShotImg,  0f, paint)
        canvas.drawText(countShots.toString(), startPositionCountShot,  yTopIndent, paint)
    }

    private fun timeToText() : String {
        val second = time.toInt() % 60
        val secondStr : String = if(second < 10) "0" + second.toString() else second.toString()
        return "0" + (time.toInt() / 60).toString() + ":" + secondStr
    }

    fun addTime() {
        time += 5
    }
}