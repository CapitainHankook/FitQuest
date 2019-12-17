package ru.tzhack.facegame.facetraking.mlkit.listener

import com.google.firebase.ml.vision.face.FirebaseVisionFace
import com.otaliastudios.cameraview.size.Size

interface MlKitDebugListener {

    /**
     * Метод для ведения отладочных работ с MlKit.
     *
     * @param frameSize размер кадра.
     * @param face полная модель пользователя.
     * */
    fun onDebugInfo(frameSize: Size, face: FirebaseVisionFace?)
}