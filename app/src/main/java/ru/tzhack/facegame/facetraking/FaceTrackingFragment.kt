package ru.tzhack.facegame.facetraking

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.firebase.ml.vision.face.FirebaseVisionFace
import com.google.firebase.ml.vision.face.FirebaseVisionFaceContour
import com.otaliastudios.cameraview.size.Size
import ru.tzhack.facegame.R
import ru.tzhack.facegame.data.model.FaceEmoji
import ru.tzhack.facegame.databinding.FragmentFaceTrackingBinding
import ru.tzhack.facegame.facetraking.mlkit.MlKitEngine
import ru.tzhack.facegame.facetraking.mlkit.listener.MlKitDebugListener
import ru.tzhack.facegame.facetraking.mlkit.listener.MlKitEmojiListener
import ru.tzhack.facegame.facetraking.util.fadeInOutAnim
import ru.tzhack.facegame.facetraking.util.fadeOutInAnim
import kotlin.random.Random

interface FaceGameOverListener {
    fun onFaceGameOverPositive()
}

class FaceTrackingFragment : Fragment() {

    companion object {
        val TAG: String = FaceTrackingFragment::class.java.simpleName

        fun createFragment() =
            FaceTrackingFragment().apply {
                arguments = Bundle().apply { }
            }
    }

    private var gameOverListener: FaceGameOverListener? = null

    private lateinit var binding: FragmentFaceTrackingBinding

    private var currentEmoji: FaceEmoji? = null

    private val emojiForWin = 20
    private var correctEmojiCount = 0

    private val emojiList = listOf(
        FaceEmoji.DOUBLE_EYE_CLOSE,
        FaceEmoji.LEFT_EYE_CLOSE,
        FaceEmoji.RIGHT_EYE_CLOSE,

//        FaceEmoji.DOUBLE_EYEBROWN_MOVE,

        FaceEmoji.SMILE,
        FaceEmoji.MOUTH_OPEN,

        FaceEmoji.HEAD_BIAS_LEFT,
        FaceEmoji.HEAD_BIAS_RIGHT,

        FaceEmoji.HEAD_BIAS_DOWN,
        FaceEmoji.HEAD_BIAS_UP,

        FaceEmoji.HEAD_ROTATE_LEFT,
        FaceEmoji.HEAD_ROTATE_RIGHT
    )

    private var lockEmojiProcess = false

    private val mlKitEmojiListener = object : MlKitEmojiListener {
        override fun onEmojiObtained(emoji: FaceEmoji) {
            if (!lockEmojiProcess && currentEmoji == emoji) doneEmoji()
        }
    }

    private val mlKitDebugListener = object : MlKitDebugListener {
        override fun onDebugInfo(frameSize: Size, face: FirebaseVisionFace?) {
            face?.let { printContourOnFace(frameSize, it) }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_face_tracking, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = DataBindingUtil.bind(view)
            ?: throw IllegalStateException("ViewDataBinding is null for ${FaceTrackingFragment::class.java.canonicalName}")

        updateEmojiOnScreen()

        var first = true
        binding.cameraView.run {
            setLifecycleOwner(this@FaceTrackingFragment)
            addFrameProcessor { frame ->

                if (first && frame.size.height != 0 && frame.size.width != 0) {
                    binding.faceOverlayView.run {
                        layoutParams = layoutParams.apply {
                            width = frame.size.height
                            height = frame.size.width
                        }
                        requestLayout()
                    }
                    first = false
                }

                MlKitEngine.extractDataFromFrame(
                    frame = frame,
                    currentEmoji = currentEmoji,
                    listenerEmoji = mlKitEmojiListener,
                    debugListener = mlKitDebugListener
                )
            }
        }

    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        when (context) {
            is FaceGameOverListener -> gameOverListener = context
        }
    }

    private fun doneEmoji() {
        lockEmojiProcess()

        correctEmojiCount++

        if (isEndGame()) showWinDialog()
        else {
            binding.doneOverlay.fadeInOutAnim { updateEmojiOnScreen() }
            binding.txtEmojiDescription.fadeOutInAnim()
        }
    }

    private fun showWinDialog() {
        AlertDialog.Builder(requireContext())
            .setCancelable(false)
            .setTitle("Молодец!")
            .setMessage("Ты хорошо поработал, теперь можно и поиграть. Ты готов?")
            .setPositiveButton(
                "Конечно"
            ) { _, _ -> gameOverListener?.onFaceGameOverPositive() }
            .create()
            .show()
    }

    private fun updateEmojiOnScreen() {
        var newEmoji = randNextEmoji()
        while (newEmoji == currentEmoji) {
            newEmoji = randNextEmoji()
        }

        currentEmoji = newEmoji
        currentEmoji?.let {
            binding.txtEmojiDescription.setText(it.resDescription)
            Glide.with(binding.emojiAnim)
                .asGif()
                .centerCrop()
                .load(it.resAnim)
                .into(binding.emojiAnim)
        }

        unlockEmojiProcess()
    }

    private fun lockEmojiProcess() {
        lockEmojiProcess = true
    }

    private fun unlockEmojiProcess() {
        lockEmojiProcess = false
    }

    private fun randNextEmoji(): FaceEmoji = emojiList[Random.Default.nextInt(emojiList.size)]

    private fun printContourOnFace(frameSize: Size, face: FirebaseVisionFace) {
        val invertFrameSize = Size(frameSize.height, frameSize.width)
        binding.faceOverlayView.updateContour(
            invertFrameSize,
            face.boundingBox,
            face.getContour(FirebaseVisionFaceContour.ALL_POINTS).points
        )
    }

    private fun isEndGame() = correctEmojiCount == emojiForWin
}