package com.cleaner.blocknotificationfunction

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.recyclerview.widget.RecyclerView
import com.cleaner.blocknotificationfunction.databinding.CardItemCkeckboxBinding

class NotificationAdapterList(private var recyclerList: ArrayList<AppInfo>) :
    RecyclerView.Adapter<NotificationAdapterList.ViewHolder>() {
    var context: Context? = null
    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(recyclerList[position])

    override fun getItemCount(): Int = recyclerList.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.card_item_ckeckbox, parent, false))
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private var binding: CardItemCkeckboxBinding = CardItemCkeckboxBinding.bind(view)
        fun bind(apps: AppInfo) {
            binding.apply {
                appsIcon.setImageDrawable(apps.appIcon)
                appsName.text = apps.appName
                appsCheckBox.setOnCheckedChangeListener { _: CompoundButton?, isChecked: Boolean ->
                    recyclerList[position].checked = isChecked
                }

                appsCheckBox.isChecked = allCheck
                appsCheckBox.isChecked = BlockNotificationService.isAppNotificationsEnabled(context!!, apps.packageName)
            }

        }

    }

    companion object {
        var allCheck = false
    }

}
