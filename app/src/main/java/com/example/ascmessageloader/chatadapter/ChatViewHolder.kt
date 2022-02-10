package com.example.ascmessageloader.chatadapter

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.amity.socialcloud.sdk.chat.channel.AmityChannel
import com.example.ascmessageloader.R

class ChatViewHolder(itemsView: View) : RecyclerView.ViewHolder(itemsView) {

    fun bind(item: AmityChannel) {
        itemView.apply {
            addMessageToView(this, item)
        }
    }

    private fun addMessageToView(view: View, item: AmityChannel) {

        view.findViewById<TextView>(R.id.chId).text = item.getChannelId()

        view.findViewById<TextView>(R.id.chName).text = item.getDisplayName()

    }

}