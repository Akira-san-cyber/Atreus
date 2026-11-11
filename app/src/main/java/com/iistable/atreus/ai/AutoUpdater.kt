package com.iistable.atreus.ai
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
object AutoUpdater {
    suspend fun downloadAndInstall(context: Context, url: String, onProgress: (Int) -> Unit): Boolean = withContext(Dispatchers.IO) {
        try {
            val file = File(context.cacheDir, "update.apk")
            val connection = URL(url).openConnection() as HttpURLConnection
            connection.connect()
            val length = connection.contentLength.toLong()
            file.outputStream().use { out -> connection.inputStream.use { input ->
                val buffer = ByteArray(8192); var read: Int; var total = 0L
                while (input.read(buffer).also { read = it } != -1) { out.write(buffer, 0, read); total += read; if (length > 0) onProgress(((total * 100) / length).toInt()) }
            }}
            withContext(Dispatchers.Main) {
                val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                context.startActivity(Intent(Intent.ACTION_VIEW).apply { setDataAndType(uri, "application/vnd.android.package-archive"); addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK) })
            }
            true
        } catch (e: Exception) { false }
    }
}
