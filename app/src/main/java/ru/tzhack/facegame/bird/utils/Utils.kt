package ru.tzhack.facegame.bird.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.annotation.DrawableRes

fun Context.createBitmap(@DrawableRes resId: Int, width: Float, height: Float): Bitmap {
    return Bitmap.createScaledBitmap(
        BitmapFactory.decodeResource(
            resources,
            resId
        ),
        width.toInt(),
        height.toInt(),
        false
    )
}

fun Context.createBitmaps(width: Float, height: Float, vararg resId: Int): Array<Bitmap> {
    return Array(resId.size) { i ->
        Bitmap.createScaledBitmap(
            BitmapFactory.decodeResource(
                resources,
                resId[i]
            ),
            width.toInt(),
            height.toInt(),
            false
        )
    }
}