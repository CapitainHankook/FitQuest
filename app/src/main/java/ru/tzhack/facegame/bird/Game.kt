package ru.tzhack.facegame.bird

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Point
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.view.MotionEvent
import android.view.SurfaceView
import androidx.core.content.ContextCompat
import ru.tzhack.facegame.R
import ru.tzhack.facegame.bird.gameobj.*

sealed class Movement {
    object Stopped : Movement()

    /**
     *  @param speedRatio 0..1
     */
    class Left(val speedRatio: Float) : Movement()

    class Right(val speedRatio: Float) : Movement()
}

@SuppressLint("ViewConstructor")
class Game(
    context: Context,
    size: Point
) : SurfaceView(context),
    Runnable {

    var endGameListener: ((timeOver: Boolean) -> Unit)? = null
    var nightMode = false

    private var playing = false
    private var pause = true
    private var thread: Thread? = null

    // Отключаем ручное управление
    private val manualInput = false

    private var canvas: Canvas = Canvas()
    private val paint: Paint = Paint()

    private val bird: Bird =
        Bird(context, size.x.toFloat())
    private val wallsSize = 20
    private val blocks: List<Block> = Block.generate(
        context,
        screenX = size.x.toFloat(),
        size = wallsSize
    )
    private val finish = Finish(
        positionY = blocks.last().getTop() + (Block.wallsSpacing * 2),
        width = size.x.toFloat(),
        context = context
    )

    private val bullets = ArrayList<Bullet>()
    private var lastShotTime = 0L
    private val bonuses = ArrayList<Bonus>()
    private val gameToolbar = GameToolbar(context, size.x.toFloat())

    private val viewport = Viewport(size.y.toFloat())
    private val input = Input(size)

    private val backgroundColor = ContextCompat.getColor(context, R.color.colorPrimaryDark)
    private val backgroundDarkColor = ContextCompat.getColor(context, R.color.colorBirdGameBackgroundDark)

    init {
        Bullet.init(context)
        Bonus.init(context, size)
    }

    fun start() {
        if (!playing) {
            playing = true
            thread = Thread(this)
            thread!!.start()
        }
    }

    fun stop() {
        if (playing) {
            playing = false
            thread?.join()
            thread = null
        }
    }

    fun isPause() = pause

    fun setPause(value: Boolean) {
        lastShotTime = SystemClock.uptimeMillis()
        pause = value
    }

    override fun run() {
        var lastFrameTime = SystemClock.uptimeMillis()
        while (playing) {
            val time = SystemClock.uptimeMillis()
            val deltaTime = (time - lastFrameTime) / 1000f
            lastFrameTime = time

            if (!pause) {
                update(deltaTime)
            }
            viewport.setWorldY(bird.getY())

            draw()


//            val timeThisFrame = SystemClock.uptimeMillis() - time
//            if (timeThisFrame >= 1) {
//                val fps = 1000 / timeThisFrame
//                Log.d("thread", "fps:$fps")
//            }
        }
    }

    private fun update(dt: Float) {
        var frontBlock: Block? = null
        blocks.forEach {
            if (it.isInFront(bird.position)) {
                frontBlock = it
                return@forEach
            }
        }

        val bonusesIterator = bonuses.iterator()
        while (bonusesIterator.hasNext()) {
            val bonus = bonusesIterator.next()
            if (bonus.collision(bird.position)) {
                when (bonus.type) {
                    BonusType.SPEED_UP,
                    BonusType.SPEED_DOWN -> bird.setSpeedBonus(bonus.type)
                    BonusType.SHOT       -> gameToolbar.countShots++
                    BonusType.TIME       -> gameToolbar.addTime()
                }
                bonusesIterator.remove()
            }
        }

        bird.update(dt, frontBlock)
        gameToolbar.update(dt)
        bonuses.forEach { it.update(dt) }

        val bulletsIterator = bullets.iterator()
        while (bulletsIterator.hasNext()) {
            val bullet = bulletsIterator.next()
            if (bullet.isCleared()) {
                bulletsIterator.remove()
            } else {
                blocks.find { it.checkCrashed(bullet.position) }?.let {
                    bullet.setCrashed()
                }

                bullet.update(dt)
            }
        }

        if (bird.getY() > Bonus.generateWhenPositionY) {
            bonuses.add(Bonus.generate())
        }

        val timeOver = gameToolbar.time <= 0F
        if (finish.position.top < bird.position.top || timeOver) {
            Handler(Looper.getMainLooper()).post {
                endGameListener?.invoke(timeOver)
            }
            stop()
        }
    }

    private fun draw() {
        if (holder.surface.isValid) {
            val lockCanvas = holder.lockCanvas()
            if (lockCanvas != null) {
                canvas = lockCanvas

                canvas.drawColor(if (nightMode) backgroundDarkColor else backgroundColor)

                blocks.forEach { it.draw(canvas, paint, viewport) }

                finish.onDraw(canvas, paint, viewport)

                bonuses.forEach { it.draw(canvas, paint, viewport) }

                bullets.forEach { it.draw(canvas, paint, viewport) }

                bird.draw(canvas, paint, viewport, nightMode)

                gameToolbar.draw(canvas, paint)

                holder.unlockCanvasAndPost(canvas)
            }
        }
    }

    fun shot() {
        if (!pause && playing) {
            if (gameToolbar.countShots > 0 && lastShotTime + Bullet.shotDebounce < SystemClock.uptimeMillis()) {
                gameToolbar.countShots--
                bullets.add(Bullet.create(bird.position))
                bird.setShotState()
                lastShotTime = SystemClock.uptimeMillis()
            }
        }
    }

    fun setMovementState(movement: Movement) {
        if (!pause && playing) {
            bird.setMovementState(movement)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(motionEvent: MotionEvent): Boolean {
        if (manualInput) {
            input.handleInput(motionEvent)
        }
        return true
    }

    internal inner class Input(private val screenSize: Point) {

        fun handleInput(motionEvent: MotionEvent) {
            val x = motionEvent.getX(0).toInt()
            val y = motionEvent.getY(0).toInt()

            when (motionEvent.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    when {
                        y < screenSize.y / 2 -> shot()
                        x > screenSize.x / 2 -> setMovementState(Movement.Right(1f))
                        else                 -> setMovementState(Movement.Left(1f))
                    }
                }
                MotionEvent.ACTION_UP   -> {
                    bird.setMovementState(Movement.Stopped)
                }
            }
        }
    }
}