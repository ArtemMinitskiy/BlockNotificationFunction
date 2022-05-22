package com.cleaner.blocknotificationfunction

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface DAONotification {

    @Insert
    suspend fun SaveNotification(notificationTableModel: NotificationTableModel)

    @Query("SELECT * FROM Notification")
    fun getAll(): LiveData<List<NotificationTableModel>>

    @Query("DELETE FROM notification")
    fun deleteAll()

}