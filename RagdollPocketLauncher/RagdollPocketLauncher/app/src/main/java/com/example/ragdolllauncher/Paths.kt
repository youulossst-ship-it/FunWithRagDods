package com.example.ragdolllauncher

import android.content.Context
import android.os.Build
import android.os.Environment
import java.io.File

object Paths {
    /** Android 9/10 (Huawei P30): public Download folder, visible to Winlator/GameHub.
     *  Android 11+: falls back to app-private storage, which other apps usually cannot read. */
    fun gameDir(ctx: Context): File =
        if (Build.VERSION.SDK_INT <= 29)
            File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "FunWithRagdolls")
        else File(ctx.getExternalFilesDir(null), "FunWithRagdolls")
}
