package ru.tzhack.facegame.bird

import ru.tzhack.facegame.bird.utils.Position

class Viewport(private val screenY: Float) {

    private var y: Float = screenY

    companion object {
        private const val BOTTOM_PADDING = 300f
    }

    fun worldToScreenPoint(objectY: Float): Float {
        return y - objectY
    }

    fun setWorldY(globalY: Float) {
        y = globalY + screenY - BOTTOM_PADDING
    }

    fun nowOnScreen(globalPos: Position): Boolean {
        return worldToScreenPoint(globalPos.top) < screenY && worldToScreenPoint(globalPos.bottom) > 0
    }
}