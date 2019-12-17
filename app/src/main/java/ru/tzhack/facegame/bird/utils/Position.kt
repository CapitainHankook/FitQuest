package ru.tzhack.facegame.bird.utils

/**
 *  Позиция и размер игрового объекта
 */
data class Position(
    var left: Float,
    var top: Float,
    val width: Float,
    val height: Float
) {

    val right: Float
        get() = left + width

    val bottom: Float
        get() = top - height

    fun contains(pos: Position, allowed: Float = 0f): Boolean {
        return pos.right - allowed >= left && pos.left + allowed < right && pos.top - allowed > bottom && pos.bottom + allowed < top
    }

    fun containsY(pos: Position, allowedY: Float = 0f): Boolean {
        return pos.top - allowedY > bottom && pos.bottom + allowedY < top
    }
}