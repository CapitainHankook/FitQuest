package ru.tzhack.facegame.facetraking.util

import android.util.Log
import com.google.firebase.ml.vision.face.FirebaseVisionFace
import com.google.firebase.ml.vision.face.FirebaseVisionFaceContour.*

private const val correctSmileProbabilityPercent = 0.75F
private const val correctCloseEyeProbabilityPercent = 0.20F
private const val correctMouthOpenDelta = 30F

private const val correctHeadLeftRotateDelta = 70F
private const val correctHeadRightRotateDelta = 70F

private const val correctHeadBiasDownDelta = 7F
private const val correctHeadBiasUpDelta = 40F

private const val correctEyeBrownMoveDelta = -17F

// For Help:
// https://firebase.google.com/docs/ml-kit/images/examples/face_contours.svg

/**
 * Метод для проверки открытого рта на лице игрока.
 * */
fun FirebaseVisionFace.checkOpenMouthOnFaceAvailable(): Boolean {
    val mouthCenterPointIndex = 4

    val upperLitBottom = getContour(UPPER_LIP_BOTTOM).points[mouthCenterPointIndex].y
    val lowerLitTop = getContour(LOWER_LIP_TOP).points[mouthCenterPointIndex].y

    val resultDelta = lowerLitTop - upperLitBottom

    val mouthLeft = getContour(UPPER_LIP_TOP).points[0].x
    val mouthRight = getContour(UPPER_LIP_TOP).points[10].x

    val scaleZoom = mouthRight / mouthLeft

    return resultDelta > correctMouthOpenDelta * scaleZoom
}

/**
 * Метод для проверки поворота головы игрока налево.
 * */
fun FirebaseVisionFace.checkHeadLeftRotateAvailable(): Boolean {
    val noseBridgeCenterPointIndex = 1
    val noseCenter = getContour(NOSE_BRIDGE).points[noseBridgeCenterPointIndex].x

    val faceCenterLeftPointIndex = 9
    val faceLeft = getContour(FACE).points[faceCenterLeftPointIndex].x

    val resultDelta = faceLeft - noseCenter
    return resultDelta < correctHeadLeftRotateDelta
}

/**
 * Метод для проверки поворота головы игрока направо.
 * */
fun FirebaseVisionFace.checkHeadRightRotateAvailable(): Boolean {
    val noseBridgeCenterPointIndex = 1
    val noseCenter = getContour(NOSE_BRIDGE).points[noseBridgeCenterPointIndex].x

    val faceCenterRightPointIndex = 27
    val faceRight = getContour(FACE).points[faceCenterRightPointIndex].x

    val resultDelta = noseCenter - faceRight
    return resultDelta < correctHeadRightRotateDelta
}

/**
 * Метод для проверки наклона головы игрока вперед.
 * */
fun FirebaseVisionFace.checkHeadBiasDownAvailable(): Boolean {
    val noseBridgeCenterPointIndex = 1
    val noseBridgeCenter = getContour(NOSE_BRIDGE).points[noseBridgeCenterPointIndex].y

    val noseBottomCenterPointIndex = 1
    val noseBottomCenter = getContour(NOSE_BOTTOM).points[noseBottomCenterPointIndex].y

    val resultDelta = noseBottomCenter - noseBridgeCenter

    val noseBottomLeft = getContour(NOSE_BOTTOM).points[0].x
    val noseBottomRight = getContour(NOSE_BOTTOM).points[2].x

    val scaleZoom = noseBottomRight / noseBottomLeft

    return resultDelta < correctHeadBiasDownDelta * scaleZoom
}

/**
 * Метод для проверки наклона головы игрока назад.
 * */
fun FirebaseVisionFace.checkHeadBiasUpAvailable(): Boolean {
    val noseBridgeCenterPointIndex = 1
    val noseBridgeCenter = getContour(NOSE_BRIDGE).points[noseBridgeCenterPointIndex].y

    val noseBridgeTopPointIndex = 0
    val noseBridgeTop = getContour(NOSE_BRIDGE).points[noseBridgeTopPointIndex].y

    val resultDelta = noseBridgeCenter - noseBridgeTop

    val noseBottomLeft = getContour(NOSE_BOTTOM).points[0].x
    val noseBottomRight = getContour(NOSE_BOTTOM).points[2].x

    val scaleZoom = noseBottomRight / noseBottomLeft

    return resultDelta < correctHeadBiasUpDelta * scaleZoom
}

/**
 * Метод для проверки наличия улыбки на лице игрока.
 * */
fun FirebaseVisionFace.checkSmileOnFaceAvailable(): Boolean =
    smilingProbability > correctSmileProbabilityPercent

/**
 * Метод для проверки подмигивания правым глазом.
 * */
fun FirebaseVisionFace.checkRightEyeCloseOnFaceAvailable(): Boolean =
    leftEyeOpenProbability < correctCloseEyeProbabilityPercent

/**
 * Метод для проверки подмигивания левым глазом.
 * */
fun FirebaseVisionFace.checkLeftEyeCloseOnFaceAvailable(): Boolean =
    rightEyeOpenProbability < correctCloseEyeProbabilityPercent

/**
 * Метод для проверки подмигивания обоими глазами.
 *
 * (Необходимо исключить моргания)
 * */
private var doubleCloseCount = 0

fun FirebaseVisionFace.checkDoubleEyeCloseOnFaceAvailable(): Boolean {
    val doubleClose = checkRightEyeCloseOnFaceAvailable() && checkLeftEyeCloseOnFaceAvailable()
    return if (doubleClose && ++doubleCloseCount > 5) {
        doubleCloseCount = 0
        true
    } else false
}

/**
 * Метод для проверки движения обеих бровей на лице игрока.
 * TODO:
 * */

fun FirebaseVisionFace.checkDoubleEyeBrownMoveOnFaceAvailable(): Boolean {
    val rightEyeBrownCenterPointIndex = 2
    val rightEyeBrownCenter = getContour(RIGHT_EYEBROW_TOP).points[rightEyeBrownCenterPointIndex].y

    val faceCenterRightPointIndex = 2
    val faceRight = getContour(FACE).points[faceCenterRightPointIndex].y

    val rightEyeDelta = rightEyeBrownCenter - faceRight
    Log.e("TAG", "rightEyeDelta: ${rightEyeDelta}")

    val rightEyeBrownTopStart = getContour(RIGHT_EYEBROW_BOTTOM).points[0].x
    val rightEyeBrownTopEnd = getContour(RIGHT_EYEBROW_BOTTOM).points[3].x
    val scaleZoomRight = rightEyeBrownTopEnd - rightEyeBrownTopStart
    //    Log.e("TAG", "ScaleRes: ${correctEyeBrownMoveDelta + scaleZoom}")

    // Защита от движения обеими бровями
//    val leftEyeBrownTop = getContour(LEFT_EYEBROW_TOP).points[2].y
//    val faceLeft = getContour(FACE).points[3].y
//    val scaleZoom = leftEyeBrownTop - faceLeft


    //Левый
    val leftEyeBrownCenterPointIndex = 2
    val leftEyeBrownCenter = getContour(LEFT_EYEBROW_TOP).points[leftEyeBrownCenterPointIndex].y

    val faceCenterLeftPointIndex = 34
    val faceLeft = getContour(FACE).points[faceCenterLeftPointIndex].y

    val leftEyeDelta = leftEyeBrownCenter - faceLeft
    Log.e("TAG", "leftEyeDelta: ${leftEyeDelta}")

    val leftEyeBrownTopStart = getContour(LEFT_EYEBROW_BOTTOM).points[0].x
    val leftEyeBrownTopEnd = getContour(LEFT_EYEBROW_BOTTOM).points[3].x
    val scaleZoomLeft = leftEyeBrownTopEnd - leftEyeBrownTopStart



//    Log.e("TAG", "ScaleRes: ${correctEyeBrownMoveDelta + scaleZoom}")
    return true
}