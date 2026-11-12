package com.iistable.atreus.ai

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import kotlinx.coroutines.*

class GameAutomatorService : AccessibilityService() {
    private var job: Job? = null
    private val prefs by lazy { getSharedPreferences("atreus_automation", Context.MODE_PRIVATE) }

    override fun onServiceConnected() {
        super.onServiceConnected()
        job = CoroutineScope(Dispatchers.Default).launch {
            while (true) {
                performAutoClick()
                delay(1000) // Check every 1 second
            }
        }
    }

    private fun performAutoClick() {
        val root = rootInActiveWindow ?: return
        
        // Atreus saves the AI Task here (e.g., "Play,Start,Claim")
        val targetText = prefs.getString("ai_task", "Play,Start,Next,Claim") ?: "Play,Start,Next,Claim"
        val targets = targetText.split(",").map { it.trim() }.filter { it.isNotEmpty() }

        for (text in targets) {
            root.findAccessibilityNodeInfosByText(text).forEach { 
                if (it.isClickable && it.isEnabled) {
                    it.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                } 
            }
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}
    override fun onInterrupt() { job?.cancel() }
    override fun onDestroy() { super.onDestroy(); job?.cancel() }
}
