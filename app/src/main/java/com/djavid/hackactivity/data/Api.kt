package com.djavid.hackactivity.data

import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface Api {

    @GET("generateToken")
    fun generateToken(): Single<TokenResponse>

    @GET("allPlaces")
    fun getAllPlaces(@Query("token") token: Int): Single<List<Place>>

    @GET("joinEvent")
    fun joinEvent(@Query("token") token: Int, @Query("event_id") eventId: Int): Single<Int>

    @GET("placeEvents")
    fun getPlaceEvents(@Query("token") token: Int, @Query("id") id: Int): Single<List<Event>>

    @POST("hostEvent")
    fun createEvent(
        @Query("name") name: String,
        @Query("date_time") dateTime: Long,
        @Query("max_allowed") maxAllowed: Int,
        @Query("duration") duration: Int,
        @Query("description") description: String,
        @Query("type") activityType: String,
        @Query("place_id") placeId: Int,
        @Query("token") token: Int
    ): Single<Boolean>

}