package com.example.ascmessageloader.chatadapter

import android.view.View
import com.amity.socialcloud.sdk.chat.channel.AmityChannel

interface ListListener {
    fun  onItemClick(chatItem: AmityChannel, position: Int, holder: View)
}