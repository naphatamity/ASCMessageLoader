package com.example.ascmessageloader

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.amity.socialcloud.sdk.AmityCoreClient
import com.amity.socialcloud.sdk.AmityRegionalEndpoint

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val selectChannel = Intent(this, SelectChannel::class.java)

        AmityCoreClient.setup(
            resources.getString(R.string.api_key)
        )
        setContentView(R.layout.activity_main)

        AmityCoreClient.login("USERID_VALUE")
            .displayName("DISPLAYNAME")
            .build()
            .submit()
            .doOnComplete {
                selectChannel.putExtra("USERID", "USERID_VALUE")
                startActivity(selectChannel)
            }
            .subscribe {
            }
    }
}