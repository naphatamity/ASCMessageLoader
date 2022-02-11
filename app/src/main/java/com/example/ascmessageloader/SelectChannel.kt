package com.example.ascmessageloader

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.LiveDataReactiveStreams
import androidx.lifecycle.Observer
import androidx.paging.PagedList
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.amity.socialcloud.sdk.chat.AmityChatClient
import com.amity.socialcloud.sdk.chat.channel.AmityChannel
import com.amity.socialcloud.sdk.chat.channel.AmityChannelRepository
import com.example.ascmessageloader.chatadapter.ChatAdapter
import io.reactivex.schedulers.Schedulers
import com.example.ascmessageloader.chatadapter.ListListener
import io.reactivex.android.schedulers.AndroidSchedulers

class SelectChannel : AppCompatActivity(), ListListener {

    val channelRepository = AmityChatClient.newChannelRepository()
    var chatAdapter: ChatAdapter? = null

    val USER_ID = "userId"
    val CHANNEL_ID = "channelID"
    val CHANNEL_NAME ="channelNAME"
    var user_ID : String? = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_channel)
        user_ID =  intent.getStringExtra(USER_ID)
        initCreateChannel()
        initChatFragment()
        initChat(channelRepository)
    }

    private fun initCreateChannel() {
        val createCHBtn = findViewById<Button>(R.id.createBtn)
        val chNameEditText = findViewById<EditText>(R.id.chNameEditText)
        createCHBtn.setOnClickListener {
            if(chNameEditText.text.isNotEmpty()) {
                val chName = chNameEditText.text.toString()
                chNameEditText.text.clear()
                createChannel(chName)
            }else{
                Toast.makeText(this, "Please fill in Channel Name", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun initChat(messageRepository: AmityChannelRepository) {
        val channelQuery = getChannelCollection(messageRepository)
        channelQuery.observe(this, Observer {
            if (chatAdapter?.itemCount ?: Int.MAX_VALUE < it.size) {
                chatAdapter?.submitList(it)
            }
        })
    }

    private fun getChannelCollection(channelRepository: AmityChannelRepository): LiveData<PagedList<AmityChannel>> {
        return LiveDataReactiveStreams.fromPublisher(
            channelRepository.getChannels()
                .conversationType()
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

    fun createChannel(chName: String){
        channelRepository.createChannel()
            .conversationType()
            .withUserId("amity_test_01")
            .displayName(chName)
            .build()
            .create()
            .subscribe()
    }

    override fun onItemClick(chatItem: AmityChannel, position: Int, holder: View) {
        val ChatOnlyActivity = Intent(this, ChatOnlyActivity::class.java)
        ChatOnlyActivity.putExtra(USER_ID, user_ID)
        ChatOnlyActivity.putExtra(CHANNEL_ID, chatItem.getChannelId())
        ChatOnlyActivity.putExtra(CHANNEL_NAME, chatItem.getDisplayName())
        startActivity(ChatOnlyActivity)
    }
}