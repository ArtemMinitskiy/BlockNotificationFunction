package com.cleaner.blocknotificationfunction

import android.annotation.SuppressLint
import android.app.Notification
import android.content.*
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Message
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.EXTRA_SMALL_ICON

class BlockNotificationService: NotificationListenerService() {

    var mRemovedNotification: StatusBarNotification? = null



    // String a;
    @SuppressLint("HandlerLeak")
    private val mMonitorHandler: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                EVENT_UPDATE_CURRENT_NOS -> updateCurrentNotifications()
                else -> {
                }
            }
        }
    }

    private val brNotificationsManager = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action: String?
            if (intent.action != null) {
                action = intent.action
                if (action == ACTION_NLS_CONTROL) {
                    val command = intent.getStringExtra("command")
                    if (TextUtils.equals(command, "cancel_last")) {
                        if (mCurrentNotificationsCounts >= 1) {
                            val statusBarNotification: StatusBarNotification = getCurrentNotifications()!![mCurrentNotificationsCounts - 1]
                            cancelNotification(statusBarNotification.packageName, statusBarNotification.tag, statusBarNotification.id)
                        }
                    } else if (TextUtils.equals(command, "cancel_all")) {
                        cancelAllNotifications()
                    }
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        logNLS("onCreate...")
        val filter = IntentFilter()
        filter.addAction(ACTION_NLS_CONTROL)
        registerReceiver(brNotificationsManager, filter)
        mMonitorHandler.sendMessage(mMonitorHandler.obtainMessage(EVENT_UPDATE_CURRENT_NOS))
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(brNotificationsManager)
    }

    override fun onBind(intent: Intent?): IBinder? {
        // a.equals("b");
        logNLS("onBind...")
        return super.onBind(intent)
    }

    companion object {

        private val EVENT_UPDATE_CURRENT_NOS = 0
        var ACTION_NLS_CONTROL = "com.cleaner.blocknotificationfunction.NLSCONTROL"
        var mCurrentNotificationsCounts = 0
        var mCurrentNotifications: MutableList<Array<StatusBarNotification>?> = ArrayList()
        var mPostedNotification: StatusBarNotification? = null
        fun getCurrentNotifications(): Array<StatusBarNotification>? {
            if (mCurrentNotifications.size == 0) {
                logNLS("mCurrentNotifications size is ZERO!!")
                return null
            }
            return mCurrentNotifications[0]
        }
        private val TAG = "BlockNotification"
        private val TAG_PRE = "[" + BlockNotificationService::class.java.getSimpleName() + "] "
        private fun logNLS(`object`: Any) {
            Log.i(TAG, TAG_PRE + `object`)
        }

        fun isAppNotificationsEnabled(context: Context, packageName: String) = context.getSharedPreferences("com.cleaner.cleanerpro", MODE_PRIVATE).getBoolean(packageName, false)
        fun setAppNotificationsEnabled(context: Context, packageName: String, isAppNotificationsEnabled: Boolean) = context.getSharedPreferences("com.cleaner.cleanerpro", MODE_PRIVATE).edit().putBoolean(packageName, isAppNotificationsEnabled).apply()

        var notificationTitle = ""
        var countLockedNotification = 0
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        if (isAppNotificationsEnabled(this, sbn!!.packageName)) {
            countLockedNotification++
            try {
                cancelNotification(sbn.key)
            } catch (ignored: SecurityException) {}
        }

        updateCurrentNotifications()
        logNLS("onNotificationPosted...")
//        Toast.makeText(this, "onNotificationPosted ${sbn.packageName} ", Toast.LENGTH_LONG).show()
        Toast.makeText(this, "onNotificationPosted $countLockedNotification ", Toast.LENGTH_LONG).show()
//        Toast.makeText(this, "onNotificationPosted ${sbn.notification.extras.getString(Notification.EXTRA_TITLE)} "  +
//                "\n" +
//                "${sbn.notification.extras.getString(Notification.EXTRA_TEXT)}", Toast.LENGTH_LONG).show()
        logNLS("have $mCurrentNotificationsCounts active notifications")
        mPostedNotification = sbn
        notificationTitle = sbn!!.notification.extras.getString(Notification.EXTRA_TITLE).toString()
        logNLS("${sbn!!.notification.extras.getString(EXTRA_SMALL_ICON)}")

        /*
         * Bundle extras = sbn.getNotification().extras; String
         * notificationTitle = extras.getString(Notification.EXTRA_TITLE);
         * Bitmap notificationLargeIcon = ((Bitmap)
         * extras.getParcelable(Notification.EXTRA_LARGE_ICON)); Bitmap
         * notificationSmallIcon = ((Bitmap)
         * extras.getParcelable(Notification.EXTRA_SMALL_ICON)); CharSequence
         * notificationText = extras.getCharSequence(Notification.EXTRA_TEXT);
         * CharSequence notificationSubText =
         * extras.getCharSequence(Notification.EXTRA_SUB_TEXT);
         * Log.i("BlockNotification", "notificationTitle:"+notificationTitle);
         * Log.i("BlockNotification", "notificationText:"+notificationText);
         * Log.i("BlockNotification", "notificationSubText:"+notificationSubText);
         * Log.i("BlockNotification",
         * "notificationLargeIcon is null:"+(notificationLargeIcon == null));
         * Log.i("BlockNotification",
         * "notificationSmallIcon is null:"+(notificationSmallIcon == null));
         */
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        updateCurrentNotifications()
        logNLS("removed...")
        logNLS("have $mCurrentNotificationsCounts active notifications")
        mRemovedNotification = sbn
    }

    private fun updateCurrentNotifications() {
        try {
            val activeNos = activeNotifications
            if (mCurrentNotifications.size == 0) {
                mCurrentNotifications.add(null)
            }
            mCurrentNotifications[0] = activeNos
            mCurrentNotificationsCounts = activeNos.size
        } catch (e: Exception) {
            logNLS("Should not be here!!")
            e.printStackTrace()
        }
    }









/*    override fun onBind(intent: Intent?): IBinder? {
        return super.onBind(intent)
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        Log.i("BlockNotification", "Notification Removed")
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        super.onNotificationPosted(sbn)
        Log.i("BlockNotification", "Notification arrived")

    }*/

}