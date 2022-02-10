package com.amityco.videochatroom.chatadapter

import butterknife.Setter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.amity.socialcloud.sdk.chat.message.AmityMessage
import com.amity.socialcloud.sdk.extension.adapter.AmityMessageAdapter
import com.amityco.videochatroom.R

class ChatAdapter(
    private val listener: ListListener
) : ListAdapter<AmityMessage,RecyclerView.ViewHolder>(object : DiffUtil.ItemCallback<AmityMessage>() {

    override fun areItemsTheSame(oldItem: AmityMessage, newItem: AmityMessage): Boolean {
        return oldItem.getMessageId() == newItem.getMessageId()
    }

    override fun areContentsTheSame(oldItem: AmityMessage, newItem: AmityMessage): Boolean {
        return oldItem.getState() == newItem.getState()
                && oldItem.getData() == newItem.getData()
                && oldItem.getFlagCount() == newItem.getFlagCount()
                && oldItem.getReactionCount() == newItem.getReactionCount()
                && oldItem.getUser() == newItem.getUser()
    }
}) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ChatViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_chat_list, parent, false)
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = getItem(position)
        val visibility = Setter { view: View, value: Int?, index: Int -> view.visibility = value!! }
        if (message == null) {
            renderLoadingItem(holder as ChatViewHolder, visibility, position)
        } else {
            (holder as ChatViewHolder).bind(message)
            addOnClickListener(message, holder, position)
        }
    }

    private fun renderLoadingItem(
        holder: ChatViewHolder,
        visibility: Setter<View, Int?>,
        position: Int
    ) {
        holder.itemView.findViewById<TextView>(R.id.usernameMessageAppTextView).text =
            String.format("loading")
        holder.itemView.findViewById<TextView>(R.id.chatTextMessageAppTextView).text =
            String.format("loading")
    }

    private fun addOnClickListener(
        message: AmityMessage,
        holder: RecyclerView.ViewHolder,
        position: Int
    ) {
        if (!message.isDeleted()) {
            holder.itemView
                .setOnClickListener {
                    listener.onItemClick(
                        message,
                        holder.adapterPosition,
                        holder.itemView
                    )
                }

            holder.itemView.setOnLongClickListener {
                listener.onItemLongClick(
                    message,
                    holder.adapterPosition,
                    holder.itemView
                )
                false
            }
        }
    }
}