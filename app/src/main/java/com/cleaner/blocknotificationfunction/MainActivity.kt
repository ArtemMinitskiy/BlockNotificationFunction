package com.cleaner.blocknotificationfunction

import android.annotation.SuppressLint
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.RemoteViews
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.cleaner.blocknotificationfunction.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var appsList: ArrayList<AppInfo>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
            var count = 0
            var list : ArrayList<String> = ArrayList()
            hideNotificationBtn.setOnClickListener {
                saveAppEnabledNotificationStatus(appsList)

            }


            textView.setOnClickListener {
                count++
                list.add(count.toString())
                sendNotification(count, "Telegram", list)

            }

        }
        val notificationManagerCompat = NotificationManagerCompat.from(this)
        notificationManagerCompat.cancelAll()
        BlockNotificationService.countLockedNotification = 0
        BlockNotificationService.list.clear()
    }

    private val CHANNEL_ID = "PRO_CLEANER"
    private fun sendNotification(count: Int, bigContent: String, list: ArrayList<String>) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationName = resources.getString(R.string.app_name)
            val notificationImportance = NotificationManager.IMPORTANCE_DEFAULT
            val notificationChannel = NotificationChannel(CHANNEL_ID, notificationName, notificationImportance)
            val notificationManager: NotificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(notificationChannel)
        }
//        val icon = BitmapFactory.decodeResource(resources, R.drawable.ic_launcher_background)

        val intent = Intent(this, MainActivity::class.java)
        val remoteViews = RemoteViews(packageName, R.layout.notification_save_badge)
        remoteViews.setTextViewText(R.id.count, BlockNotificationService.countLockedNotification.toString())
        val pendingIntent = PendingIntent.getActivity(this, 30, intent, 0)
        remoteViews.setOnClickPendingIntent(R.id.count, pendingIntent)

        val notification = NotificationCompat.Builder(this@MainActivity, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_background)
//            .setLargeIcon(icon)
            .setContentTitle("Saved notification")
            .setContentIntent(pendingIntent)
//            .setContentText("setNotificationBody(context, CHANNEL_ID)")
//            .setGroup("group")
//            .setContent(remoteViews)
//            .setGroup(CHANNEL_ID)
            .setAutoCancel(false)
        val inboxStyle = NotificationCompat.InboxStyle()
        inboxStyle.setBigContentTitle(bigContent)
        list.forEach {
            inboxStyle.addLine("$bigContent:  $it")

        }
//        inboxStyle.addLine("Messgare $count")

        notification.setStyle(inboxStyle);

        val mangerCompat = NotificationManagerCompat.from(this@MainActivity)
        mangerCompat.notify(1, notification.build())
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

    private fun getAppsListInfo(activity: Activity): ArrayList<AppInfo> {
        val appsArrayList: ArrayList<AppInfo> = ArrayList()

        for (app in getListOfAppsInfo(this)) {
            appsArrayList.add(AppInfo(app.loadLabel(activity.packageManager).toString(), app.loadIcon(activity.packageManager), app.packageName, false))
        }

        return appsArrayList
    }

    private fun isEnabled(context: Context): Boolean = NotificationManagerCompat.getEnabledListenerPackages(context).contains(context.packageName)
    private fun requestPermission(context: Context) {
        context.startActivity(Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"))
    }

    override fun onResume() {
        super.onResume()
        if (!isEnabled(this)) {
            requestPermission(this)
        }

    }

}
