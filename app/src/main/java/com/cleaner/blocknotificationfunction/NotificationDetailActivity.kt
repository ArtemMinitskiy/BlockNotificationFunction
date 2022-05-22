package com.cleaner.blocknotificationfunction

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.cleaner.blocknotificationfunction.databinding.ActivityNotificationDetailBinding
import kotlinx.coroutines.*

class NotificationDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityNotificationDetailBinding
    lateinit var notificationViewModel: NotificationViewModel
    private lateinit var notificationList: ArrayList<NotificationTableModel>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotificationDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        notificationViewModel = ViewModelProvider(this).get(NotificationViewModel::class.java)
        notificationList = ArrayList()

        notificationViewModel.getNotificationDetails(this)!!.observe(this) {
            if (it == null) {
                println("Data Not Found")
            } else {
                it.forEach {
                    notificationList.add(it)
                    println("$it \n")
                }
            }
        }

        binding.getBtn.setOnClickListener {
            binding.apply {
                notificationsRecyclerView.setHasFixedSize(true)
                val adapter = SavedNotificationAdapterList(notificationList)
                notificationsRecyclerView.layoutManager = LinearLayoutManager(applicationContext, LinearLayoutManager.VERTICAL, false)
                notificationsRecyclerView.adapter = adapter
            }
        }

    }

    private val scope = CoroutineScope(Job() + Dispatchers.IO)
    private suspend fun delete() {
        coroutineScope {
            notificationViewModel.deleteAllNotificationDetails(this@NotificationDetailActivity)

        }

    }

    fun initialize() {
        scope.launch {
            delete()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        initialize()
        BlockNotificationService.countLockedNotification = 0

    }
}