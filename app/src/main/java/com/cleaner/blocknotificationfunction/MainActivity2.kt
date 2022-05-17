package com.cleaner.blocknotificationfunction

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.provider.Settings
import android.service.notification.StatusBarNotification
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity2 : AppCompatActivity() {
    private val TAG = "BlockNotification"
    private val TAG_PRE = "[" + MainActivity::class.java.simpleName + "] "
    private val EVENT_SHOW_CREATE_NOS = 0
    private val EVENT_LIST_CURRENT_NOS = 1
    private val ENABLED_NOTIFICATION_LISTENERS = "enabled_notification_listeners"
    private val ACTION_NOTIFICATION_LISTENER_SETTINGS = "android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"
    private var isEnabledNLS = false
    private var mTextView: TextView? = null
    private var btnCreateNotify: Button? = null
    private var btnClearLastNotify: Button? = null
    private var btnClearAllNotify: Button? = null
    private var btnListNotify: Button? = null
    private var btnEnableUnEnableNotify: Button? = null

    @SuppressLint("HandlerLeak")
    private val mHandler: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                EVENT_SHOW_CREATE_NOS -> showCreateNotification()
                EVENT_LIST_CURRENT_NOS -> listCurrentNotification()
                else -> {
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)


        BlockNotificationService.setAppNotificationsEnabled(this, "org.telegram.messenger", true)
        logNLS("${BlockNotificationService.isAppNotificationsEnabled(this, "org.telegram.messenger")}")


        mTextView = findViewById(R.id.textView)

        btnCreateNotify = findViewById(R.id.btnCreateNotify)
        btnClearLastNotify = findViewById(R.id.btnClearLastNotify)
        btnClearAllNotify = findViewById(R.id.btnClearAllNotify)
        btnListNotify = findViewById(R.id.btnListNotify)
        btnEnableUnEnableNotify = findViewById(R.id.btnEnableUnEnableNotify)

        btnCreateNotify!!.setOnClickListener {
            logNLS("Create notifications...")
//                createNotification(this)
            setNotif()
//            mHandler.sendMessageDelayed(mHandler.obtainMessage(EVENT_SHOW_CREATE_NOS), 50)
        }
        btnClearLastNotify!!.setOnClickListener {
            logNLS("Clear Last notification...")
            clearLastNotification()
            mHandler.sendMessageDelayed(mHandler.obtainMessage(EVENT_LIST_CURRENT_NOS), 50)
        }
        btnClearAllNotify!!.setOnClickListener {
            logNLS("Clear All notifications...")
            clearAllNotifications()
            mHandler.sendMessageDelayed(mHandler.obtainMessage(EVENT_LIST_CURRENT_NOS), 50)
        }
        btnListNotify!!.setOnClickListener {
            logNLS("List notifications...")
            listCurrentNotification()
        }
        btnEnableUnEnableNotify!!.setOnClickListener {
            logNLS("Enable/UnEnable notification...")
            openNotificationAccess()
        }
    }

    override fun onResume() {
        super.onResume()
        isEnabledNLS = isEnabled()
        logNLS("isEnabledNLS = $isEnabledNLS")
        if (!isEnabledNLS) {
            showConfirmDialog()
        }
    }

    fun buttonOnClicked(view: View) {
        mTextView!!.setTextColor(Color.BLACK)
        when (view.id) {
            R.id.btnCreateNotify -> {

            }
            R.id.btnClearLastNotify -> {

            }
            R.id.btnClearAllNotify -> {

            }
            R.id.btnListNotify -> {

            }
            R.id.btnEnableUnEnableNotify -> {

            }
            else -> {
            }
        }
    }

    private fun isEnabled(): Boolean {
        val pkgName = packageName
        val flat = Settings.Secure.getString(contentResolver, ENABLED_NOTIFICATION_LISTENERS)
        if (!TextUtils.isEmpty(flat)) {
            val names = flat.split(":").toTypedArray()
            for (i in names.indices) {
                val cn = ComponentName.unflattenFromString(names[i])
                if (cn != null) {
                    if (TextUtils.equals(pkgName, cn.packageName)) {
                        return true
                    }
                }
            }
        }
        return false
    }

/*    private fun createNotification(context: Context) {
        val manager = context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val ncBuilder: NotificationCompat.Builder = Builder(context)
        ncBuilder.setContentTitle("My Notification")
        ncBuilder.setContentText("Notification Listener Service Example")
        ncBuilder.setTicker("Notification Listener Service Example")
        ncBuilder.setSmallIcon(R.drawable.ic_launcher)
        ncBuilder.setAutoCancel(true)
        manager.notify(System.currentTimeMillis().toInt(), ncBuilder.build())
    }*/

    private fun cancelNotification(context: Context, isCancelAll: Boolean) {
        val intent = Intent()
        intent.action = BlockNotificationService.ACTION_NLS_CONTROL
        if (isCancelAll) {
            intent.putExtra("command", "cancel_all")
        } else {
            intent.putExtra("command", "cancel_last")
        }
        context.sendBroadcast(intent)
    }

    private fun getCurrentNotificationString(): String {
        var listNos = ""
        val currentNos: Array<StatusBarNotification> = BlockNotificationService.getCurrentNotifications()!!
        if (currentNos != null) {
            for (i in currentNos.indices) {
                listNos = """$i ${currentNos[i].packageName}
$listNos"""
            }
        }
        return listNos
    }

    private fun setNotif() {
        mTextView!!.text = BlockNotificationService.notificationTitle
    }

    private fun listCurrentNotification() {
        var result = ""
        if (isEnabledNLS) {
            if (BlockNotificationService.getCurrentNotifications() == null) {
                logNLS("mCurrentNotifications.get(0) is null")
                return
            }
            val n: Int = BlockNotificationService.mCurrentNotificationsCounts
            result = if (n == 0) {
                "active_notification_count_zero"
            } else {
                "active_notification_count_nonzero"
            }
            result = """
            $result
            ${getCurrentNotificationString()}
            """.trimIndent()
            mTextView!!.text = result
        } else {
            mTextView!!.setTextColor(Color.RED)
            mTextView!!.text = "Please Enable Notification Access"
        }
    }

    private fun clearLastNotification() {
        if (isEnabledNLS) {
            cancelNotification(this, false)
        } else {
            mTextView!!.setTextColor(Color.RED)
            mTextView!!.text = "Please Enable Notification Access"
        }
    }

    private fun clearAllNotifications() {
        if (isEnabledNLS) {
            cancelNotification(this, true)
        } else {
            mTextView!!.setTextColor(Color.RED)
            mTextView!!.text = "Please Enable Notification Access"
        }
    }

    private fun showCreateNotification() {
        if (BlockNotificationService.mPostedNotification != null) {
            var result: String = (BlockNotificationService.mPostedNotification!!.packageName.toString() + "\n" + BlockNotificationService.mPostedNotification!!.getTag() + "\n" + BlockNotificationService.mPostedNotification!!.getId() + "\n" + "\n" + mTextView!!.text)
            result = "Create notification:\n$result"
            mTextView!!.text = result
        }
    }

    private fun openNotificationAccess() {
        startActivity(Intent(ACTION_NOTIFICATION_LISTENER_SETTINGS))
    }

    private fun showConfirmDialog() {
        AlertDialog.Builder(this).setMessage("Please enable NotificationMonitor access").setTitle("Notification Access").setIconAttribute(android.R.attr.alertDialogIcon).setCancelable(true).setPositiveButton(android.R.string.ok) { dialog, id -> openNotificationAccess() }.setNegativeButton(android.R.string.cancel) { dialog, id ->
            // do nothing
        }.create().show()
    }

    private fun logNLS(`object`: Any) {
        Log.i(TAG, TAG_PRE + `object`)
    }
}