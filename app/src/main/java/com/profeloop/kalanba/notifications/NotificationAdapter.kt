package com.profeloop.kalanba.notifications

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.profeloop.kalanba.databinding.ItemNotificationBinding
import com.profeloop.kalanba.models.AppNotification
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NotificationAdapter(
    private var items: List<AppNotification>
) : RecyclerView.Adapter<NotificationAdapter.ViewHolder>() {

    fun updateData(newItems: List<AppNotification>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemNotificationBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size

    inner class ViewHolder(private val binding: ItemNotificationBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(notification: AppNotification) {
            binding.tvTitle.text = notification.titulo
            binding.tvMessage.text = notification.mensaje
            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            binding.tvTime.text = sdf.format(Date(notification.timestamp))
        }
    }
}
