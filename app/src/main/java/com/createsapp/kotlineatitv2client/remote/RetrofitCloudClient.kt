package com.createsapp.kotlineatitv2client.remote


import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitCloudClient {
    private var instances: Retrofit?=null

    fun getInstance():Retrofit {
        if (instances == null)
            instances = Retrofit.Builder()
                .baseUrl("https://us-central1-androideatit-58044.cloudfunctions.net/")
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build()
        return instances!!
    }
}