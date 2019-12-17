package ru.tzhack.facegame.facetraking.mlkit

import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.face.FirebaseVisionFace
import com.google.firebase.ml.vision.face.FirebaseVisionFaceContour
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions
import com.otaliastudios.cameraview.frame.Frame
import ru.tzhack.facegame.data.model.FaceEmoji.*
import ru.tzhack.facegame.data.model.FaceEmoji
import ru.tzhack.facegame.facetraking.mlkit.listener.MlKitDebugListener
import ru.tzhack.facegame.facetraking.mlkit.listener.MlKitEmojiListener
import ru.tzhack.facegame.facetraking.mlkit.listener.MlKitHeroListener
import ru.tzhack.facegame.facetraking.util.*
import java.util.concurrent.atomic.AtomicBoolean

const val maxHeadZ = 25F
const val minHeadZ = maxHeadZ * -1

object MlKitEngine {

    private var faceDetector: FirebaseVisionFaceDetector? = null

    private var analyzing = AtomicBoolean(false)

    fun initMlKit() {
        // face classification and landmark detection
        val options = FirebaseVisionFaceDetectorOptions.Builder()
            .setPerformanceMode(FirebaseVisionFaceDetectorOptions.FAST)
            .setContourMode(FirebaseVisionFaceDetectorOptions.ALL_CONTOURS)
            .setClassificationMode(FirebaseVisionFaceDetectorOptions.ALL_CLASSIFICATIONS)
            .setMinFaceSize(0.9F)
            .build()

        faceDetector = FirebaseVision.getInstance().getVisionFaceDetector(options)
    }

    fun extractDataFromFrame(
        frame: Frame,
        currentEmoji: FaceEmoji? = null,
        listenerHero: MlKitHeroListener? = null,
        listenerEmoji: MlKitEmojiListener? = null,
        debugListener: MlKitDebugListener? = null
    ) {
        if (analyzing.get()) return
        analyzing.set(true)

        val frameSize = frame.size
        getFaceDetector().detectInImage(frame.getVisionImageFromFrame())
            .addOnSuccessListener { faces ->
                if (faces.isNotEmpty()) {
                    // Работаем только с одним лицом
                    val face = faces.first()

                    // Защита от ситуации, когда точки не были получены
                    if (face.getContour(FirebaseVisionFaceContour.ALL_POINTS).points.isNotEmpty()) {
                        if (currentEmoji == null) listenerHero?.let { calculateHeroActions(face, it) }
                        else listenerEmoji?.let { calculateEmojiActions(face, currentEmoji, it) }

                        //Debug Info
                        debugListener?.onDebugInfo(frameSize, face)
                    }
                }

                analyzing.set(false)
            }
            .addOnFailureListener {
                listenerHero?.onError(it)

                analyzing.set(false)
            }
    }

    private fun calculateHeroActions(face: FirebaseVisionFace, listener: MlKitHeroListener) {
        listener.onHeroHorizontalAnim(face.headEulerAngleZ)

        if (face.checkSmileOnFaceAvailable()) listener.onHeroSuperPowerAnim()
        if (face.checkRightEyeCloseOnFaceAvailable()) listener.onHeroRightEyeAnim()
        if (face.checkLeftEyeCloseOnFaceAvailable()) listener.onHeroLeftEyeAnim()

        if (face.checkDoubleEyeCloseOnFaceAvailable()) listener.onHeroDoubleEyeAnim()

        if (face.checkOpenMouthOnFaceAvailable()) listener.onHeroMouthOpenAnim()
    }

    private fun calculateEmojiActions(
        face: FirebaseVisionFace,
        currentEmoji: FaceEmoji,
        listener: MlKitEmojiListener
    ) {
        when (currentEmoji) {
            // Зажмуривание
            DOUBLE_EYE_CLOSE -> if (face.checkDoubleEyeCloseOnFaceAvailable()) listener.onEmojiObtained(
                DOUBLE_EYE_CLOSE
            )

            // Левый глаз закрыт
            LEFT_EYE_CLOSE -> if (face.checkLeftEyeCloseOnFaceAvailable()) listener.onEmojiObtained(LEFT_EYE_CLOSE)

            //Правый глаз закрыт
            RIGHT_EYE_CLOSE -> if (face.checkRightEyeCloseOnFaceAvailable()) listener.onEmojiObtained(
                RIGHT_EYE_CLOSE
            )

            // Движения обеими бровями
            DOUBLE_EYEBROWN_MOVE -> if (face.checkDoubleEyeBrownMoveOnFaceAvailable()) listener.onEmojiObtained(
                DOUBLE_EYEBROWN_MOVE
            )

            // Улыбка
            SMILE -> if (face.checkSmileOnFaceAvailable()) listener.onEmojiObtained(SMILE)

            // Открыт рот
            MOUTH_OPEN -> if (face.checkOpenMouthOnFaceAvailable()) listener.onEmojiObtained(MOUTH_OPEN)

            // Повороты головы влево
            HEAD_ROTATE_LEFT -> if (face.checkHeadLeftRotateAvailable()) listener.onEmojiObtained(HEAD_ROTATE_LEFT)

            // Повороты головы вправо
            HEAD_ROTATE_RIGHT -> if (face.checkHeadRightRotateAvailable()) listener.onEmojiObtained(HEAD_ROTATE_RIGHT)

            // Повороты головы вперед
            HEAD_BIAS_DOWN -> if (face.checkHeadBiasDownAvailable()) listener.onEmojiObtained(HEAD_BIAS_DOWN)

            // Повороты головы назад
            HEAD_BIAS_UP -> if (face.checkHeadBiasUpAvailable()) listener.onEmojiObtained(HEAD_BIAS_UP)

            // Наклон головы влево
            HEAD_BIAS_LEFT -> if (face.headEulerAngleZ <= minHeadZ) listener.onEmojiObtained(HEAD_BIAS_LEFT)

            // Наклон головы вправо
            HEAD_BIAS_RIGHT -> if (face.headEulerAngleZ >= maxHeadZ) listener.onEmojiObtained(HEAD_BIAS_RIGHT)
        }
    }

    private fun getFaceDetector(): FirebaseVisionFaceDetector = faceDetector
        ?: throw Exception("MlKit is not configured! Call first 'initMlKit()' method.")
}