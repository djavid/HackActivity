package com.djavid.hackactivity.data

import io.reactivex.Single
import retrofit2.http.GET

interface Api {

    @GET("genToken")
    fun generateToken(): Single<String>

    @GET("allPlaces")
    fun getAllPlaces(): Single<List<Place>>
}