package com.cleaner.blocknotificationfunction

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.cleaner.blocknotificationfunction.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var appsList: ArrayList<AppInfo>
    private lateinit var notificationList: ArrayList<NotificationTableModel>

    lateinit var notificationViewModel: NotificationViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        notificationViewModel = ViewModelProvider(this).get(NotificationViewModel::class.java)

        notificationList = ArrayList()
        appsList = ArrayList()
        appsList = getAppsListInfo(this)

        initScreen()

    }

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
                saveAppEnabledNotificationStatus(appsList)
            }

            textView.setOnClickListener {
                startActivity(Intent(this@MainActivity, NotificationDetailActivity::class.java))
            }

        }
    }

    private fun saveAppEnabledNotificationStatus(appsList: ArrayList<AppInfo>) {
        appsList.forEach {
            if (it.checked) {
                Log.i("NotificationLog", "checked: " + it.packageName)
                BlockNotificationService.setAppNotificationsEnabled(this, it.packageName, true)
            } else {
                Log.i("NotificationLog", "unchecked: " + it.packageName)
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
        }

        return appsArrayList
    }

    fun isEnabled(context: Context): Boolean = NotificationManagerCompat.getEnabledListenerPackages(context).contains(context.packageName)
    fun requestPermission(context: Context) {
        context.startActivity(Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"))
    }

    override fun onResume() {
        super.onResume()
        if (!isEnabled(this)) {
            requestPermission(this)
        }

    }

}
