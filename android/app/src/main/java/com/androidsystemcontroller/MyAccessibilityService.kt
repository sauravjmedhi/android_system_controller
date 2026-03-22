package com.androidsystemcontroller

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.graphics.Rect

class MyAccessibilityService : AccessibilityService() {

    companion object {
        var shouldClickNow = false
        var triggerId = 0
    }

    private var lastHandledTrigger = -1

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {

        // 🚨 Only act when triggered
        if (!shouldClickNow) return

        // 🚨 Prevent duplicate handling
        if (lastHandledTrigger == triggerId) return

        val rootNode = rootInActiveWindow ?: return

        val target = findBestEndCall(rootNode)

        target?.let {
            android.util.Log.d("A11Y", "CLICKING BEST MATCH")
            it.performAction(AccessibilityNodeInfo.ACTION_CLICK)

            lastHandledTrigger = triggerId
            shouldClickNow = false
        }
    }

    override fun onInterrupt() {}

    // 🔥 Find best candidate (center + lowest)
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
            node.getChild(i)?.let {
                val candidate = findBestEndCall(it)
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

    override fun onServiceConnected() {
        super.onServiceConnected()
        android.util.Log.d("A11Y", "Accessibility Connected")
    }
}