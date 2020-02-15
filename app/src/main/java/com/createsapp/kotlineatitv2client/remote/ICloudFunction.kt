package com.createsapp.kotlineatitv2client.remote

import io.reactivex.Observable
import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Query

interface ICloudFunction {
    @GET("")
    fun getCustomToken(@Query("access_token") accessToken: String): Observable<ResponseBody>
}