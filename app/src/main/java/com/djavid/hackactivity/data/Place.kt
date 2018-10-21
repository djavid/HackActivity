package com.djavid.hackactivity.data

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

data class Place(val id: Int, val name: String, val latitude: BigDecimal,
                 val longitude: BigDecimal,
                 val events: List<Int>,
                 val types: List<String>,
                 @SerializedName("comming_next") val upcomingEvent: Event?
)