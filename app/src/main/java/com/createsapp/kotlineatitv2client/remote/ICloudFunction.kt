package com.createsapp.kotlineatitv2client.remote

import com.createsapp.kotlineatitv2client.model.BraintreeToken
import com.createsapp.kotlineatitv2client.model.BraintreeTransaction
import io.reactivex.Observable
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST

interface ICloudFunction {
    @GET("token")
    fun getToken(): Observable<BraintreeToken>

    @POST("checkout")
    @FormUrlEncoded
    fun submitPayment(
        @Field("amount") amount: Double,
        @Field("payment_method_nonce") nonce: String
    ): Observable<BraintreeTransaction>
}