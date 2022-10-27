package com.example.ascmessageloader

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.amity.socialcloud.sdk.AmityCoreClient
import com.amity.socialcloud.sdk.push.AmityFcm

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val selectChannel = Intent(this, SelectChannel::class.java)

        AmityCoreClient.setup(
            resources.getString(R.string.api_key)
        ).doOnComplete {
            AmityCoreClient.login(resources.getString(R.string.userId))
                .displayName(resources.getString(R.string.displayName))
                .build()
                .submit()
                .doOnComplete {
                    AmityFcm.create()
                        .setup("token")
                        .subscribe()
                    selectChannel.putExtra("userId", resources.getString(R.string.userId))
                    startActivity(selectChannel)
                }.doOnError {
                    Log.e("ERROR", "login Error")
                }
                .subscribe()
        }.doOnError {
            Log.e("ERROR", "setup Error")
        }.subscribe()
        setContentView(R.layout.activity_main)
    }
}