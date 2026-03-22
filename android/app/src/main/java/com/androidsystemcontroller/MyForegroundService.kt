package com.androidsystemcontroller

import android.app.*
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat

class MyForegroundService : Service() {

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        val channelId = "sleep_timer_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Sleep Timer",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Sleep Timer Running")
            .setContentText("Timer started...")
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .build()

        startForeground(1, notification)

        // TIMER START
        Thread {
            try {
                Thread.sleep(10000)

                android.util.Log.d("SleepTimer", "Timer Finished")

                // 🔥 Wake screen
                val pm = getSystemService(POWER_SERVICE) as android.os.PowerManager

                val wakeLock = pm.newWakeLock(
                    android.os.PowerManager.FULL_WAKE_LOCK or
                    android.os.PowerManager.ACQUIRE_CAUSES_WAKEUP or
                    android.os.PowerManager.ON_AFTER_RELEASE,
                    "SleepTimer::WakeLock"
                )

                wakeLock.acquire(3000)

                // 🔥 Wait for UI to settle
                Thread.sleep(2500)

                // 🔥 Trigger accessibility safely
                MyAccessibilityService.triggerId++
                MyAccessibilityService.shouldClickNow = true

                android.util.Log.d("SleepTimer", "Accessibility Triggered AFTER DELAY")

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null
}