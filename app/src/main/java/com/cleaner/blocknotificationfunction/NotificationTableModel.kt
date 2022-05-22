package com.cleaner.blocknotificationfunction

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Notification")
data class NotificationTableModel(

    @ColumnInfo(name = "title")
    var Title: String,

    @ColumnInfo(name = "message")
    var Message: String,

    @ColumnInfo(name = "package_name")
    var PackageName: String,

    @ColumnInfo(name = "is_read")
    var IsRead: Boolean

) {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    var id: Int? = null

}