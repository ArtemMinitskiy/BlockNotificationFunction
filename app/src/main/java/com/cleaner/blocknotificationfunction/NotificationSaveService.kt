package com.cleaner.blocknotificationfunction

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.CountDownTimer
import android.os.IBinder
import android.os.SystemClock
import android.util.Log
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.cleaner.blocknotificationfunction.Constants.NOTIFICATION
import com.cleaner.blocknotificationfunction.Constants.SCREEN_TYPE
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NotificationSaveService : Service() {
    private val CHANNEL_ID = "PRO_CLEANER"
    private var remoteViews: RemoteViews? = null
    private val scope = CoroutineScope(Dispatchers.IO)
    private var notifCount = 0
    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        scope.launch {
            startForeground(1, buildNotification(this@NotificationSaveService, BlockNotificationService.countLockedNotification))
        }
        notifStartUpdateTimer()
        return START_NOT_STICKY
    }

    private fun notifStartUpdateTimer() {
        val timer = object : CountDownTimer(300_000L, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                notifCount = BlockNotificationService.countLockedNotification
                Log.i("NotificationLog", "Count $notifCount")
                scope.launch {
                    startForeground(1, buildNotification(this@NotificationSaveService, BlockNotificationService.countLockedNotification))
                }

            }

            override fun onFinish() {
                scope.launch {
                    startForeground(1, buildNotification(this@NotificationSaveService, BlockNotificationService.countLockedNotification))
                }
                notifStartUpdateTimer()
            }
        }
        timer.start()
    }

    fun buildNotification(context: Context, countLockedNotification: Int): Notification {
        createNotificationChannel(context)
        remoteViews = RemoteViews(packageName, R.layout.notification_save_badge)

        val countIntent = Intent(context, MainActivity::class.java).apply { putExtra(SCREEN_TYPE, NOTIFICATION) }
        countIntent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        countIntent.flags = Intent.FLAG_ACTIVITY_NO_ANIMATION
        val countPendingIntent = PendingIntent.getActivity(context, 1, countIntent, 0)

        remoteViews!!.setOnClickPendingIntent(R.id.badgeBtn, countPendingIntent)

        remoteViews!!.setTextViewText(R.id.count, countLockedNotification.toString())

        return NotificationCompat.Builder(context, CHANNEL_ID).setDefaults(Notification.DEFAULT_SOUND).setVibrate(longArrayOf(0L)).setCategory(Notification.CATEGORY_SERVICE).setSmallIcon(R.mipmap.ic_launcher).setContent(remoteViews).build()
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nChannel = NotificationChannel(CHANNEL_ID, "Pro Cleaner Notification Channel", NotificationManager.IMPORTANCE_NONE)
            val nManager = context.getSystemService(NotificationManager::class.java)
            nManager.createNotificationChannel(nChannel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        val intent = Intent()
        intent.action = "servicereload"
        intent.setClass(this, NotificationSaveReceiver::class.java)
        this.sendBroadcast(intent)
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        val intent = Intent(applicationContext, this.javaClass)
        intent.setPackage(packageName)
        val pendingIntent = PendingIntent.getService(applicationContext, 1, intent, PendingIntent.FLAG_ONE_SHOT)
        val alarmManager = applicationContext.getSystemService(ALARM_SERVICE) as AlarmManager
        alarmManager[AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + 1000] = pendingIntent
        super.onTaskRemoved(rootIntent)
    }

}