package com.androidsystemcontroller

import android.content.Intent
import com.facebook.react.bridge.*

class SleepTimerModule(reactContext: ReactApplicationContext) :
    ReactContextBaseJavaModule(reactContext) {

    override fun getName() = "SleepTimer"

    @ReactMethod
    fun startService() {
        val intent = Intent(reactApplicationContext, MyForegroundService::class.java)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            reactApplicationContext.startForegroundService(intent)
        } else {
            reactApplicationContext.startService(intent)
        }
    }
}