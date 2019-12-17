package ru.tzhack.facegame.facetraking.util

import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata
import com.otaliastudios.cameraview.frame.Frame

fun Frame.getVisionImageFromFrame(): FirebaseVisionImage {
    val frameRotation = when (rotation) {
        in (0 until 90)    -> FirebaseVisionImageMetadata.ROTATION_0
        in (90 until 180)  -> FirebaseVisionImageMetadata.ROTATION_90
        in (180 until 270) -> FirebaseVisionImageMetadata.ROTATION_180
        in (270 until 360) -> FirebaseVisionImageMetadata.ROTATION_270
        else               -> FirebaseVisionImageMetadata.ROTATION_0
    }

    //Metadata that gives more information on the image that is to be converted to FirebaseVisionImage
    val imageMetaData = FirebaseVisionImageMetadata.Builder()
        .setWidth(size.width)
        .setHeight(size.height)
        .setFormat(FirebaseVisionImageMetadata.IMAGE_FORMAT_NV21)
        .setRotation(frameRotation)
        .build()

    return FirebaseVisionImage.fromByteArray(data, imageMetaData)
}