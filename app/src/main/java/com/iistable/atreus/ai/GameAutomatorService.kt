package com.iistable.atreus.ai
import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import kotlinx.coroutines.*
class GameAutomatorService : AccessibilityService() {
    private var job: Job? = null
    override fun onServiceConnected() { super.onServiceConnected(); job = CoroutineScope(Dispatchers.Default).launch { while (true) { performAutoClick(); delay(500) } } }
    private fun performAutoClick() {
        val root = rootInActiveWindow ?: return
        listOf("Start", "Play", "Next", "Continue", "Collect", "Upgrade", "Claim").forEach { text ->
            root.findAccessibilityNodeInfosByText(text).forEach { if (it.isClickable && it.isEnabled) it.performAction(AccessibilityNodeInfo.ACTION_CLICK) }
        }
    }
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}
    override fun onInterrupt() { job?.cancel() }
    override fun onDestroy() { super.onDestroy(); job?.cancel() }
}
