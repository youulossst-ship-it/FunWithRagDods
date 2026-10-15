package com.example.ragdolllauncher

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class ControlsActivity : AppCompatActivity() {
    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        val root = FrameLayout(this)
        root.addView(ControlsView(this))
        root.addView(Button(this).apply {
            text = "Export Winlator profile"
            setOnClickListener {
                try { Toast.makeText(context, "Saved: ${export().path}", Toast.LENGTH_LONG).show() }
                catch (e: Exception) { Toast.makeText(context, "Failed: ${e.message}", Toast.LENGTH_LONG).show() }
            }
        }, FrameLayout.LayoutParams(-2, -2))
        setContentView(root)
    }

    /** EXPERIMENTAL: Winlator's profile JSON schema is undocumented and may differ by version/fork. */
    private fun el(type: String, shape: String, bind: List<String>, scale: Double, x: Double, y: Double, text: String) =
        JSONObject().put("type", type).put("shape", shape).put("bindings", JSONArray(bind))
            .put("scale", scale).put("x", x).put("y", y).put("toggleSwitch", false).put("text", text).put("iconId", 0)

    private fun export(): File {
        val els = JSONArray()
        els.put(el("STICK", "CIRCLE", listOf("KEY_W", "KEY_D", "KEY_S", "KEY_A"), 1.4, ControlsView.JOY_X.toDouble(), ControlsView.JOY_Y.toDouble(), ""))
        els.put(el("TRACKPAD", "RECT", listOf("MOUSE_MOVE_UP", "MOUSE_MOVE_RIGHT", "MOUSE_MOVE_DOWN", "MOUSE_MOVE_LEFT"), 2.4,
            (ControlsView.CAM_X + ControlsView.CAM_W / 2).toDouble(), (ControlsView.CAM_Y + ControlsView.CAM_H / 2).toDouble(), "CAMERA"))
        val keys = mapOf("GRAB" to "MOUSE_LEFT_BUTTON", "JUMP" to "KEY_SPACE", "RESET" to "KEY_R")
        ControlsView.BUTTONS.forEach { els.put(el("BUTTON", "CIRCLE", listOf(keys.getValue(it.label)), 1.1, it.x.toDouble(), it.y.toDouble(), it.label)) }
        val out = File(Paths.gameDir(this).apply { mkdirs() }, "ragdolls-controls.json")
        out.writeText(JSONObject().put("id", 9001).put("name", "Ragdolls P30").put("elements", els).toString(2))
        return out
    }
}
