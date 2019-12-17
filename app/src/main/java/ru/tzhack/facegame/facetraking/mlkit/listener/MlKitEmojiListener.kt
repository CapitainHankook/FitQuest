package ru.tzhack.facegame.facetraking.mlkit.listener

import ru.tzhack.facegame.data.model.FaceEmoji

interface MlKitEmojiListener {
    /**
     * Данный метод вызывается, когда пользователь изображает
     * какую-либо эмоцию.
     *
     * @param emoji текущая эмоция пользователя.
     * */
    fun onEmojiObtained(emoji: FaceEmoji)
}