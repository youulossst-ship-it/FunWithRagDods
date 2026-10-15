package com.example.ragdolllauncher

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.MotionEvent
import android.view.View
import kotlin.math.hypot
import kotlin.math.min

/** Landscape layout preview: joystick (left), camera pad (right), action buttons. Positions are normalised 0..1. */
class ControlsView(ctx: Context) : View(ctx) {
    data class Btn(val label: String, val x: Float, val y: Float)
    companion object {
        const val JOY_X = .14f; const val JOY_Y = .70f
        const val CAM_X = .62f; const val CAM_Y = .20f; const val CAM_W = .26f; const val CAM_H = .38f
        val BUTTONS = listOf(Btn("GRAB", .90f, .78f), Btn("JUMP", .78f, .86f), Btn("RESET", .93f, .62f))
    }
    private val p = Paint(Paint.ANTI_ALIAS_FLAG)
    private var joy: Pair<Float, Float>? = null; private var joyId = -1
    private var camDelta = 0f to 0f; private var camId = -1; private var camLast = 0f to 0f
    private val down = HashMap<Int, String>()

    override fun onDraw(c: Canvas) {
        c.drawColor(Color.rgb(18, 22, 34))
        val w = width.toFloat(); val h = height.toFloat(); val u = min(w, h)
        p.style = Paint.Style.STROKE; p.strokeWidth = 4f; p.color = Color.argb(160, 255, 255, 255)
        c.drawCircle(JOY_X * w, JOY_Y * h, u * .17f, p)
        p.style = Paint.Style.FILL; p.color = Color.argb(200, 56, 225, 255)
        val j = joy ?: (0f to 0f); c.drawCircle(JOY_X * w + j.first * u * .12f, JOY_Y * h + j.second * u * .12f, u * .07f, p)
        p.style = Paint.Style.STROKE; p.color = Color.argb(160, 255, 255, 255)
        c.drawRoundRect(CAM_X * w, CAM_Y * h, (CAM_X + CAM_W) * w, (CAM_Y + CAM_H) * h, 24f, 24f, p)
        p.style = Paint.Style.FILL; p.textAlign = Paint.Align.CENTER; p.textSize = u * .05f
        c.drawText("CAMERA", (CAM_X + CAM_W / 2) * w, (CAM_Y + CAM_H / 2) * h, p)
        for (b in BUTTONS) {
            p.color = if (down.values.contains(b.label)) Color.rgb(255, 90, 74) else Color.argb(170, 255, 190, 30)
            c.drawCircle(b.x * w, b.y * h, u * .085f, p); p.color = Color.BLACK; c.drawText(b.label, b.x * w, b.y * h + u * .018f, p)
        }
    }

    override fun onTouchEvent(e: MotionEvent): Boolean {
        val w = width.toFloat(); val h = height.toFloat(); val u = min(w, h)
        val i = e.actionIndex; val id = e.getPointerId(i)
        when (e.actionMasked) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                val x = e.getX(i); val y = e.getY(i)
                BUTTONS.firstOrNull { hypot(x - it.x * w, y - it.y * h) < u * .1f }?.let { down[id] = it.label }
                if (id !in down && x < w * .4f) joyId = id
                else if (id !in down && x > CAM_X * w && x < (CAM_X + CAM_W) * w && y > CAM_Y * h && y < (CAM_Y + CAM_H) * h) { camId = id; camLast = x to y }
            }
            MotionEvent.ACTION_MOVE -> for (k in 0 until e.pointerCount) {
                val pid = e.getPointerId(k)
                if (pid == joyId) { var dx = (e.getX(k) - JOY_X * w) / (u * .17f); var dy = (e.getY(k) - JOY_Y * h) / (u * .17f)
                    val l = hypot(dx, dy); if (l > 1) { dx /= l; dy /= l }; joy = dx to dy }
                if (pid == camId) { camDelta = (e.getX(k) - camLast.first) to (e.getY(k) - camLast.second); camLast = e.getX(k) to e.getY(k) }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP, MotionEvent.ACTION_CANCEL -> {
                down.remove(id); if (id == joyId) { joyId = -1; joy = null }; if (id == camId) camId = -1
            }
        }
        invalidate(); return true
    }
}
