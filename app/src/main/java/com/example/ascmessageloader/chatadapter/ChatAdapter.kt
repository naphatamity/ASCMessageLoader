package com.example.ascmessageloader.chatadapter

import butterknife.Setter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.amity.socialcloud.sdk.chat.channel.AmityChannel
import com.amity.socialcloud.sdk.extension.adapter.AmityChannelAdapter
import com.example.ascmessageloader.R

class ChatAdapter(
    private val listener: ListListener
) : AmityChannelAdapter<RecyclerView.ViewHolder>(object :
    DiffUtil.ItemCallback<AmityChannel>() {

    override fun areItemsTheSame(oldItem: AmityChannel, newItem: AmityChannel): Boolean {
        return oldItem.getChannelId() == newItem.getChannelId()
    }

    override fun areContentsTheSame(oldItem: AmityChannel, newItem: AmityChannel): Boolean {
        return oldItem.getChannelId() == newItem.getChannelId()
                && oldItem.getCreatedAt() == newItem.getCreatedAt()
                && oldItem.getDisplayName() == newItem.getDisplayName()
                && oldItem.getMemberCount() == newItem.getMemberCount()
    }
}) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ChatViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.channel_list, parent, false)
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
        holder.itemView.findViewById<TextView>(R.id.chId).text =
            String.format("loading")
    }

    private fun addOnClickListener(
        message: AmityChannel,
        holder: RecyclerView.ViewHolder,
        position: Int
    ) {

        holder.itemView
            .setOnClickListener {
                listener.onItemClick(
                    message,
                    holder.adapterPosition,
                    holder.itemView
                )
            }
    }
}