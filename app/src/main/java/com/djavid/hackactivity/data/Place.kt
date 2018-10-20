package com.djavid.hackactivity.data

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

data class Place(val id: Int, val name: String, val latitude: BigDecimal,
                 val longitude: BigDecimal,
                 val events: List<Int>,
                 @SerializedName("comming_next") val upcomingEvent: Event
) {

    data class Event(
        @SerializedName("date_time") val dateTime: String,
        @SerializedName("max_allowed") val maxAllowed: Int,
        @SerializedName("host_id") val hostId: Int,
        @SerializedName("place_id") val placeId: Int,
        val duration: Int,
        val description: String,
        val name: String,
        val type: String
    )

}