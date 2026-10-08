package com.example.util

import android.content.Context
import android.net.Uri
import android.util.Log
import java.io.File
import java.io.FileOutputStream

object ImageStorageUtils {
    private const val TAG = "ImageStorageUtils"

    /**
     * Copies an image from a content URI (e.g. from Android PhotoPicker) to the app's internal private storage directory.
     * Returns the permanent `file://` URI string, which persists across app restarts without security/permission issues.
     */
    fun saveImageToInternalStorage(context: Context, sourceUri: Uri, prefix: String = "photo"): String? {
        return try {
            val contentResolver = context.contentResolver
            val inputStream = contentResolver.openInputStream(sourceUri) ?: return null
            
            val fileName = "${prefix}_${System.currentTimeMillis()}.jpg"
            val destFile = File(context.filesDir, fileName)

            FileOutputStream(destFile).use { outputStream ->
                inputStream.use { input ->
                    input.copyTo(outputStream)
                }
            }

            val savedUri = Uri.fromFile(destFile).toString()
            Log.d(TAG, "Successfully copied image to permanent internal storage: $savedUri (file exists: ${destFile.exists()}, size: ${destFile.length()} bytes)")
            savedUri
        } catch (e: Exception) {
            Log.e(TAG, "Failed to copy image to internal storage", e)
            // Fallback to original URI string if copy failed
            sourceUri.toString()
        }
    }
}
