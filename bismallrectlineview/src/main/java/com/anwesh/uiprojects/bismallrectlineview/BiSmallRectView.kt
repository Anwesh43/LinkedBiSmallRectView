package com.anwesh.uiprojects.bismallrectlineview

/**
 * Created by anweshmishra on 31/07/19.
 */

import android.view.View
import android.view.MotionEvent
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Color
import android.graphics.Canvas
import android.content.Context
import android.app.Activity

val nodes : Int = 5
val lines : Int = 2
val parts : Int = 2
val scGap : Float = 0.05f
val scDiv : Double = 0.51
val strokeFactor : Int = 90
val sizeFactor : Float = 2.9f
val foreColor : Int = Color.parseColor("#388E3C")
val backColor : Int = Color.parseColor("#BDBDBD")
val rectSizeFactor : Float = 3.7f

fun Int.inverse() : Float = 1f / this
fun Float.scaleFactor() : Float = Math.floor(this / scDiv).toFloat()
fun Float.maxScale(i : Int, n : Int) : Float = Math.max(0f, this - i * n.inverse())
fun Float.divideScale(i : Int, n : Int) : Float = Math.min(n.inverse(), maxScale(i, n)) * n
fun Float.mirrorValue(a : Int, b : Int) : Float {
    val k : Float = scaleFactor()
    return (1 - k) * a.inverse() + k * b.inverse()
}
fun Float.updateValue(dir : Float, a : Int, b : Int) : Float = mirrorValue(a, b) * dir * scGap

fun Canvas.drawBiSmallRect(i : Int, sc : Float, size : Float, paint : Paint) {
    val sci : Float = sc.divideScale(i, parts)
    val sf : Float = 1f - 2 * i
    val x : Float = size * sf * sci.divideScale(0, lines)
    val y : Float = size * sf * sci.divideScale(1, lines)
    drawLine(0f, 0f, x, 0f, paint)
    drawLine(x, 0f, x, y, paint)
    val rectSize = size / (2 * rectSizeFactor)
    save()
    translate(x, y)
    drawRect(RectF(-rectSize, -rectSize, rectSize, rectSize), paint)
    restore()
}

fun Canvas.drawBSRNode(i : Int, scale : Float, paint : Paint) {
    val w : Float = width.toFloat()
    val h : Float = height.toFloat()
    val gap : Float = h / (nodes + 1)
    val size : Float = gap / sizeFactor
    paint.color = foreColor
    paint.strokeCap = Paint.Cap.ROUND
    paint.strokeWidth = Math.min(w, h) / strokeFactor
    val sc1 : Float = scale.divideScale(0, 2)
    val sc2 : Float = scale.divideScale(1, 2)
    save()
    translate(w / 2, gap * (i + 1))
    rotate(90f * sc2)
    for (j in 0..(parts - 1)) {
        drawBiSmallRect(j, sc1, size, paint)
    }
    restore()
}

class BiSmallRectView(ctx : Context) : View(ctx) {

    private val paint : Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val renderer : Renderer = Renderer(this)

    override fun onDraw(canvas : Canvas) {
        renderer.render(canvas, paint)
    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                renderer.handleTap()
            }
        }
        return true
    }

    data class State(var scale : Float = 0f, var dir : Float = 0f, var prevScale : Float = 0f) {

        fun update(cb : (Float) -> Unit) {
            scale += scale.updateValue(dir, lines, 1)
            if (Math.abs(scale - prevScale) > 1) {
                scale = prevScale + dir
                dir = 0f
                prevScale = scale
                cb(prevScale)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            if (dir == 0f) {
                dir = 1f - 2 * prevScale
                cb()
            }
        }
    }

    data class Animator(var view : View, var animated : Boolean = false) {

        fun animate(cb : () -> Unit) {
            if (animated) {
                cb()
                try {
                    Thread.sleep(50)
                    view.invalidate()
                } catch(ex : Exception) {

                }
            }
        }

        fun start() {
            if (!animated) {
                animated = true
                view.postInvalidate()
            }
        }

        fun stop() {
            if (animated) {
                animated = false
            }
        }
    }

    data class BSRNode(var i : Int, val state : State = State()) {

        private var next : BSRNode? = null
        private var prev : BSRNode? = null

        init {
            addNeighbor()
        }

        fun addNeighbor() {
            if (i < nodes - 1) {
                next = BSRNode(i + 1)
                next?.prev = this
            }
        }

        fun draw(canvas : Canvas, paint : Paint) {
            canvas.drawBSRNode(i, state.scale, paint)
            next?.draw(canvas, paint)
        }

        fun update(cb : (Int, Float) -> Unit) {
            state.update {
                cb(i, it)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            state.startUpdating(cb)
        }

        fun getNeighbor(dir : Int, cb : () -> Unit) : BSRNode {
            var curr : BSRNode? = prev
            if (dir == 1) {
                curr = next
            }
            if (curr != null) {
                return curr
            }
            cb()
            return this
        }
    }

    data class BiSmallRect(var i : Int) {

        private val root : BSRNode = BSRNode(0)
        private var curr : BSRNode = root
        private var dir : Int = 1

        fun draw(canvas : Canvas, paint : Paint) {
            root.draw(canvas, paint)
        }

        fun update(cb : (Int, Float) -> Unit) {
            curr.update {i, scl ->
                curr = curr.getNeighbor(dir) {
                    dir *=-1
                }
                cb(i, scl)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            curr.startUpdating(cb)
        }
    }

    data class Renderer(var view : BiSmallRectView) {

        private val bsr : BiSmallRect = BiSmallRect(0)
        private val animator : Animator = Animator(view)

        fun render(canvas : Canvas, paint : Paint) {
            canvas.drawColor(backColor)
            bsr.draw(canvas, paint)
            animator.animate {
                bsr.update {i, scl ->
                    animator.stop()
                }
            }
        }

        fun handleTap() {
            bsr.startUpdating {
                animator.start()
            }
        }
    }

    companion object {

        fun create(activity : Activity) : BiSmallRectView {
            val view : BiSmallRectView = BiSmallRectView(activity)
            activity.setContentView(view)
            return view
        }
    }
}