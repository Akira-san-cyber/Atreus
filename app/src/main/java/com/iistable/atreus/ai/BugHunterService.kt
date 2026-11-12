package com.iistable.atreus.ai

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.net.URL

class BugHunterService : Service() {
    private val serviceScope = CoroutineScope(Dispatchers.IO)
    private val CHANNEL_ID = "BugHunterChannel"

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val target = intent?.getStringExtra("TARGET") ?: "example.com"
        
        createNotificationChannel()
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Atreus White-Hat Hunter")
            .setContentText("Hunting vulnerabilities on $target in the background...")
            .setSmallIcon(android.R.drawable.ic_menu_search)
            .setOngoing(true)
            .build()

        startForeground(1, notification)

        serviceScope.launch {
            // Run Nmap
            val nmapResult = downloadAndRunNmap(target)
            // Run Web Toolkit
            val webToolkitResult = OffensiveToolkit.executeFullScan(target)
            
            val finalReport = "$nmapResult\n\n$webToolkitResult"
            
            val reportFile = File(filesDir, "last_hunt_report.txt")
            reportFile.writeText(finalReport)
            
            stopSelf()
        }
        return START_NOT_STICKY
    }

    private fun downloadAndRunNmap(target: String): String {
        val nmapFile = File(filesDir, "nmap")
        if (!nmapFile.exists()) {
            try {
                val url = URL("https://github.com/andronixApp/AndronixOrigin/raw/master/binaries/nmap")
                val connection = url.openConnection()
                connection.connect()
                val input = connection.getInputStream()
                val output = FileOutputStream(nmapFile)
                input.copyTo(output)
                output.close()
                input.close()
                nmapFile.setExecutable(true)
            } catch (e: Exception) {
                return "Failed to download Nmap: ${e.message}"
            }
        }
        val command = "${nmapFile.absolutePath} -F -T4 $target"
        return runTerminalCommand(command)
    }

    private fun runTerminalCommand(command: String): String {
        return try {
            val process = Runtime.getRuntime().exec(arrayOf("sh", "-c", command))
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val output = StringBuilder()
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                output.append(line).append("\n")
            }
            output.toString()
        } catch (e: Exception) {
            "Error: ${e.message}"
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID, "Bug Hunter Service",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(serviceChannel)
        }
    }
}
