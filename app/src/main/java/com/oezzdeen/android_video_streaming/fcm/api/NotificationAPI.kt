package com.oezzdeen.android_video_streaming.fcm.api
import com.dev_fawzi.cc_assignment4.utils.Utils
import com.oezzdeen.android_video_streaming.fcm.notif_model.PushNotification
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface NotificationAPI {

    @Headers("Authorization: key=${Utils.SERVER_KEY}", "Content-Type:${Utils.CONTENT_TYPE}")
    @POST(value = "fcm/send")
    suspend fun postNotification(
        @Body pushNotification: PushNotification
    ): Response<ResponseBody>
}