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
    val sci : Float = sc.divideScale(0, parts)
    val sf : Float = 1f - 2 * i
    val x : Float = size * sf * sci.divideScale(0, lines)
    val y : Float = size * sf * sci.divideScale(1, lines)
    drawLine(0f, 0f, x, 0f, paint)
    drawLine(x, 0f, x, y, paint)
    val rectSize = size / (2 * rectSizeFactor)
    save()
    translate(x, y)
    drawRect(-rectSize, -rectSize, rectSize, rectSize, paint)
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
