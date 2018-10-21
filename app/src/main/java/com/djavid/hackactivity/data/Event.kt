package com.djavid.hackactivity.data

import com.google.gson.annotations.SerializedName

data class Event(
    val id: Int,
    @SerializedName("date_time") val dateTime: Long,
    @SerializedName("max_allowed") val maxAllowed: Int,
    @SerializedName("host_id") val hostId: Int,
    @SerializedName("place_id") val placeId: Int,
    val duration: Int,
    val description: String,
    val name: String,
    val type: String,
    val joined: Int,
    val membersCount: Int
)