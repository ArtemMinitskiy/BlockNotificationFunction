package com.cleaner.blocknotificationfunction

import android.content.Context
import androidx.room.*

@Database(entities = [NotificationTableModel::class], version = 1, exportSchema = false)
abstract class NotificationDatabase : RoomDatabase() {

    abstract fun notificationDao() : DAONotification

    companion object {

        @Volatile
        private var INSTANCE: NotificationDatabase? = null

        fun getDataseClient(context: Context) : NotificationDatabase {
            if (INSTANCE != null) return INSTANCE!!
            synchronized(this) {
                INSTANCE = Room
                    .databaseBuilder(context, NotificationDatabase::class.java, "NOTIFICATION_DATABASE")
                    .fallbackToDestructiveMigration()
                    .build()

                return INSTANCE!!

            }
        }

    }

}