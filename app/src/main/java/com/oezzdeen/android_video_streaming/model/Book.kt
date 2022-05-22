package com.oezzdeen.android_video_streaming.model
import android.os.Parcelable
import java.util.*

@Parcelize
data class Book(
    val bookID: String = "",
    val bookName: String = "",
    val author: String = "",
    val launchYear: Date? = Date(),
    val price: Double = 0.0,
    val rating: Float = 0f,
    val videoUrl: String = ""
) : Parcelable
