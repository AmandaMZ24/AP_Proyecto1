package com.example.controldeflotillastransandina.ui.maintenance

import android.content.Context
import androidx.core.content.FileProvider
import java.io.File

fun createImageFileUri(context: Context): android.net.Uri {
    val dir = File(context.cacheDir, "images").apply { mkdirs() }
    val file = File(dir, "maint_${System.currentTimeMillis()}.jpg")
    return FileProvider.getUriForFile(context, context.packageName + ".fileprovider", file)
}