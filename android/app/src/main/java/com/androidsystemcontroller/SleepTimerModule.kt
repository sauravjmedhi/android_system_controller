package com.androidsystemcontroller

import android.content.Intent
import com.facebook.react.bridge.*

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
}