package ru.tzhack.facegame.facetraking.util

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.core.view.ViewCompat
import ru.tzhack.facegame.facetraking.mlkit.maxHeadZ
import ru.tzhack.facegame.facetraking.mlkit.minHeadZ

/* TODO: Make Private to Release */
const val speedMultiply = 150F

fun View.heroHorizontalAnim(headEulerAngleZ: Float) {
    val angleValue = when {
        headEulerAngleZ < minHeadZ -> minHeadZ
        headEulerAngleZ > maxHeadZ -> maxHeadZ
        else                       -> headEulerAngleZ
    } / maxHeadZ * speedMultiply

    translationTo(angleValue)
}

fun View.fadeInOutAnim(actionAfterFadeOut: () -> Unit) {
    val animDuration = 400L
    val fadeInAnim = ValueAnimator.ofFloat(0F, 1F).apply {
        duration = animDuration
        addUpdateListener { value -> alpha = value.animatedValue as Float }
        interpolator = LinearInterpolator()

        repeatCount = 1
        repeatMode = ValueAnimator.REVERSE
        addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                actionAfterFadeOut()
            }
        })
    }

    fadeInAnim.start()
}

fun View.fadeOutInAnim() {
    val animDuration = 400L
    val fadeInAnim = ValueAnimator.ofFloat(1F, 0F).apply {
        duration = animDuration
        addUpdateListener { value -> alpha = value.animatedValue as Float }
        interpolator = LinearInterpolator()

        repeatCount = 1
        repeatMode = ValueAnimator.REVERSE
    }

    fadeInAnim.start()
}

/*** Anim */
private fun View.translationTo(coordinateX: Float) {
    ViewCompat.animate(this)
        .translationXBy(coordinateX)
        .setInterpolator(LinearInterpolator())
        .start()
}
