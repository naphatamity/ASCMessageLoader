package com.example.ascmessageloader

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.LiveDataReactiveStreams
import androidx.lifecycle.Observer
import androidx.paging.PagedList
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.amity.socialcloud.sdk.AmityCoreClient
import com.amity.socialcloud.sdk.chat.AmityChatClient
import com.amity.socialcloud.sdk.chat.channel.AmityChannel
import com.amity.socialcloud.sdk.chat.channel.AmityChannelFilter
import com.amity.socialcloud.sdk.chat.channel.AmityChannelRepository
import com.amity.socialcloud.sdk.core.user.AmityUser
import com.example.ascmessageloader.chatadapter.ChatAdapter
import io.reactivex.schedulers.Schedulers
import com.example.ascmessageloader.chatadapter.ListListener
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import io.reactivex.android.schedulers.AndroidSchedulers

class SelectChannel : AppCompatActivity(), ListListener {

    val channelRepository = AmityChatClient.newChannelRepository()
    var userRepository = AmityCoreClient.newUserRepository()
    var chatAdapter: ChatAdapter? = null

    val USER_ID = "userId"
    val CHANNEL_ID = "channelID"
    val CHANNEL_NAME ="channelNAME"
    var user_ID : String? = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_channel)
        user_ID =  intent.getStringExtra(USER_ID)
        initChatFragment()
        initChat(channelRepository)
    }

    private fun initChat(messageRepository: AmityChannelRepository) {
        val channelQuery = getChannelCollection(messageRepository)
        channelQuery.observe(this, Observer {
            if ((chatAdapter?.itemCount ?: Int.MAX_VALUE) < it.size) {
                chatAdapter?.submitList(it)
            }
        })
    }

    private fun getChannelCollection(channelRepository: AmityChannelRepository): LiveData<PagedList<AmityChannel>> {
        return LiveDataReactiveStreams.fromPublisher(
            channelRepository.getChannels()
                .all()
                .filter(AmityChannelFilter.MEMBER)
                .build()
                .query()
                .subscribeOn(Schedulers.io())
                .observeOn((AndroidSchedulers.mainThread()))
        )
    }

    private fun initChatFragment() {
        val recycler = findViewById<RecyclerView>(R.id.channelListRecycler)
        chatAdapter = ChatAdapter(this)
        recycler.apply {
            layoutManager = LinearLayoutManager(this.context, LinearLayoutManager.VERTICAL, false)
            isNestedScrollingEnabled = false
            adapter = chatAdapter
            onFlingListener = null
        }
    }

    fun createChannel(user: AmityUser){
        val channelChatRepository = AmityChatClient.newChannelRepository()
        val allUsers = arrayListOf<AmityUser>()
        allUsers.add(user)
        allUsers.add(AmityCoreClient.getCurrentUser().blockingFirst())

        val jsonArrayUserId = JsonArray()
        val jsonArrayDisplayName = JsonArray()

        //create meta data before create channel.
        allUsers.map { jsonArrayUserId.add(it.getUserId()) }
        allUsers.map { jsonArrayDisplayName.add(it.getUserId() + ":" + it.getDisplayName()) }
        val isDirectChat = allUsers.count() == 2


        val metaData = JsonObject()
        metaData.addProperty("CREATOR_ID", AmityCoreClient.getUserId())
        metaData.addProperty("isDirectChat", isDirectChat)
        metaData.add("USER_IDS", jsonArrayUserId)

        user.getDisplayName()?.let {
            channelChatRepository.createChannel()
                .communityType()
                .withDisplayName(it)
                .userIds(userIds = listOf(user.getUserId()))
                .metadata(metaData) // optional
                .build()
                .create()
                .doOnSuccess {  }
                .doOnError { }
                .ignoreElement()
                .subscribe()
        }
    }

    override fun onItemClick(chatItem: AmityChannel, position: Int, holder: View) {
        val chatOnlyActivity = Intent(this, ChatOnlyActivity::class.java)
        chatOnlyActivity.putExtra(USER_ID, user_ID)
        chatOnlyActivity.putExtra(CHANNEL_ID, chatItem.getChannelId())

        var chatDisplayName = chatItem.getDisplayName()
        chatItem.getMetadata()?.let { metadataObject ->
            if(metadataObject.getAsJsonArray("USER_IDS").size() > 1) {
                metadataObject.getAsJsonArray("USER_IDS").map {
                    if (!it.asString.equals(
                            userRepository.getCurrentUser().blockingFirst().getDisplayName()
                        )
                    ) chatDisplayName = it.asString
                }
            }
        }
        chatOnlyActivity.putExtra(CHANNEL_NAME, chatDisplayName)
        startActivity(chatOnlyActivity)
    }

}