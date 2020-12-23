package com.mygamecompany.kotlinchat.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.mygamecompany.kotlinchat.R
import com.mygamecompany.kotlinchat.data.ChatRoom

class ChatRoomAdapter(context: Context,
                      objects: ArrayList<out ChatRoom>): ArrayAdapter<ChatRoom>(context, 0, objects) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val room = getItem(position)
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.chat_room_item, parent, false)
        view.findViewById<TextView>(R.id.roomNameView).text = room?.roomName
        return view
    }
}