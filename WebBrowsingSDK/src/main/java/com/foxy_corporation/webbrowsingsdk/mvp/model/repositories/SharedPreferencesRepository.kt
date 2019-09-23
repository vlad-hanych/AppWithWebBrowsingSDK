package com.foxy_corporation.webbrowsingsdk.mvp.model.repositories

import android.content.Context
import android.content.SharedPreferences

class SharedPreferencesRepository(context: Context) {
    private val PREFERENCES_NAME = "WebBrowsingSDKPrefs"

    private val ALREADY_LAUNCHED = "already_launched"

    private val FACEBOOK_DEEPLINK = "facebook_deeplink"

    private val GOOGLE_PLAY_REFERRER = "referrer"

    private val USER_ID = "id"

    private val sharedPreferences: SharedPreferences

    init {
        sharedPreferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
    }

    fun getAlreadyLaunched(): Boolean {
        return sharedPreferences.getBoolean(ALREADY_LAUNCHED, false)
    }

    fun setAlreadyLaunched() {
        sharedPreferences.edit()
            .putBoolean(ALREADY_LAUNCHED, true)
            .apply()
    }

    fun rememberFacebookDeeplink(fbDeeplink: String) {
        sharedPreferences.edit()
            .putString(FACEBOOK_DEEPLINK, fbDeeplink)
            .apply()
    }

    fun getFacebookDeeplink(): String? {
        return sharedPreferences.getString(FACEBOOK_DEEPLINK, "none")
    }

    fun rememberGooglePlayReferrer(referrer: String) {
        sharedPreferences.edit()
            .putString(GOOGLE_PLAY_REFERRER, referrer)
            .apply()
    }

    fun getGooglePlayReferrer(): String? {
        return sharedPreferences.getString(GOOGLE_PLAY_REFERRER, "none")
    }

    fun rememberId(id: String) {
        sharedPreferences.edit()
            .putString(USER_ID, id)
            .apply()
    }

    fun getUserId(): String? {
        return sharedPreferences.getString(USER_ID, "")
    }
}