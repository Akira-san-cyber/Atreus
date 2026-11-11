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
object Installer {
    suspend fun downloadAndInstall(context: Context, url: String, fileName: String, onProgress: (Int) -> Unit): String = withContext(Dispatchers.IO) {
        try {
            val file = File(context.cacheDir, fileName)
            val connection = URL(url).openConnection() as HttpURLConnection
            connection.connect()
            val fileLength = connection.contentLength.toLong()
            file.outputStream().use { output ->
                connection.inputStream.use { input ->
                    val buffer = ByteArray(8192)
                    var bytesRead: Int
                    var totalRead = 0L
                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                        totalRead += bytesRead
                        if (fileLength > 0) onProgress(((totalRead * 100) / fileLength).toInt())
                    }
                }
            }
            withContext(Dispatchers.Main) {
                val apkUri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(apkUri, "application/vnd.android.package-archive")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            }
            "✅ Downloaded $fileName"
        } catch (e: Exception) { "⚠️ Failed: ${e.message}" }
    }
}
