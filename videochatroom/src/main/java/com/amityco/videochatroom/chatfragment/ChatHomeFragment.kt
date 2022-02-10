package com.amityco.videochatroom.chatfragment

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.LiveDataReactiveStreams
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.paging.PagedList
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.amity.socialcloud.sdk.AmityCoreClient
import com.amity.socialcloud.sdk.chat.AmityChatClient
import com.amity.socialcloud.sdk.chat.AmityMessageRepository
import com.amity.socialcloud.sdk.chat.message.AmityMessage
import com.amity.socialcloud.sdk.chat.message.AmityMessageLoader
import com.squareup.picasso.OkHttp3Downloader
import com.squareup.picasso.Picasso
import com.amityco.videochatroom.ChatReaction
import com.amityco.videochatroom.R
import com.amityco.videochatroom.chatadapter.ChatAdapter
import com.amityco.videochatroom.chatadapter.ListListener
import com.ekoapp.ekosdk.message.flag.AmityMessageFlagger
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

class ChatHomeFragment() : Fragment(R.layout.chat_list), ListListener {
    private var channelID = ""
    private var userID = ""
    var chatAdapter: ChatAdapter? = null
    private lateinit var messageQuery: Flowable<List<AmityMessage>>
    private lateinit var channelDisposable: Disposable
    lateinit var amityDisposable: Disposable

    companion object {
        const val PROFILE_IMAGE = "profileImage"
        const val CHANNEL_ID_ARG_KEY = "channelID"
        const val USER_ID = "userId"
        const val HIDE_KEY_BOARD = 0
        const val POSITION = 1
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        this.channelID = arguments?.getString(CHANNEL_ID_ARG_KEY) ?: ""
        initChatChannel()
    }

    private fun initChatChannel() {
        val sharedPref = requireActivity().getPreferences(AppCompatActivity.MODE_PRIVATE) ?: return
        val uuid = sharedPref.getString(USER_ID, null)
        if (uuid == null) {
            amityDisposable = AmityCoreClient.getCurrentUser().subscribe {
                with(sharedPref.edit()) {
                    putString(PROFILE_IMAGE, it.getAvatar()?.getUrl())
                    apply()
                }
                with(sharedPref.edit()) {
                    putString(USER_ID, it.getUserId())
                    apply()
                }
                userID = it.getUserId()
            }
        }
        initChatFragment()
        initJoinChannel()
    }

    private fun initJoinChannel(){
        val channelRepository = AmityChatClient.newChannelRepository()
        val messageRepository = AmityChatClient.newMessageRepository()
        channelRepository.joinChannel(channelID)
            .doOnError {
                Log.e("ERROR", it.message.toString())
            }
            .subscribe().run {
                channelDisposable = this
            }

        if (!channelDisposable.isDisposed) {
            initChatInput(messageRepository)
            initChat(messageRepository)
        }
    }

    private fun initChat(messageRepository: AmityMessageRepository) {
        messageQuery = getMessageCollection(messageRepository)
        messageQuery.subscribe{
            Log.e("LIST",it.toString())
            chatAdapter?.submitList(it)
        }
    }

    private fun getMessageCollection(messageRepository: AmityMessageRepository): Flowable<List<AmityMessage>> {
        return  messageRepository.getMessages("63e8cb57-964f-4140-b413-1a67f0fdd652")
            .parentId(null)
            .build()
            .loader()
            .getResult()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnNext {
                Log.e("LIST",it.toString())
            }
            .doOnError {
                Log.e("ERROR", it.message.toString())
            }
    }

    override fun onPause() {
        super.onPause()
        if (!channelDisposable.isDisposed) {
            channelDisposable.dispose()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (!channelDisposable.isDisposed) {
            channelDisposable.dispose()
        }
    }

    private fun initChatFragment() {
        val recycler = requireView().findViewById<RecyclerView>(R.id.content_recycler)
        chatAdapter = ChatAdapter(this@ChatHomeFragment)
        recycler.apply {
            layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
            isNestedScrollingEnabled = false
            adapter = chatAdapter
            onFlingListener = null
        }
        addOnScroll(recycler)
    }

    private fun addOnScroll(recycler: RecyclerView) {
        val bottomBtn = requireView().findViewById<Button>(R.id.bottomBtn)
        bottomBtn.setOnClickListener {
            chatAdapter?.itemCount?.minus(POSITION)
                ?.let { it -> recycler.scrollToPosition(it) }
            bottomBtn.visibility = View.GONE
        }

        chatAdapter?.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                super.onItemRangeInserted(positionStart, itemCount)
                val lastPosition = chatAdapter?.itemCount?.minus(POSITION)
                if (positionStart == lastPosition) {
                    recycler.scrollToPosition(lastPosition)
                }
            }
        })

        recycler.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            var scroll_state = 0

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                scroll_state = newState
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (scroll_state > 0 && chatAdapter != null) {
                    val visibleItemCount =
                        (recycler.layoutManager as LinearLayoutManager).childCount
                    val positionView =
                        (recycler.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
                    chatAdapter?.itemCount?.minus(visibleItemCount + positionView).let {
                        it?.let {
                            if (it > 0) {
                                bottomBtn.visibility = View.VISIBLE
                            } else {
                                bottomBtn.visibility = View.GONE
                            }
                        }
                    }
                }
            }
        })
    }

    private fun initChatInput(messageRepository: AmityMessageRepository) {
        addProfileImage()
        addBtnCickListener(messageRepository)
    }

    private fun addProfileImage() {
        val profileImage = requireActivity().findViewById<ImageView>(R.id.profileDarkImageView)
        val sharedPref = requireActivity().getPreferences(Context.MODE_PRIVATE) ?: return
        val profileUrl = sharedPref.getString(PROFILE_IMAGE, null)
    }

    private fun addBtnCickListener(messageRepository: AmityMessageRepository) {
        val chatEditText = requireActivity().findViewById<EditText>(R.id.messageDarkTextview)
        val sendBtn = requireActivity().findViewById<ImageButton>(R.id.sendChatImageView)
        sendBtn.setOnClickListener {
            if (chatEditText.text.isNotBlank()) {
                val imm =
                    requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                imm?.hideSoftInputFromWindow(
                    view?.windowToken,
                    HIDE_KEY_BOARD
                )
                messageRepository.createMessage(channelID)
                    .with()
                    .text(chatEditText.text.toString())
                    .build()
                    .send()
                    .subscribe()
            }
            chatEditText.setText("")
        }
    }

    override fun onItemClick(chatItem: AmityMessage, position: Int, holder: View) {
        val inflater = LayoutInflater.from(requireContext())
        val view = inflater.inflate(R.layout.popup_window_message_reaction, null)

        val popup = PopupWindow(
            view,
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT,
            false,
        )
        popup.isOutsideTouchable = true
        val holderLayout = holder.findViewById<ConstraintLayout>(R.id.mainItemChatLinearLayout)
        val xDimen = holderLayout.x.toInt()
        val yDimen = holderLayout.bottom

        popup.showAsDropDown(holder, xDimen * 2, -1 * yDimen)

        initialLoveButton(chatItem, view)
        initialSmileButton(chatItem, view)
        initialButton(chatItem, view)
        initialHeartEyeButton(chatItem, view)
        initialFireButton(chatItem, view)
    }

    override fun onItemLongClick(chatItem: AmityMessage, position: Int, holder: View) {
        onAlertDialog(chatItem, holder)
    }

    private fun initialLoveButton(chatItem: AmityMessage, view: View) {
        val loveBtn = view.findViewById<TextView>(R.id.sendLoveEmoji)
        loveBtn.setOnClickListener {
            chatItem.react()
                .addReaction(ChatReaction.LOVE)
                .subscribe()
            view.visibility = View.GONE
        }
    }

    private fun initialSmileButton(chatItem: AmityMessage, view: View) {
        val smileBtn = view.findViewById<TextView>(R.id.sendSmileEmoji)
        smileBtn.setOnClickListener {
            chatItem.react()
                .addReaction(ChatReaction.SMILE)
                .subscribe()
            view.visibility = View.GONE
        }
    }

    private fun initialButton(chatItem: AmityMessage, view: View) {
        val lolBtn = view.findViewById<TextView>(R.id.sendlolEmoji)
        lolBtn.setOnClickListener {
            chatItem.react()
                .addReaction(ChatReaction.LOL)
                .subscribe()
            view.visibility = View.GONE
        }
    }

    private fun initialHeartEyeButton(chatItem: AmityMessage, view: View) {
        val heartEyeBtn = view.findViewById<TextView>(R.id.sendHeartEyeEmoji)
        heartEyeBtn.setOnClickListener {
            chatItem.react()
                .addReaction(ChatReaction.HEARTEYE)
                .subscribe()
            view.visibility = View.GONE
        }
    }

    private fun initialFireButton(chatItem: AmityMessage, view: View) {
        val fireBtn = view.findViewById<TextView>(R.id.sendFiremoji)
        fireBtn.setOnClickListener {
            chatItem.react()
                .addReaction(ChatReaction.FIRE)
                .subscribe()
            view.visibility = View.GONE
        }
    }

    operator fun invoke(): Fragment {
        return this
    }

    private fun onAlertDialog(chatItem: AmityMessage, view: View) {
        val builder = AlertDialog.Builder(view.context, R.style.AlertDialogCustom)
        builder.setTitle((resources.getString(R.string.report_title)))
        builder.setMessage((resources.getString(R.string.report_description)))
        builder.setPositiveButton(
            resources.getString(R.string.ok)
        ) { dialog, id ->
            val flagger: AmityMessageFlagger = chatItem.report()
            flagger.flag()
                .doOnComplete { dialog.dismiss() }
                .subscribe()

         /*   val userFlagger: EkoUserFlagger? = chatItem.getUser()?.report()
            userFlagger?.flag()
                ?.doOnComplete { dialog.dismiss() }
                ?.subscribe()*/
        }
        builder.setNegativeButton(
            resources.getString(R.string.cancel)
        ) { dialog, id ->
            dialog.dismiss()
        }
        builder.show()
    }

}