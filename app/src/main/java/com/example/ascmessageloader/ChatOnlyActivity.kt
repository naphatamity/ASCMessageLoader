package com.example.ascmessageloader

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.amityco.videochatroom.chatfragment.ChatHomeFragment
import org.json.JSONObject

class ChatOnlyActivity : AppCompatActivity() {

    val CHANNEL_ID = "channelID"
    val CHANNEL_NAME ="channelNAME"
    val USER_ID = "userId"

    var chID : String?=""
    var chName : String?=""
    var userID : String?=""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_only)
        userID =  intent.getStringExtra(USER_ID)
        chID =  intent.getStringExtra(CHANNEL_ID)
        chName =  intent.getStringExtra(CHANNEL_NAME)
        findViewById<TextView>(R.id.joinedChName).text = chName
        initFragment(savedInstanceState)
    }

    private fun initFragment(savedInstanceState: Bundle?) {
        val channelObject = JSONObject()
        channelObject.put(CHANNEL_ID, chID)
        channelObject.put(USER_ID, userID)

        if (savedInstanceState == null) {
            val chatFrag = newInstance(channelObject, ChatHomeFragment())
            addFragment(chatFrag, com.amityco.videochatroom.R.id.chatFragment)
        }

    }

    private fun newInstance(channelObject: JSONObject, frag: Fragment): Fragment {
        val args = Bundle()
        args.putString(CHANNEL_ID, channelObject.getString(CHANNEL_ID))
        frag.arguments = args
        return frag
    }

    private fun addFragment(fragment: Fragment, viewID: Int) {
        supportFragmentManager
            .beginTransaction()
            .replace(
                viewID,
                fragment,
                fragment.javaClass.simpleName
            )
            .commit()
    }

}