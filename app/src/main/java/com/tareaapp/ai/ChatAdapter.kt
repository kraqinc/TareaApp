package com.tareaapp.ai

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.tareaapp.databinding.ItemChatMessageBinding

class ChatAdapter(private val messages: List<ChatMessage>) :
    RecyclerView.Adapter<ChatAdapter.ChatVH>() {

    inner class ChatVH(val binding: ItemChatMessageBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ChatVH(ItemChatMessageBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun getItemCount() = messages.size

    override fun onBindViewHolder(holder: ChatVH, position: Int) {
        val msg = messages[position]
        val binding = holder.binding
        binding.tvMessage.text = msg.content
        if (msg.role == "user") {
            binding.layoutUser.visibility = android.view.View.VISIBLE
            binding.layoutAssistant.visibility = android.view.View.GONE
            binding.tvMessageUser.text = msg.content
        } else {
            binding.layoutUser.visibility = android.view.View.GONE
            binding.layoutAssistant.visibility = android.view.View.VISIBLE
            binding.tvMessage.text = msg.content
        }
    }
}
