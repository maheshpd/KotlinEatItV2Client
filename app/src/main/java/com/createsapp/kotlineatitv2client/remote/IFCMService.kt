package com.createsapp.kotlineatitv2client.remote


import com.createsapp.kotlineatitv2client.model.FCMResponse
import com.createsapp.kotlineatitv2client.model.FCMSendData
import io.reactivex.Observable
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface IFCMService {
    @Headers(
        "Content-Type:application/json",
        "Authorization:key=AAAAyRoAKtM:APA91bG_Wa4--7oUvDEldg_SYFGabMfYP2tpCNksZfQtmviCGit1EZgZIkRjzhihwuQTqj-s1GPwOB-eEFaBCehol7v5bQAsSrONsHFPoWQGZS1iv28f82DcJONXTOKboa_LSiAyGXk7"
    )
    @POST("fcm/send")
    fun sendNotification(@Body body: FCMSendData): Observable<FCMResponse>
}