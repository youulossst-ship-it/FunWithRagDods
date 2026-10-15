package com.example.ragdolllauncher

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.File
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {
    private val io = Executors.newSingleThreadExecutor()
    private var picked: Uri? = null
    private var exes: List<File> = emptyList()
    private lateinit var logView: TextView
    private lateinit var extractBtn: Button
    private lateinit var launchBtn: Button
    private lateinit var bar: ProgressBar

    private val picker = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) { picked = uri; extractBtn.isEnabled = true; log("Selected: ${displayName(uri)}") }
    }

    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        val pad = (16 * resources.displayMetrics.density).toInt()
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(pad, pad, pad, pad) }
        fun btn(t: String, on: () -> Unit) = Button(this).apply { text = t; setOnClickListener { on() } }.also { root.addView(it) }
        root.addView(TextView(this).apply { text = "Ragdoll Pocket Launcher"; textSize = 22f })
        btn("1. Select game archive (RAR/ZIP)") { picker.launch(arrayOf("*/*")) }
        extractBtn = btn("2. Extract & find .exe") { extract() }.apply { isEnabled = false }
        bar = ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal).apply { isIndeterminate = true; visibility = View.GONE }
        root.addView(bar)
        launchBtn = btn("3. Launch in Winlator / GameHub") { chooseAndLaunch() }.apply { isEnabled = false }
        btn("Touch controls layout") { startActivity(Intent(this, ControlsActivity::class.java)) }
        logView = TextView(this).apply { textSize = 13f }
        root.addView(ScrollView(this).apply { addView(logView) }, LinearLayout.LayoutParams(-1, 0, 1f))
        setContentView(root)
        log("Extracts to: ${Paths.gameDir(this).path}")
        if (Build.VERSION.SDK_INT <= 29 && ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE), 1)
    }

    private fun log(s: String) = runOnUiThread { logView.append(s + "\n") }
    private fun displayName(u: Uri) = contentResolver.query(u, null, null, null, null)?.use {
        if (it.moveToFirst()) it.getString(it.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME)) else null } ?: u.toString()

    private fun extract() {
        val uri = picked ?: return
        extractBtn.isEnabled = false; bar.visibility = View.VISIBLE
        io.execute {
            val tmp = File(cacheDir, "input.bin")
            try {
                val dest = Paths.gameDir(this)
                log("Copying archive to cache (needs free space ≈ archive size)…")
                Extractor.copyToCache(this, uri)
                log("Extracting…")
                val n = Extractor.extract(tmp, dest) { log(it) }
                exes = ExeFinder.find(dest)
                log("Done: $n files. Found ${exes.size} .exe candidate(s):")
                exes.take(6).forEach { log(" • ${it.relativeTo(dest)}") }
                runOnUiThread { launchBtn.isEnabled = exes.isNotEmpty() }
            } catch (e: Throwable) { log("Error: ${e.message ?: e.javaClass.simpleName}") }
            finally { tmp.delete(); runOnUiThread { bar.visibility = View.GONE; extractBtn.isEnabled = true } }
        }
    }

    private fun chooseAndLaunch() {
        if (exes.size <= 1) { exes.firstOrNull()?.let { RuntimeLauncher.launch(this, it) }; return }
        val root = Paths.gameDir(this)
        AlertDialog.Builder(this).setTitle("Which .exe?")
            .setItems(exes.take(8).map { it.relativeTo(root).path }.toTypedArray()) { _, i -> RuntimeLauncher.launch(this, exes[i]) }.show()
    }
}
