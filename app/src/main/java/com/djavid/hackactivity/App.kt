package com.djavid.hackactivity

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import com.djavid.hackactivity.data.Api
import com.google.gson.GsonBuilder
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import net.danlew.android.joda.JodaTimeAndroid
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class App: Application() {

    companion object {
        private lateinit var appInstance: App
        private lateinit var api: Api
        private lateinit var sharedPreferences: SharedPreferences

        fun getAppInstance(): App {
            return appInstance
        }

        fun getContext(): Context {
            return appInstance.applicationContext
        }

        fun getApi() = api

        fun getPreferences() = sharedPreferences

    }

    override fun onCreate() {
        super.onCreate()
        appInstance = applicationContext as App

        initJoda()
        initRetrofit()
        initSharedPreferences()
        checkToken()
    }

    @SuppressLint("CheckResult")
    private fun generateToken() {
        api.generateToken()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                sharedPreferences.edit().putInt("token", it.id).apply()
            }, {
                it.printStackTrace()
            })
    }

    private fun initRetrofit() {
        val retrofit = Retrofit.Builder()
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create(GsonBuilder().create()))
            .baseUrl("https://damp-brook-17873.herokuapp.com/api/")
            .client(
                OkHttpClient.Builder()
                    .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY })
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .build()
            )
            .build()
        api = retrofit.create(Api::class.java)
    }

    private fun initJoda() {
        JodaTimeAndroid.init(this)
    }

    private fun initSharedPreferences() {
        sharedPreferences = getSharedPreferences("HackActivity", Context.MODE_PRIVATE)
    }

    private fun checkToken() {
        if (!sharedPreferences.contains("token"))
            generateToken()
    }

}