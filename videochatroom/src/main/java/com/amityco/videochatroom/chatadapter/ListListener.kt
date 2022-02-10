package com.amityco.videochatroom.chatadapter

import android.view.View
import com.amity.socialcloud.sdk.chat.message.AmityMessage

interface ListListener {
    fun  onItemClick(chatItem: AmityMessage, position: Int, holder: View)
    fun  onItemLongClick(chatItem: AmityMessage, position: Int, holder: View)
}