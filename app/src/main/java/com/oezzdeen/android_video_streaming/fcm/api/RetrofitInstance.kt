package com.oezzdeen.android_video_streaming.fcm.api
import com.dev_fawzi.cc_assignment4.utils.Utils
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {
    val api: NotificationAPI by lazy {
        Retrofit.Builder()
            .baseUrl(Utils.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create()) // To parse json to pojo
            .build() // until here; We made Retrofit Instance
            .create(NotificationAPI::class.java) // here to create and specify the class of our api
    }
}