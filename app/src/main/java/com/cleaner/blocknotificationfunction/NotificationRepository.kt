package com.cleaner.blocknotificationfunction

import android.content.Context
import androidx.lifecycle.LiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch

class NotificationRepository {

    companion object {

        var notificationDatabase: NotificationDatabase? = null

        var notificationTableModel: LiveData<NotificationTableModel>? = null
        var notificationTableModel2: LiveData<List<NotificationTableModel>>? = null

        private fun initializeDB(context: Context): NotificationDatabase {
            return NotificationDatabase.getDataseClient(context)
        }

        fun insertData(context: Context, title: String, message: String, packageName: String, isRead: Boolean) {

            notificationDatabase = initializeDB(context)

            CoroutineScope(IO).launch {
                val notificationDetails = NotificationTableModel(title, message, packageName, isRead)
                notificationDatabase!!.notificationDao().SaveNotification(notificationDetails)
            }

        }

        fun getNotificationDetails(context: Context): LiveData<List<NotificationTableModel>> {
            notificationDatabase = initializeDB(context)
            notificationTableModel2 = notificationDatabase!!.notificationDao().getAll()
            return notificationTableModel2 as LiveData<List<NotificationTableModel>>
        }

        fun deleteAllNotification(context: Context) {
            notificationDatabase = initializeDB(context)
            notificationDatabase!!.notificationDao().deleteAll()
        }

    }
}