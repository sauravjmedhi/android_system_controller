package com.androidsystemcontroller

import android.content.Intent
import com.facebook.react.bridge.*
import android.content.Context
import android.provider.Settings
import android.text.TextUtils

class SleepTimerModule(reactContext: ReactApplicationContext) :
    ReactContextBaseJavaModule(reactContext) {

    override fun getName() = "SleepTimer"

    @ReactMethod
    fun startService(callType: String, durationSeconds: Int) {

        val intent = Intent(reactApplicationContext, MyForegroundService::class.java)
        intent.putExtra("CALL_TYPE", callType)
        intent.putExtra("DURATION", durationSeconds)

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            reactApplicationContext.startForegroundService(intent)
        } else {
            reactApplicationContext.startService(intent)
        }
    }

    @ReactMethod
    fun stopService() {
        val intent = Intent(reactApplicationContext, MyForegroundService::class.java)
        reactApplicationContext.stopService(intent)
    }

    @ReactMethod
    fun getRemainingTime(promise: Promise) {
        val prefs = reactApplicationContext.getSharedPreferences("TIMER_PREFS", Context.MODE_PRIVATE)
        val endTime = prefs.getLong("END_TIME", 0)

        if (endTime == 0L) {
            promise.resolve(0)
            return
        }

        val remaining = endTime - System.currentTimeMillis()
        promise.resolve((remaining / 1000).toInt().coerceAtLeast(0))
    }

    @ReactMethod
    fun isAccessibilityEnabled(promise: Promise) {
        try {
            val service = reactApplicationContext.packageName + "/" + MyAccessibilityService::class.java.name

            val enabledServices = Settings.Secure.getString(
                reactApplicationContext.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            )

            val isEnabled = !enabledServices.isNullOrEmpty() &&
                    enabledServices.contains(service)

            promise.resolve(isEnabled)

        } catch (e: Exception) {
            promise.resolve(false)
        }
    }

    @ReactMethod
    fun openAccessibilitySettings() {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        reactApplicationContext.startActivity(intent)
    }
}