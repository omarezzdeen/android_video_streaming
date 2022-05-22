package com.oezzdeen.android_video_streaming.listener_event


import android.net.Uri

interface OnVideoPickedListener {
    fun onVideoPickedListener(uri: Uri?)
}

interface OnVideoDownloadedUrl {
    fun onVideoDownloadedUrl(uri: Uri?)
}