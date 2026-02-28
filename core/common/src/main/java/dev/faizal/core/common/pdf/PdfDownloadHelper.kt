package dev.faizal.core.common.pdf

import androidx.annotation.RequiresApi
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class PdfDownloadHelper(private val context: Context) {

    /**
     * Save PDF to Downloads folder
     * Works for all Android versions
     */
    fun savePdfToDownloads(
        sourceFile: File,
        fileName: String,
        onSuccess: (Uri) -> Unit,
        onError: (Exception) -> Unit
    ) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Android 10+ (Scoped Storage)
                savePdfMediaStore(sourceFile, fileName, onSuccess, onError)
            } else {
                // Android 9 and below
                savePdfLegacy(sourceFile, fileName, onSuccess, onError)
            }
        } catch (e: Exception) {
            onError(e)
        }
    }

    /**
     * Android 10+ using MediaStore (Scoped Storage)
     */
    @RequiresApi(Build.VERSION_CODES.Q)
    private fun savePdfMediaStore(
        sourceFile: File,
        fileName: String,
        onSuccess: (Uri) -> Unit,
        onError: (Exception) -> Unit
    ) {
        try {
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }

            val resolver = context.contentResolver
            val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)

            if (uri != null) {
                resolver.openOutputStream(uri)?.use { outputStream ->
                    FileInputStream(sourceFile).use { inputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
                onSuccess(uri)
                showSuccessNotification(fileName, uri)
            } else {
                onError(Exception("Failed to create MediaStore entry"))
            }
        } catch (e: Exception) {
            onError(e)
        }
    }

    /**
     * Android 9 and below (Legacy External Storage)
     */
    private fun savePdfLegacy(
        sourceFile: File,
        fileName: String,
        onSuccess: (Uri) -> Unit,
        onError: (Exception) -> Unit
    ) {
        try {
            val downloadsDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS
            )

            if (!downloadsDir.exists()) {
                downloadsDir.mkdirs()
            }

            val destFile = File(downloadsDir, fileName)

            FileInputStream(sourceFile).use { input ->
                FileOutputStream(destFile).use { output ->
                    input.copyTo(output)
                }
            }

            // Notify media scanner
            val uri = Uri.fromFile(destFile)
            context.sendBroadcast(
                Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri)
            )

            onSuccess(uri)
            showSuccessNotification(fileName, uri)
        } catch (e: Exception) {
            onError(e)
        }
    }

    /**
     * Open PDF with default viewer
     */
    fun openPdf(file: File) {
        try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
            }

            context.startActivity(Intent.createChooser(intent, "Open PDF with"))
        } catch (e: Exception) {
            Toast.makeText(context, "No PDF viewer found", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Share PDF
     */
    fun sharePdf(file: File) {
        try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            }

            context.startActivity(Intent.createChooser(intent, "Share PDF"))
        } catch (e: Exception) {
            Toast.makeText(context, "Failed to share PDF", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Show success notification/toast
     */
    private fun showSuccessNotification(fileName: String, uri: Uri) {
        Toast.makeText(
            context,
            "PDF saved to Downloads: $fileName",
            Toast.LENGTH_LONG
        ).show()
    }

    /**
     * Get file name with timestamp
     */
    fun generateFileName(prefix: String, extension: String = "pdf"): String {
        val timestamp = System.currentTimeMillis()
        return "${prefix}_${timestamp}.${extension}"
    }
}