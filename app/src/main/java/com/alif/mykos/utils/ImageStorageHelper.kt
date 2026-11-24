package com.alif.mykos.utils

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

object ImageStorageHelper {

    fun copyUriToInternalStorage(context: Context, sourceUri: Uri): String? {
        return try {
            if (sourceUri.scheme == "file") {
                return sourceUri.toString()
            }

            val photosDir = File(context.filesDir, "kamar_photos").apply {
                if (!exists()) mkdirs()
            }

            val destFile = File(photosDir, "kamar_${UUID.randomUUID()}.jpg")
            context.contentResolver.openInputStream(sourceUri)?.use { inputStream ->
                FileOutputStream(destFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            } ?: return null

            Uri.fromFile(destFile).toString()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun createTempCameraUri(context: Context): Uri {
        val tempDir = File(context.cacheDir, "camera_temp").apply {
            if (!exists()) mkdirs()
        }
        val tempFile = File(tempDir, "capture_${System.currentTimeMillis()}.jpg")
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            tempFile
        )
    }

    fun deleteInternalPhoto(fotoUriString: String?) {
        if (fotoUriString.isNullOrBlank()) return
        try {
            val uri = Uri.parse(fotoUriString)
            if (uri.scheme == "file" && uri.path != null) {
                val file = File(uri.path!!)
                if (file.exists()) {
                    file.delete()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}