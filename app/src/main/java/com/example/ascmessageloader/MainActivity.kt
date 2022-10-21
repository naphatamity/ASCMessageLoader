package com.example.ascmessageloader

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.amity.socialcloud.sdk.AmityCoreClient
import com.amity.socialcloud.sdk.AmityRegionalEndpoint
import com.amity.socialcloud.sdk.chat.AmityChatClient
import com.amity.socialcloud.sdk.chat.channel.AmityChannelRepository
import com.amity.socialcloud.sdk.core.AmityTags
import com.amity.socialcloud.sdk.core.user.AmityUser
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import io.reactivex.Completable

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val selectChannel = Intent(this, SelectChannel::class.java)

        AmityCoreClient.setup(
            resources.getString(R.string.api_key)
        )
        setContentView(R.layout.activity_main)

        AmityCoreClient.login(resources.getString(R.string.userId))
            .displayName(resources.getString(R.string.displayName))
            .build()
            .submit()
            .doOnComplete {
                selectChannel.putExtra("userId", resources.getString(R.string.userId))
                startActivity(selectChannel)
            }
            .subscribe()
    }
}