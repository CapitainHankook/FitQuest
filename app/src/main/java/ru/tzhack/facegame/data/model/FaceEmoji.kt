package ru.tzhack.facegame.data.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import ru.tzhack.facegame.R

enum class FaceEmoji(
    @DrawableRes val resAnim: Int,
    @StringRes val resDescription: Int
) {
    DOUBLE_EYE_CLOSE(R.drawable.double_eye_anim, R.string.face_emoji_double_eye_close),
    LEFT_EYE_CLOSE(R.drawable.left_eye_anim, R.string.face_emoji_left_eye_close),
    RIGHT_EYE_CLOSE(R.drawable.right_eye_anim, R.string.face_emoji_right_eye_close),

    DOUBLE_EYEBROWN_MOVE(R.drawable.double_eyebrown_anim, R.string.face_emoji_double_eyebrown_move),

    SMILE(R.drawable.smile_anim, R.string.face_emoji_smile),

    MOUTH_OPEN(R.drawable.mouth_open_anim, R.string.face_emoji_mouth_open),

    HEAD_BIAS_LEFT(R.drawable.head_bias_left_anim, R.string.face_emoji_head_bias_left),
    HEAD_BIAS_RIGHT(R.drawable.head_bias_right_anim, R.string.face_emoji_head_bias_right),

    HEAD_BIAS_DOWN(R.drawable.head_bias_down_anim, R.string.face_emoji_head_bias_down),
    HEAD_BIAS_UP(R.drawable.head_bias_up_anim, R.string.face_emoji_head_bias_up),

    HEAD_ROTATE_LEFT(R.drawable.head_rotate_left_anim, R.string.face_emoji_head_rotate_left),
    HEAD_ROTATE_RIGHT(R.drawable.head_rotate_right_anim, R.string.face_emoji_head_rotate_right),
}