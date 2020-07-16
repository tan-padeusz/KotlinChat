package com.mygamecompany.kotlinchat.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mygamecompany.kotlinchat.BR
import com.mygamecompany.kotlinchat.data.ChatRoom
import com.mygamecompany.kotlinchat.databinding.ChatRoomItemBinding

class ChatRoomAdapter(private val clickListener: ChatRoomClickListener): ListAdapter<ChatRoom, ChatRoomDataBindingViewHolder>(ChatRoomDiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatRoomDataBindingViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ChatRoomItemBinding.inflate(inflater, parent, false)
        return ChatRoomDataBindingViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChatRoomDataBindingViewHolder, position: Int) {
        return holder.bind(position, getItem(position), clickListener)
    }
}

class ChatRoomDataBindingViewHolder(private val binding: ViewDataBinding): RecyclerView.ViewHolder(binding.root) {
    fun bind(position: Int, chatRoom: ChatRoom, clickListener: ChatRoomClickListener) {
        binding.setVariable(BR.position, position)
        binding.setVariable(BR.chatRoomItem, chatRoom)
        binding.executePendingBindings()
        binding.setVariable(BR.itemClickListener, clickListener)
    }
}

class ChatRoomDiffCallback: DiffUtil.ItemCallback<ChatRoom>() {
    override fun areItemsTheSame(oldItem: ChatRoom, newItem: ChatRoom): Boolean {
        return oldItem.address == newItem.address && oldItem.roomName == newItem.roomName
    }

    override fun areContentsTheSame(oldItem: ChatRoom, newItem: ChatRoom): Boolean {
        return areItemsTheSame(oldItem, newItem)
    }
}

class ChatRoomClickListener(val clickListener: (position: Int) -> Unit) {
    fun onClick(position: Int) = clickListener(position)
}