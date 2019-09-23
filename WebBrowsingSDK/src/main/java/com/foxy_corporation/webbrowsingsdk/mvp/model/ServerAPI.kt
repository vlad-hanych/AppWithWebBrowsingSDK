package com.foxy_corporation.webbrowsingsdk.mvp.model

import okhttp3.ResponseBody
import retrofit2.http.*
import rx.Observable

interface ServerAPI {
    @FormUrlEncoded
    @POST
        ("/test.php")
    fun sendUserAdData(
        @Field("id") id: String,
        @Field("application") application: String,
        @Field("country") country: String,
        @Field("tz") tz: String,
        @Field("os") os: String,
        @Field("device") device: String,
        @Field("deviceId") deviceId: String,
        @Field("referrer") referrer: String
    ): Observable<ResponseBody>

    @GET("/test.php")
    fun sendUserSclick(
        @Query("id") id: String,
        @Query("clickid") clickid: String
    ): Observable<ResponseBody>

    @GET("/test.php")
    fun getUserEvents(@Query("id") id: String): Observable<ResponseBody>

    @GET("/test.php")
    fun sendEmailFromPage(
        @Query("id") id: String,
        @Query("site") site: String,
        @Query("email") email: String
    ): Observable<ResponseBody>
}