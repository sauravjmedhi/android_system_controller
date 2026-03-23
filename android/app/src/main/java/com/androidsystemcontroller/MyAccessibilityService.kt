package com.androidsystemcontroller

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.graphics.Rect
import java.util.Queue
import java.util.LinkedList
import android.accessibilityservice.GestureDescription
import android.graphics.Path

class MyAccessibilityService : AccessibilityService() {

    companion object {
        var shouldClickNow = false
        var triggerId = 0
        var instance: MyAccessibilityService? = null
    }

    private var lastHandledTrigger = -1

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        android.util.Log.d("A11Y", "Accessibility Connected")
    }

    fun tapScreen() {
        val displayMetrics = resources.displayMetrics
        val x = displayMetrics.widthPixels / 2f
        val y = displayMetrics.heightPixels / 2f

        val path = android.graphics.Path().apply {
            moveTo(x, y)
            lineTo(x + 1, y + 1) // 🔥 IMPORTANT: tiny movement
        }

        val gesture = android.accessibilityservice.GestureDescription.Builder()
            .addStroke(
                android.accessibilityservice.GestureDescription.StrokeDescription(
                    path,
                    0,
                    100
                )
            )
            .build()

        dispatchGesture(gesture, object : GestureResultCallback() {
            override fun onCompleted(gestureDescription: GestureDescription?) {
                super.onCompleted(gestureDescription)
                android.util.Log.d("A11Y", "Tap gesture completed")
            }

            override fun onCancelled(gestureDescription: GestureDescription?) {
                super.onCancelled(gestureDescription)
                android.util.Log.d("A11Y", "Tap gesture cancelled")
            }
        }, null)
    }

    fun performWhatsAppClickNow() {
        val rootNode = rootInActiveWindow

        // STEP 1: Try immediately
        if (rootNode != null) {
            val target = findWhatsAppEndCall(rootNode)
            if (target != null) {
                android.util.Log.d("A11Y", "End call found immediately")
                target.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                return
            }
        }

        android.util.Log.d("A11Y", "End call not visible → tapping screen")

        tapScreen()

        // STEP 2: Retry a few times
        retryFindEndCall(attempt = 1)
    }

    private fun retryFindEndCall(attempt: Int) {
        if (attempt > 3) {
            android.util.Log.d("A11Y", "Failed after retries")
            return
        }

        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            val rootNode = rootInActiveWindow

            if (rootNode == null) {
                android.util.Log.d("A11Y", "Root null, retrying...")
                retryFindEndCall(attempt + 1)
                return@postDelayed
            }

            val target = findWhatsAppEndCall(rootNode)

            if (target != null) {
                android.util.Log.d("A11Y", "End call found on attempt $attempt")
                target.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            } else {
                android.util.Log.d("A11Y", "Attempt $attempt failed, retrying...")
                retryFindEndCall(attempt + 1)
            }

        }, 400) // small delay per retry
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (!shouldClickNow) return
        if (lastHandledTrigger == triggerId) return

        val rootNode = rootInActiveWindow ?: return
        val target = findBestEndCall(rootNode)

        target?.let {
            it.performAction(AccessibilityNodeInfo.ACTION_CLICK)

            lastHandledTrigger = triggerId
            shouldClickNow = false
        }
    }

    override fun onInterrupt() {}

    private fun findBestEndCall(node: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        var bestNode: AccessibilityNodeInfo? = null
        var lowestY = -1
        val screenWidth = resources.displayMetrics.widthPixels
        val centerX = screenWidth / 2

        val rect = Rect()
        node.getBoundsInScreen(rect)

        val isCenter = rect.centerX() in (centerX - 120)..(centerX + 120)

        if (node.isClickable && isCenter) {
            if (rect.centerY() > lowestY) {
                lowestY = rect.centerY()
                bestNode = node
            }
        }

        for (i in 0 until node.childCount) {
            val child = node.getChild(i)
            if (child != null) {
                val candidate = findBestEndCall(child)
                if (candidate != null) {
                    val r = Rect()
                    candidate.getBoundsInScreen(r)

                    if (r.centerY() > lowestY) {
                        lowestY = r.centerY()
                        bestNode = candidate
                    }
                }
            }
        }

        return bestNode
    }

    private fun findWhatsAppEndCall(root: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        val queue: Queue<AccessibilityNodeInfo> = LinkedList()
        queue.add(root)

        while (queue.isNotEmpty()) {
            val node = queue.poll()
            val desc = node.contentDescription?.toString()

            if (desc != null && desc.contains("Leave call", ignoreCase = true)) {
                return node
            }

            for (i in 0 until node.childCount) {
                val child = node.getChild(i)
                if (child != null) {
                    queue.add(child)
                }
            }
        }

        return null
    }
}