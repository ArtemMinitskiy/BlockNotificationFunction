package com.cleaner.blocknotificationfunction

import android.app.ActivityManager
import android.app.Notification
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import androidx.core.content.ContextCompat

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
        fun saveNotificationsCount(context: Context, count: Int) = context.getSharedPreferences("com.cleaner.cleanerpro", MODE_PRIVATE).edit().putInt(Constants.NOTIFICATION, count).apply()

        var countLockedNotification = 0
    }

    private var notificationTitle = ""
    private var notificationText = ""
    private var notificationPackageName = ""
    private var isServiceStart = false
    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        if (isAppNotificationsEnabled(this, sbn!!.packageName)) {
            saveNotificationsCount(this, countLockedNotification++)
            notificationTitle = sbn.notification.extras.getString(Notification.EXTRA_TITLE).toString()
            notificationText = sbn.notification.extras.getString(Notification.EXTRA_TEXT).toString()
            notificationPackageName = sbn.packageName
            NotificationRepository.insertData(this, notificationTitle, notificationText, notificationPackageName, false)
            if (!isServiceStart) {
                startService()
            }

            try {
                cancelNotification(sbn.key)
            } catch (ignored: SecurityException) {
            }
        }
//        Toast.makeText(this, "onNotificationPosted $countLockedNotification ", Toast.LENGTH_LONG).show()
    }

    private fun toggleNotificationListenerService() {
        val pm = packageManager
        pm.setComponentEnabledSetting(ComponentName(this, BlockNotificationService::class.java), PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP)
        pm.setComponentEnabledSetting(ComponentName(this, BlockNotificationService::class.java), PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP)
    }

    private fun startService() {
        isServiceStart = true
        val serviceIntent = Intent(this, NotificationSaveService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!isMyServiceRunning(NotificationSaveService::class.java)) {
                ContextCompat.startForegroundService(this, serviceIntent)
            }
        } else {
            if (!isMyServiceRunning(NotificationSaveService::class.java)) {
                startService(Intent(this, NotificationSaveService::class.java))
            }
        }
    }

    private fun isMyServiceRunning(serviceClass: Class<*>): Boolean {
        val manager = getSystemService(ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {

    }

}