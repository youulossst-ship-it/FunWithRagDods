package com.example.ragdolllauncher

import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import java.io.File

/**
 * Winlator and GameHub do not document a public "run this .exe" intent, so this does the closest practical thing:
 * copy the exe path to the clipboard, then open the installed runtime so you can add/run it there.
 * Edit RUNTIMES if your build uses a different package name.
 */
object RuntimeLauncher {
    val RUNTIMES = listOf(
        "com.winlator" to "Winlator",
        "com.winlator.cmod" to "Winlator Cmod",
        "com.xiaoji.egggame" to "GameHub",
        "gamehub.lite" to "GameHub Lite",
    )

    fun launch(ctx: Context, exe: File) {
        val cb = ctx.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        cb.setPrimaryClip(ClipData.newPlainText("exe path", exe.absolutePath))
        val pm = ctx.packageManager
        val hit = RUNTIMES.firstNotNullOfOrNull { (pkg, name) -> pm.getLaunchIntentForPackage(pkg)?.let { it to name } }
        val steps = "Game: ${exe.absolutePath}\n(path copied to clipboard)\n\n" +
            "In the runtime: add/choose a container, make sure your Download folder is mapped as a drive, " +
            "then browse to FunWithRagdolls and run the .exe (or create a shortcut for it)."
        if (hit == null) {
            AlertDialog.Builder(ctx).setTitle("No Windows runtime found")
                .setMessage("Install Winlator or GameHub first.\n\n$steps").setPositiveButton("OK", null).show()
        } else {
            AlertDialog.Builder(ctx).setTitle("Open ${hit.second}")
                .setMessage(steps).setNegativeButton("Cancel", null)
                .setPositiveButton("Open") { _, _ -> ctx.startActivity(hit.first.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }.show()
        }
    }
}
