package com.cleaner.blocknotificationfunction

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel

class NotificationViewModel : ViewModel() {

    var liveDataNotification2: LiveData<List<NotificationTableModel>>? = null

    fun getNotificationDetails(context: Context): LiveData<List<NotificationTableModel>>? {
        liveDataNotification2 = NotificationRepository.getNotificationDetails(context)
        return liveDataNotification2
    }

    fun deleteAllNotificationDetails(context: Context) {
        NotificationRepository.deleteAllNotification(context)
    }

}