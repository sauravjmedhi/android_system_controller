package com.androidsystemcontroller

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.graphics.Rect
import java.util.Queue
import java.util.LinkedList

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

    fun performWhatsAppClickNow() {
        val rootNode = rootInActiveWindow ?: return
        val target = findWhatsAppEndCall(rootNode)
        target?.performAction(AccessibilityNodeInfo.ACTION_CLICK)
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