package com.cleaner.blocknotificationfunction

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.cleaner.blocknotificationfunction.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var appsList: ArrayList<AppInfo>
//    private val EVENT_LIST_CURRENT_NOS = 1
//    private val ENABLED_NOTIFICATION_LISTENERS = "enabled_notification_listeners"
//    private var isEnabledNLS = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Log.i("BlockNotification", "onCreate()")

        appsList = ArrayList()
        appsList = getAppsListInfo(this)
//org.telegram.messenger
        initScreen()
//        isEnabledUtilsAppUsage = isEnabled(this)
        Log.i("BlockNotification", "${isNotificationListenerServiceEnabled(this)}")
//        binding.textView.text = isNotificationListenerServiceEnabled(this).toString()
//        val pm = packageManager
//        if (!isNotificationListenerServiceEnabled(this)) {
//            pm.setComponentEnabledSetting(ComponentName(this, BlockNotificationService::class.java), PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP)
//            Log.i("BlockNotification", "Enabled")
//            binding.textView.text = isNotificationListenerServiceEnabled(this).toString()
//        }

    }

//    private fun toggleNotificationListenerService() {
//        val pm = packageManager
//        pm.setComponentEnabledSetting(ComponentName(this, BlockNotificationService::class.java), PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP)
//        pm.setComponentEnabledSetting(ComponentName(this, BlockNotificationService::class.java), PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP)
//    }

    private fun initScreen() {
        binding.apply {
            notificationsRecyclerView.setHasFixedSize(true)
            val adapter = NotificationAdapterList(appsList)
            notificationsRecyclerView.layoutManager = LinearLayoutManager(applicationContext, LinearLayoutManager.VERTICAL, false)
            notificationsRecyclerView.adapter = adapter

            notificationsAllCheckBox.setOnClickListener {
                if (notificationsAllCheckBox.isChecked) {
                    for (app in appsList) {
                        app.checked = true
                    }
                    NotificationAdapterList.allCheck = true
                } else {
                    for (app in appsList) {
                        app.checked = false
                    }
                    NotificationAdapterList.allCheck = false
                }
                adapter.notifyDataSetChanged()
            }

            hideNotificationBtn.setOnClickListener {
//                appsList.forEach {
//                    if (it.checked) Log.i("BlockNotification", it.packageName)
//                }
                saveAppEnabledNotificationStatus(appsList)
            }

        }
    }

    private fun isNotificationListenerServiceEnabled(context: Context): Boolean {
        val packageNames = NotificationManagerCompat.getEnabledListenerPackages(context)
        return packageNames.contains(context.packageName)
    }

    private fun saveAppEnabledNotificationStatus(appsList: ArrayList<AppInfo>) {
        appsList.forEach {
            if (it.checked) {
                Log.i("BlockNotification", "checked: " + it.packageName)
                BlockNotificationService.setAppNotificationsEnabled(this, it.packageName, true)
            } else {
                Log.i("BlockNotification", "unchecked: " + it.packageName)
                BlockNotificationService.setAppNotificationsEnabled(this, it.packageName, false)
            }
        }
    }

    @SuppressLint("QueryPermissionsNeeded")
    private fun getListOfAppsInfo(activity: Activity): MutableList<ApplicationInfo> {
        val appForReturnedList = mutableListOf<ApplicationInfo>()
        val appsInfoList: MutableList<ApplicationInfo> = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            activity.packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
        } else {
            val intent = Intent(Intent.ACTION_MAIN, null)
            intent.addCategory(Intent.CATEGORY_LAUNCHER)
            val arrayAppsNew = mutableListOf<ApplicationInfo>()
            val listAppsReturned = activity.packageManager.queryIntentActivities(intent, 0)
            listAppsReturned.forEach {
                arrayAppsNew.add(it.activityInfo.applicationInfo)
            }
            arrayAppsNew
        }
        val appsInstalled: MutableList<ApplicationInfo> = mutableListOf()
        val appsSystem: MutableList<ApplicationInfo> = mutableListOf()
        (appsInfoList.indices).forEach { i ->
            if (appsInfoList[i].packageName != activity.packageName) {
                if (appsInfoList[i].flags and ApplicationInfo.FLAG_SYSTEM == 1) {
                    appsSystem.add(appsInfoList[i])
                } else {
                    appsInstalled.add(appsInfoList[i])
                }
            }
        }

        appForReturnedList.addAll(appsInstalled)

        return appForReturnedList
    }

    fun getAppsListInfo(activity: Activity): ArrayList<AppInfo> {
        val appsArrayList: ArrayList<AppInfo> = ArrayList()

        for (app in getListOfAppsInfo(this)) {
            appsArrayList.add(AppInfo(app.loadLabel(activity.packageManager).toString(), app.loadIcon(activity.packageManager), app.packageName, false))
//            Log.i("BlockNotification", app.packageName)

        }

        return appsArrayList
    }

    fun isEnabled(context: Context): Boolean = NotificationManagerCompat.getEnabledListenerPackages(context).contains(context.packageName)
    fun requestPermission(context: Context) {
        context.startActivity(Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"))
    }

    override fun onResume() {
        super.onResume()
//        isEnabledNLS = isEnabled()
        if (!isEnabled(this)) {
            requestPermission(this)
        }

    }

/*    private var isEnabledUtilsAppUsage = false
    @SuppressLint("HandlerLeak")
    private val mHandler: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                EVENT_LIST_CURRENT_NOS -> listCurrentNotification()
                else -> {
                }
            }
        }
    }
    *//*btnClearLastNotify!!.setOnClickListener {
        logNLS("Clear Last notification...")
        clearLastNotification()
        mHandler.sendMessageDelayed(mHandler.obtainMessage(EVENT_LIST_CURRENT_NOS), 50)
    }*//*
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
    private fun listCurrentNotification() {
        var result = ""
        if (isEnabledNLS) {
            if (BlockNotificationService.getCurrentNotifications() == null) {
                Log.i("BlockNotification", "mCurrentNotifications.get(0) is null")
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
            Log.i("BlockNotification", result)
        } else {
            Log.i("BlockNotification", "Please Enable Notification Access")
        }
    }*/
}
