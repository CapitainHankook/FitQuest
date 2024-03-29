///*
// * Copyright 2019 Google LLC
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *     https://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
package ru.tzhack.facegame.facetraking.view
//
//import android.content.Context
//import android.graphics.Matrix
//import android.graphics.PointF
//import android.hardware.display.DisplayManager
//import android.os.Handler
//import android.os.HandlerThread
//import android.os.Looper
//import android.util.Log
//import android.util.Rational
//import android.util.Size
//import android.view.*
//import androidx.camera.core.*
//import com.google.android.gms.tasks.OnFailureListener
//import com.google.android.gms.tasks.OnSuccessListener
//import com.google.firebase.ml.vision.FirebaseVision
//import com.google.firebase.ml.vision.common.FirebaseVisionImage
//import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata
//import com.google.firebase.ml.vision.face.FirebaseVisionFace
//import com.google.firebase.ml.vision.face.FirebaseVisionFaceContour
//import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector
//import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions
//import ru.tzhack.facegame.facetraking.mlkit.MlKitEngine
//import ru.tzhack.facegame.facetraking.mlkit.listener.MlKitEmojiListener
//import ru.tzhack.facegame.facetraking.mlkit.listener.MlKitHeroListener
//import java.lang.ref.WeakReference
//import java.util.*
//import java.util.concurrent.atomic.AtomicBoolean
//
///**
// * Builder for [Preview] that takes in a [WeakReference] of the view finder and
// * [PreviewConfig], then instantiates a [Preview] which automatically
// * resizes and rotates reacting to config changes.
// */
//class AutoFitPreviewAnalysis private constructor(
//    previewConfig: PreviewConfig,
//    analysisConfig: ImageAnalysisConfig,
//    viewFinderRef: WeakReference<TextureView>,
//    overlayRef: WeakReference<FaceContourRender>,
//    private val mlKitHeroListener: MlKitHeroListener? = null,
//    private val mlKitEmojiListener: MlKitEmojiListener? = null
//) {
//    /** Public instance of preview use-case which can be used by consumers of this adapter */
//    val previewUseCase: Preview
//    /** Public instance of analysis use-case which can be used by consumers of this adapter */
//    val analysisUseCase: ImageAnalysis
//
//    /** Internal variable used to keep track of the use-case's output rotation */
//    private var bufferRotation: Int = 0
//    /** Internal variable used to keep track of the view's rotation */
//    private var viewFinderRotation: Int? = null
//    /** Internal variable used to keep track of the use-case's output dimension */
//    private var bufferDimens: Size = Size(0, 0)
//    /** Internal variable used to keep track of the view's dimension */
//    private var viewFinderDimens: Size = Size(0, 0)
//    /** Internal variable used to keep track of the view's display */
//    private var viewFinderDisplay: Int = -1
//    /** Internal variable used to keep track of the image analysis dimension */
//    private var cachedAnalysisDimens = Size(0, 0)
//    /** Internal variable used to keep track of the calculated dimension of the preview image */
//    private var cachedTargetDimens = Size(0, 0)
//
//    /** Internal reference of the [DisplayManager] */
//    private lateinit var displayManager: DisplayManager
//    /**
//     * We need a display listener for orientation changes that do not trigger a configuration
//     * change, for example if we choose to override config change in manifest or for 180-degree
//     * orientation changes.
//     */
//    private val displayListener = object : DisplayManager.DisplayListener {
//        override fun onDisplayAdded(displayId: Int) = Unit
//        override fun onDisplayRemoved(displayId: Int) = Unit
//        override fun onDisplayChanged(displayId: Int) {
//            val viewFinder = viewFinderRef.get() ?: return
//            if (displayId == viewFinderDisplay) {
//                val display = displayManager.getDisplay(displayId)
//                val rotation = getDisplaySurfaceRotation(display)
//                updateTransform(viewFinder, rotation, bufferDimens, viewFinderDimens)
//                updateOverlayTransform(overlayRef.get(), cachedAnalysisDimens)
//            }
//        }
//    }
//
//    init {
//        // Make sure that the view finder reference is valid
//        val viewFinder = viewFinderRef.get() ?: throw IllegalArgumentException("Invalid reference to view finder used")
//
//        // Initialize the display and rotation from texture view information
//        viewFinderDisplay = viewFinder.display.displayId
//        viewFinderRotation = getDisplaySurfaceRotation(viewFinder.display) ?: 0
//
//        // Initialize public use-cases with the given config
//        previewUseCase = Preview(previewConfig)
//        analysisUseCase = ImageAnalysis(analysisConfig).apply {
//            analyzer = FaceAnalyzer(mlKitHeroListener, mlKitEmojiListener).apply {
//                pointsListListener = { face, points ->
//                    overlayRef.get()?.points = points
//                }
//                analysisSizeListener = {
//                    updateOverlayTransform(overlayRef.get(), it)
//                }
//            }
//        }
//
//        // Every time the view finder is updated, recompute layout
//        previewUseCase.onPreviewOutputUpdateListener = Preview.OnPreviewOutputUpdateListener {
//            val viewFinder =
//                viewFinderRef.get() ?: return@OnPreviewOutputUpdateListener
//
//            // To update the SurfaceTexture, we have to remove it and re-add it
//            val parent = viewFinder.parent as ViewGroup
//            parent.removeView(viewFinder)
//            parent.addView(viewFinder, 0)
//
//            viewFinder.surfaceTexture = it.surfaceTexture
//            bufferRotation = it.rotationDegrees
//            val rotation = getDisplaySurfaceRotation(viewFinder.display)
//            updateTransform(viewFinder, rotation, it.textureSize, viewFinderDimens)
//            updateOverlayTransform(overlayRef.get(), cachedAnalysisDimens)
//        }
//
//        // Every time the provided texture view changes, recompute layout
//        viewFinder.addOnLayoutChangeListener { view, left, top, right, bottom, _, _, _, _ ->
//            val viewFinder = view as TextureView
//            val newViewFinderDimens = Size(right - left, bottom - top)
//            val rotation = getDisplaySurfaceRotation(viewFinder.display)
//            updateTransform(viewFinder, rotation, bufferDimens, newViewFinderDimens)
//            updateOverlayTransform(overlayRef.get(), cachedAnalysisDimens)
//        }
//
//        // Every time the orientation of device changes, recompute layout
//        displayManager = viewFinder.context
//            .getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
//        displayManager.registerDisplayListener(displayListener, null)
//
//        // Remove the display listeners when the view is detached to avoid
//        // holding a reference to the View outside of a Fragment.
//        // NOTE: Even though using a weak reference should take care of this,
//        // we still try to avoid unnecessary calls to the listener this way.
//        viewFinder.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
//            override fun onViewAttachedToWindow(view: View?) {
//                displayManager.registerDisplayListener(displayListener, null)
//            }
//
//            override fun onViewDetachedFromWindow(view: View?) {
//                displayManager.unregisterDisplayListener(displayListener)
//            }
//
//        })
//    }
//
//    /** Helper function that fits a camera preview into the given [TextureView] */
//    private fun updateTransform(
//        textureView: TextureView?, rotation: Int?, newBufferDimens: Size,
//        newViewFinderDimens: Size
//    ) {
//        // This should not happen anyway, but now the linter knows
//        val textureView = textureView ?: return
//
//        if (rotation == viewFinderRotation &&
//            Objects.equals(newBufferDimens, bufferDimens) &&
//            Objects.equals(newViewFinderDimens, viewFinderDimens)) {
//            // Nothing has changed, no need to transform output again
//            return
//        }
//
//        if (rotation == null) {
//            // Invalid rotation - wait for valid inputs before setting matrix
//            return
//        } else {
//            // Update internal field with new inputs
//            viewFinderRotation = rotation
//        }
//
//        if (newBufferDimens.width == 0 || newBufferDimens.height == 0) {
//            // Invalid buffer dimens - wait for valid inputs before setting matrix
//            return
//        } else {
//            // Update internal field with new inputs
//            bufferDimens = newBufferDimens
//        }
//
//        if (newViewFinderDimens.width == 0 || newViewFinderDimens.height == 0) {
//            // Invalid view finder dimens - wait for valid inputs before setting matrix
//            return
//        } else {
//            // Update internal field with new inputs
//            viewFinderDimens = newViewFinderDimens
//        }
//
//        val matrix = Matrix()
//
//        // Compute the center of the view finder
//        val centerX = viewFinderDimens.width / 2f
//        val centerY = viewFinderDimens.height / 2f
//
//        // Correct preview output to account for display rotation
//        matrix.postRotate(-viewFinderRotation!!.toFloat(), centerX, centerY)
//
//        // Buffers are rotated relative to the device's 'natural' orientation: swap width and height
//        val bufferRatio = bufferDimens.height / bufferDimens.width.toFloat()
//
//        val scaledWidth: Int
//        val scaledHeight: Int
//        // Match longest sides together -- i.e. apply center-crop transformation
//        if (viewFinderDimens.width > viewFinderDimens.height) {
//            scaledHeight = viewFinderDimens.width
//            scaledWidth = Math.round(viewFinderDimens.width * bufferRatio)
//        } else {
//            scaledHeight = viewFinderDimens.height
//            scaledWidth = Math.round(viewFinderDimens.height * bufferRatio)
//        }
//
//        // save the scaled dimens for use with the overlay
//        cachedTargetDimens = Size(scaledWidth, scaledHeight)
//
//        // Compute the relative scale value
//        val xScale = scaledWidth / viewFinderDimens.width.toFloat()
//        val yScale = scaledHeight / viewFinderDimens.height.toFloat()
//
//        // Scale input buffers to fill the view finder
//        matrix.preScale(xScale, yScale, centerX, centerY)
//
//        // Finally, apply transformations to our TextureView
//        textureView.setTransform(matrix)
//    }
//
//    private fun updateOverlayTransform(overlayView: FaceContourRender?, size: Size) {
//        if (overlayView == null) return
//
//        if (size == cachedAnalysisDimens) {
//            // nothing has changed since the last update, so return early
//            return
//        } else {
//            cachedAnalysisDimens = size
//        }
//
//        Log.d("autofit", "cachedAnalysisDimens are now $cachedAnalysisDimens")
//        Log.d("autofit", "cachedTargetDimens are now $cachedTargetDimens")
//        Log.d("autofit", "viewFinderDimens are now $viewFinderDimens")
//
//        overlayView.transform = overlayMatrix()
//    }
//
//    private fun overlayMatrix(): Matrix {
//        val matrix = Matrix()
//
//        // ---- SCALE the overlay to match the preview ----
//        // Buffers are rotated relative to the device's 'natural' orientation: swap width and height
//        val scale = cachedTargetDimens.height.toFloat() / cachedAnalysisDimens.width.toFloat()
//
//        // Scale input buffers to fill the view finder
//        matrix.preScale(scale, scale)
//
//        // ---- MOVE the overlay ----
//        // move all the points of the overlay so that the relative (0,0) point is at the top-left of the preview
//        val xTranslate: Float
//        val yTranslate: Float
//        if (viewFinderDimens.width > viewFinderDimens.height) {
//            // portrait: viewFinder width corresponds to target height
//            xTranslate = (viewFinderDimens.width - cachedTargetDimens.height) / 2f
//            yTranslate = (viewFinderDimens.height - cachedTargetDimens.width) / 2f
//        } else {
//            // landscape: viewFinder width corresponds to target width
//            xTranslate = (viewFinderDimens.width - cachedTargetDimens.width) / 2f
//            yTranslate = (viewFinderDimens.height - cachedTargetDimens.height) / 2f
//        }
//        matrix.postTranslate(xTranslate, yTranslate)
//
//        // ---- MIRROR the overlay ----
//        // Compute the center of the view finder
//        val centerX = viewFinderDimens.width / 2f
//        val centerY = viewFinderDimens.height / 2f
//        matrix.postScale(-1f, 1f, centerX, centerY)
//
//        return matrix
//    }
//
//    companion object {
//        /** Helper function that gets the rotation of a [Display] in degrees */
//        fun getDisplaySurfaceRotation(display: Display?) = when (display?.rotation) {
//            Surface.ROTATION_0   -> 0
//            Surface.ROTATION_90  -> 90
//            Surface.ROTATION_180 -> 180
//            Surface.ROTATION_270 -> 270
//            else                 -> null
//        }
//
//        fun build(
//            screenSize: Size,
//            aspectRatio: Rational,
//            rotation: Int,
//            viewFinder: TextureView,
//            overlay: FaceContourRender,
//            mlKitHeroListener: MlKitHeroListener? = null,
//            mlKitEmojiListener: MlKitEmojiListener? = null
//        ): AutoFitPreviewAnalysis {
//            val previewConfig = createPreviewConfig(screenSize, aspectRatio, rotation)
//            val analysisConfig = createAnalysisConfig(screenSize, aspectRatio, rotation)
//            return AutoFitPreviewAnalysis(
//                previewConfig,
//                analysisConfig,
//                WeakReference(viewFinder),
//                WeakReference(overlay),
//                mlKitHeroListener,
//                mlKitEmojiListener
//            )
//        }
//
//        private fun createPreviewConfig(screenSize: Size, aspectRatio: Rational, rotation: Int): PreviewConfig {
//            return PreviewConfig.Builder().apply {
//                setLensFacing(CameraX.LensFacing.FRONT)
//                setTargetResolution(screenSize)
//                setTargetAspectRatio(aspectRatio)
//                setTargetRotation(rotation)
//            }.build()
//        }
//
//        private fun createAnalysisConfig(screenSize: Size, aspectRatio: Rational, rotation: Int): ImageAnalysisConfig {
//            return ImageAnalysisConfig.Builder().apply {
//                setLensFacing(CameraX.LensFacing.FRONT)
//                setImageReaderMode(ImageAnalysis.ImageReaderMode.ACQUIRE_LATEST_IMAGE)
//                setTargetRotation(rotation)
//                setTargetResolution(screenSize)
//                setTargetAspectRatio(aspectRatio)
//
//                val analysisThread = HandlerThread("FaceDetectionThread").apply { start() }
//                setCallbackHandler(Handler(analysisThread.looper))
//            }.build()
//        }
//    }
//}
//
//private class FaceAnalyzer(
//    private val listenerHero: MlKitHeroListener? = null,
//    private val listenerEmoji: MlKitEmojiListener? = null
//) : ImageAnalysis.Analyzer {
//
//    private var isAnalyzing = AtomicBoolean(false)
//    var pointsListListener: ((face: FirebaseVisionFace, (List<PointF>)) -> Unit)? = null
//    var analysisSizeListener: ((Size) -> Unit)? = null
//
//    private val faceDetector: FirebaseVisionFaceDetector by lazy {
//        val options = FirebaseVisionFaceDetectorOptions.Builder()
//            .setPerformanceMode(FirebaseVisionFaceDetectorOptions.FAST)
//            .setContourMode(FirebaseVisionFaceDetectorOptions.ALL_CONTOURS)
//            .setClassificationMode(FirebaseVisionFaceDetectorOptions.ALL_CLASSIFICATIONS)
//            .setMinFaceSize(0.9F)
//            .build()
//
//        FirebaseVision.getInstance().getVisionFaceDetector(options)
//    }
//
//    override fun analyze(image: ImageProxy?, rotationDegrees: Int) {
//        val cameraImage = image?.image ?: return
//
//        if (isAnalyzing.get()) return
//        isAnalyzing.set(true)
//
//        Handler(Looper.getMainLooper()).post {
//            analysisSizeListener?.invoke(Size(image.width, image.height))
//        }
//
//        val firebaseVisionImage = FirebaseVisionImage.fromMediaImage(cameraImage, getRotationConstant(rotationDegrees))
//
//        faceDetector.detectInImage(firebaseVisionImage)
//            .addOnSuccessListener { faces ->
//                isAnalyzing.set(false)
//
//                if (faces.isNotEmpty()) {
//                    // Работаем только с одним лицом
//                    val face = faces.first()
//
//                    val points = face.getContour(FirebaseVisionFaceContour.ALL_POINTS).points
//                    // Защита от ситуации, когда точки не были получены
//                    if (points.isNotEmpty()) {
//                        listenerHero?.let { MlKitEngine.calculateHeroActions(face, it) }
//                        listenerEmoji?.let { MlKitEngine.calculateEmojiActions(face, it) }
//
//                        //Debug Info
////                debugListener?.onDebugInfo(face)
//
//                        pointsListListener?.invoke(face, points.map { PointF(it.x, it.y) })
//                    }
//                }
//            }
//            .addOnFailureListener { e ->
//                isAnalyzing.set(false)
//                Log.e("FaceAnalyzer", "Face analysis failure.", e)
//            }
//    }
//
//    private fun getRotationConstant(rotationDegrees: Int): Int {
//        return when (rotationDegrees) {
//            90   -> FirebaseVisionImageMetadata.ROTATION_90
//            180  -> FirebaseVisionImageMetadata.ROTATION_180
//            270  -> FirebaseVisionImageMetadata.ROTATION_270
//            else -> FirebaseVisionImageMetadata.ROTATION_0
//        }
//    }
//}