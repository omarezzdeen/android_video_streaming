package com.oezzdeen.android_video_streaming.ui.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import com.dev_fawzi.cc_assignment4.utils.Utils
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import com.oezzdeen.android_video_streaming.R
import com.oezzdeen.android_video_streaming.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        /*----------------------------------*/

//        setSupportActionBar(binding.ToolBar)
        supportActionBar?.title = getString(R.string.my_library)

        /*----------------------------------*/

        Firebase.messaging.subscribeToTopic(Utils.TOPIC)

        /*----------------------------------*/
    }
}