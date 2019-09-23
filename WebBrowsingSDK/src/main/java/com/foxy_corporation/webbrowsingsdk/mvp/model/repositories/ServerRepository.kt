package com.foxy_corporation.webbrowsingsdk.mvp.model.repositories

import com.foxy_corporation.webbrowsingsdk.App
import com.foxy_corporation.webbrowsingsdk.mvp.model.ServerAPI
import okhttp3.ResponseBody
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import javax.inject.Inject

class ServerRepository {
    @Inject
    lateinit var serverAPI: ServerAPI

    init {
        App.instance.appComponent.inject(this@ServerRepository)
    }

    fun launchSendingUserAdData(id: String, application: String, coutry: String, tz: String, os: String, device: String, deviceId: String, referrer: String): Observable<ResponseBody> {
        return serverAPI.sendUserAdData(id, application, coutry, tz, os, device, deviceId, referrer)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
    }

    fun launchSendingUserSclick(id: String, sclick: String): Observable<ResponseBody> {
        return serverAPI.sendUserSclick(id, sclick)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
    }

    fun launchGettingUserEvets(id: String): Observable<ResponseBody> {
        return serverAPI.getUserEvents(id)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
    }

    fun launchSendingEmailFromPage(id: String, site: String, email: String): Observable<ResponseBody> {
        return serverAPI.sendEmailFromPage(id, site, email)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
    }
}