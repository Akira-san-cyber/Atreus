package com.iistable.atreus.ai

import android.content.Context
import android.provider.Settings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

object DeviceManager {
    
    // Get the unique hardware ID of the phone
    fun getDeviceId(context: Context): String {
        return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID) ?: "unknown"
    }

    // Check if this device is the Master Admin
    // (You will replace "PUT_YOUR_DEVICE_ID_HERE" with your actual ID later)
    fun isMasterAdmin(context: Context): Boolean {
        val id = getDeviceId(context)
        // For now, everyone is Admin on first launch. We will lock this down.
        return true 
    }

    // Check the cloud for a remote ban
    suspend fun checkRemoteBan(context: Context): Boolean = withContext(Dispatchers.IO) {
        try {
            // This URL will hold a list of banned Device IDs (e.g., on a free JSON host like Pastebin or GitHub Gist)
            val banListUrl = "https://raw.githubusercontent.com/Akira-san-cyber/Atreus/main/banned_devices.txt"
            val connection = URL(banListUrl).openConnection() as HttpURLConnection
            val response = connection.inputStream.bufferedReader().readText()
            val myId = getDeviceId(context)
            
            // If the response contains this phone's ID, they are banned
            response.contains(myId)
        } catch (e: Exception) {
            false // If internet is off, don't ban
        }
    }
}
