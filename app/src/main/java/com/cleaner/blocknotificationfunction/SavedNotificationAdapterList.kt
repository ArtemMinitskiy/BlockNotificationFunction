package com.cleaner.blocknotificationfunction

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.cleaner.blocknotificationfunction.databinding.CardItemNotificationBinding

class SavedNotificationAdapterList(private var recyclerList: ArrayList<NotificationTableModel>) :
    RecyclerView.Adapter<SavedNotificationAdapterList.ViewHolder>() {
    var context: Context? = null
    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(recyclerList[position])

    override fun getItemCount(): Int = recyclerList.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.card_item_notification, parent, false))
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private var binding: CardItemNotificationBinding = CardItemNotificationBinding.bind(view)
        fun bind(notification: NotificationTableModel) {
            binding.apply {
                try {
                    val icon: Drawable = context!!.packageManager.getApplicationIcon(notification.PackageName)
                    appsIcon.setImageDrawable(icon)
                } catch (e: PackageManager.NameNotFoundException) {
                    e.printStackTrace()
                }
                notifTitle.text = notification.Title
                notifMessage.text = notification.Message

            }

        }

    }


}
