package com.foxy_corporation.webbrowsingsdk.mvp

import com.foxy_corporation.webbrowsingsdk.App
import com.foxy_corporation.webbrowsingsdk.mvp.model.repositories.ServerRepository
import com.foxy_corporation.webbrowsingsdk.mvp.model.repositories.SharedPreferencesRepository
import okhttp3.ResponseBody
import rx.Observable
import javax.inject.Inject

class DataManager {
    /*@Inject
    var serverRepository: ServerRepository? = null

    @Inject
    var sharedPreferencesRepository: SharedPreferencesRepository? = null*/

    @Inject
    lateinit var serverRepository: ServerRepository

    @Inject
    lateinit var sharedPreferencesRepository: SharedPreferencesRepository

    init {
        App.instance.appComponent.inject(this@DataManager)
    }

    fun manageGettingAlreadyLaunched(): Boolean {
        return sharedPreferencesRepository.getAlreadyLaunched()
    }

    fun manageSettingAlreadyLaunched() {
        sharedPreferencesRepository.setAlreadyLaunched()
    }

    fun manageRememberingFacebookDeeplink(fbDeeplink: String) {
        sharedPreferencesRepository.rememberFacebookDeeplink(fbDeeplink)
    }

    fun manageGettingFacebookDeeplink(): String? {
        return sharedPreferencesRepository.getFacebookDeeplink()
    }

    fun manageRememberingGooglePlayReferrer(referrer: String) {
        sharedPreferencesRepository.rememberGooglePlayReferrer(referrer)
    }

    fun manageGettingGooglePlayReferrer(): String? {
        return sharedPreferencesRepository.getGooglePlayReferrer()
    }

    fun manageSendingUserAdData(id: String, application: String, coutry: String, tz: String, os: String, device: String, deviceId: String, referrer: String): Observable<ResponseBody> {
        return serverRepository.launchSendingUserAdData(id, application, coutry, tz, os, device, deviceId, referrer)
    }

    fun manageRememberingUserId(id: String) {
        sharedPreferencesRepository.rememberId(id)
    }

    fun manageGettingUserId(): String? {
        return sharedPreferencesRepository.getUserId()
    }

    fun manageSendingUserSclick(id: String, sclick: String): Observable<ResponseBody> {
        return serverRepository.launchSendingUserSclick(id, sclick)
    }

    fun manageGettingUserEvents(id: String): Observable<ResponseBody> {
        return serverRepository.launchGettingUserEvets(id)
    }

    fun manageSendingEmailFromPage(id: String, site: String, email: String): Observable<ResponseBody> {
        return serverRepository.launchSendingEmailFromPage(id, site, email)
    }
}