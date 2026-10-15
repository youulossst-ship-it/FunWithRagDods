package com.example.ragdolllauncher

import android.content.Context
import android.net.Uri
import com.github.junrar.Archive
import java.io.File
import java.util.zip.ZipInputStream

object Extractor {
    fun copyToCache(ctx: Context, uri: Uri): File {
        val out = File(ctx.cacheDir, "input.bin")
        ctx.contentResolver.openInputStream(uri)!!.use { i -> out.outputStream().use { o -> i.copyTo(o, 1 shl 16) } }
        return out
    }

    /** Returns number of files extracted. Supports RAR4 and ZIP. RAR5 is detected and rejected with a clear message. */
    fun extract(src: File, dest: File, log: (String) -> Unit): Int {
        dest.mkdirs()
        val h = ByteArray(8)
        src.inputStream().use { it.read(h) }
        return when {
            h[0] == 'P'.code.toByte() && h[1] == 'K'.code.toByte() -> unzip(src, dest, log)
            h[0] == 'R'.code.toByte() && h[1] == 'a'.code.toByte() -> {
                if (h[6] == 1.toByte()) throw UnsupportedOperationException(
                    "This is a RAR5 archive, which the built-in extractor can't read. Re-pack it as ZIP or RAR4 (or extract it with ZArchiver into Download/FunWithRagdolls), then try again.")
                unrar(src, dest, log)
            }
            else -> throw IllegalArgumentException("Not a RAR or ZIP file")
        }
    }

    private fun safe(dest: File, name: String): File {
        val f = File(dest, name.replace('\\', '/'))
        if (!f.canonicalPath.startsWith(dest.canonicalPath + File.separator)) throw SecurityException("Blocked unsafe path: $name")
        return f
    }

    private fun unrar(src: File, dest: File, log: (String) -> Unit): Int {
        var n = 0
        Archive(src).use { a ->
            while (true) {
                val fh = a.nextFileHeader() ?: break
                val f = safe(dest, fh.fileName)
                if (fh.isDirectory) { f.mkdirs(); continue }
                f.parentFile?.mkdirs()
                f.outputStream().buffered().use { a.extractFile(fh, it) }
                if (++n % 25 == 0) log("Extracted $n files…")
            }
        }
        return n
    }

    private fun unzip(src: File, dest: File, log: (String) -> Unit): Int {
        var n = 0
        ZipInputStream(src.inputStream().buffered()).use { z ->
            while (true) {
                val e = z.nextEntry ?: break
                val f = safe(dest, e.name)
                if (e.isDirectory) { f.mkdirs(); continue }
                f.parentFile?.mkdirs()
                f.outputStream().buffered().use { z.copyTo(it) }
                if (++n % 25 == 0) log("Extracted $n files…")
            }
        }
        return n
    }
}

object ExeFinder {
    private val junk = listOf("unins", "crash", "vcredist", "vc_redist", "dxsetup", "dotnet", "redist", "setup", "updater")
    /** Candidate game executables, best guess first: names containing "ragdoll", then largest file. */
    fun find(root: File): List<File> = root.walkTopDown()
        .filter { it.isFile && it.extension.equals("exe", true) && junk.none { j -> it.name.contains(j, true) } }
        .sortedWith(compareByDescending<File> { it.name.contains("ragdoll", true) }.thenByDescending { it.length() })
        .toList()
}
