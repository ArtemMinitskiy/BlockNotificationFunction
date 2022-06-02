package com.cleaner.blocknotificationfunction

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class BlockNotificationService : NotificationListenerService() {

    override fun onCreate() {
        super.onCreate()
        toggleNotificationListenerService()
    }

    override fun onDestroy() {
        super.onDestroy()
        toggleNotificationListenerService()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return super.onBind(intent)
    }

    companion object {
        fun isAppNotificationsEnabled(context: Context, packageName: String) = context.getSharedPreferences("com.cleaner.cleanerpro", MODE_PRIVATE).getBoolean(packageName, false)
        fun setAppNotificationsEnabled(context: Context, packageName: String, isAppNotificationsEnabled: Boolean) = context.getSharedPreferences("com.cleaner.cleanerpro", MODE_PRIVATE).edit().putBoolean(packageName, isAppNotificationsEnabled).apply()

        var countLockedNotification = 0
        var list : ArrayList<NotificationData> = ArrayList()
    }

    private var notificationTitle = ""
    private var notificationText = ""
    private var notificationPackageName = ""

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        if (isAppNotificationsEnabled(this, sbn!!.packageName)) {
            countLockedNotification++
            notificationTitle = sbn.notification.extras.getString(Notification.EXTRA_TITLE).toString()
            notificationText = sbn.notification.extras.getString(Notification.EXTRA_TEXT).toString()
            notificationPackageName = sbn.packageName

            val packageManager = applicationContext.packageManager
            val appName = packageManager.getApplicationLabel(packageManager.getApplicationInfo(notificationPackageName, PackageManager.GET_META_DATA)) as String
            list.add(NotificationData(notificationTitle, notificationText, appName))

            sendNotification(countLockedNotification, list)
            try {
                cancelNotification(sbn.key)
            } catch (ignored: SecurityException) {
            }
        }

    }

    data class NotificationData(val title: String, val text: String, val appName: String)

    private fun sendNotification(count: Int,  list: ArrayList<NotificationData>) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationName = resources.getString(R.string.app_name)
            val notificationImportance = NotificationManager.IMPORTANCE_DEFAULT
            val notificationChannel = NotificationChannel(CHANNEL_ID, notificationName, notificationImportance)
            val notificationManager: NotificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(notificationChannel)
        }

        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 30, intent, 0)

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Saved notification $count")
            .setContentIntent(pendingIntent)
            .setAutoCancel(false)
        val inboxStyle = NotificationCompat.InboxStyle()
        list.forEach {
            inboxStyle.addLine("${it.appName}: ${it.title}: ${it.text}")
        }

        notification.setStyle(inboxStyle);

        val mangerCompat = NotificationManagerCompat.from(this)
        mangerCompat.notify(2, notification.build())
    }

    private val CHANNEL_ID = "PRO_CLEANER"

    private fun toggleNotificationListenerService() {
        val pm = packageManager
        pm.setComponentEnabledSetting(ComponentName(this, BlockNotificationService::class.java), PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP)
        pm.setComponentEnabledSetting(ComponentName(this, BlockNotificationService::class.java), PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP)
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {

    }

}