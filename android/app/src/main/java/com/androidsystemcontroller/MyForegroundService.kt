package com.androidsystemcontroller

import android.app.*
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat

class MyForegroundService : Service() {

    companion object {
        var currentThread: Thread? = null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        android.util.Log.d("SleepTimer", "Timer Started")

        currentThread?.interrupt()

        val callType = intent?.getStringExtra("CALL_TYPE") ?: "normal"
        val durationSeconds = intent?.getIntExtra("DURATION", 10) ?: 10

        val prefs = getSharedPreferences("TIMER_PREFS", MODE_PRIVATE)
        val endTime = System.currentTimeMillis() + (durationSeconds * 1000)
        prefs.edit().putLong("END_TIME", endTime).apply()

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

        val notificationManager = getSystemService(NotificationManager::class.java)

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Sleep Timer Running")
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setOnlyAlertOnce(true)

        startForeground(
            1,
            notificationBuilder
                .setContentText("Ends in: --:--")
                .build()
        )

        val thread = Thread {
            try {
                while (true) {
                    if (Thread.currentThread().isInterrupted) {
                        android.util.Log.d("SleepTimer", "Previous timer cancelled")
                        return@Thread
                    }

                    val remainingMillis = endTime - System.currentTimeMillis()

                    if (remainingMillis <= 0) break

                    val totalSeconds = (remainingMillis / 1000).toInt()
                    val minutes = totalSeconds / 60
                    val seconds = totalSeconds % 60

                    val timeFormatted = String.format("%02d:%02d", minutes, seconds)

                    notificationManager.notify(
                        1,
                        notificationBuilder
                            .setContentText("Ends in: $timeFormatted")
                            .build()
                    )

                    Thread.sleep(1000)
                }

                android.util.Log.d("SleepTimer", "Timer Finished")

                val pm = getSystemService(POWER_SERVICE) as android.os.PowerManager

                val wakeLock = pm.newWakeLock(
                    android.os.PowerManager.FULL_WAKE_LOCK or
                    android.os.PowerManager.ACQUIRE_CAUSES_WAKEUP or
                    android.os.PowerManager.ON_AFTER_RELEASE,
                    "SleepTimer::WakeLock"
                )

                wakeLock.acquire(3000)

                Thread.sleep(2500)

                if (callType == "whatsapp") {
                    MyAccessibilityService.instance?.performWhatsAppClickNow()
                } else {
                    MyAccessibilityService.triggerId++
                    MyAccessibilityService.shouldClickNow = true
                    MyAccessibilityService.instance?.forceEndCallCheck()
                }

                Thread.sleep(5000)

                prefs.edit().remove("END_TIME").apply()

                stopForeground(true)
                stopSelf()

            } catch (e: InterruptedException) {
                // expected when cancelling → DO NOTHING
                android.util.Log.d("SleepTimer", "Thread interrupted safely")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        currentThread = thread
        thread.start()

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()

        android.util.Log.d("SleepTimer", "Service destroyed")

        currentThread?.interrupt()
        currentThread = null

        val prefs = getSharedPreferences("TIMER_PREFS", MODE_PRIVATE)
        prefs.edit().remove("END_TIME").apply()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)

        android.util.Log.d("SleepTimer", "App removed → stopping timer")

        // Stop running thread
        currentThread?.interrupt()
        currentThread = null

        val prefs = getSharedPreferences("TIMER_PREFS", MODE_PRIVATE)
        prefs.edit().remove("END_TIME").apply()

        // Remove notification
        stopForeground(true)

        // Stop service completely
        stopSelf()
    }
}