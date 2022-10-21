package com.example.ascmessageloader.chatadapter

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.amity.socialcloud.sdk.AmityCoreClient
import com.amity.socialcloud.sdk.chat.channel.AmityChannel
import com.example.ascmessageloader.R

class ChatViewHolder(itemsView: View) : RecyclerView.ViewHolder(itemsView) {
    var userRepository = AmityCoreClient.newUserRepository()

    fun bind(item: AmityChannel) {
        itemView.apply {
            addMessageToView(this, item)
        }
    }

    private fun addMessageToView(view: View, item: AmityChannel) {
        view.findViewById<TextView>(R.id.chId).text = item.getDisplayName()
        item.getMetadata()?.let { metadataObject ->
            if(metadataObject.getAsJsonArray("USER_IDS").size() > 1) {
                metadataObject.getAsJsonArray("USER_IDS").map {
                    if (!it.asString.equals(
                            userRepository.getCurrentUser().blockingFirst().getDisplayName()
                        )
                    )
                        view.findViewById<TextView>(R.id.chId).text = it.asString
                }
            }
        }
    }
}