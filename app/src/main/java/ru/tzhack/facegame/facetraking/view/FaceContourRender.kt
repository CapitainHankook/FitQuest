package ru.tzhack.facegame.facetraking.view

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.google.firebase.ml.vision.common.FirebaseVisionPoint
import com.otaliastudios.cameraview.size.Size

class FaceContourRender @JvmOverloads constructor(
        context: Context,
        private val attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val dotSize = 3f

    private val paintWhite = Paint().apply {
        color = ContextCompat.getColor(context, android.R.color.white)
    }

    private val paintBox = Paint().apply {
        color = ContextCompat.getColor(context, android.R.color.holo_blue_dark)
        style = Paint.Style.STROKE
    }

    private val faceContour = mutableListOf<FirebaseVisionPoint>()

    private var rect = Rect()

    private var widthScaleFactor = 1.0F
    private var heightScaleFactor = 1.0F

    fun updateContour(
            frameSize: Size,
            faceRect: Rect?,
            points: List<FirebaseVisionPoint>
    ) {
        frameSize.let {
            widthScaleFactor = width.toFloat() / it.width.toFloat()
            heightScaleFactor = height.toFloat() / it.height.toFloat()
        }

        faceRect?.let {
            rect = it.apply {
                set(left.translateX(), top.translateY(), right.translateX(), bottom.translateY())
            }
        }

        faceContour.clear()
        faceContour.addAll(points)

        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (this.width != 0 && this.height != 0) {
            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)

            faceContour.forEach {
                canvas.drawCircle(
                        it.x.translateX(),
                        it.y.translateY(),
                        dotSize,
                        paintWhite
                )
            }

            canvas.drawRect(rect, paintBox)
        }
    }

    private fun Float.translateX(): Float = width - scaleX()
    private fun Float.scaleX(): Float = this * widthScaleFactor
    private fun Float.translateY(): Float = this * heightScaleFactor

    private fun Int.translateX(): Int = width - scaleX()
    private fun Int.scaleX(): Int = (this * widthScaleFactor).toInt()
    private fun Int.translateY(): Int = (this * heightScaleFactor).toInt()
}