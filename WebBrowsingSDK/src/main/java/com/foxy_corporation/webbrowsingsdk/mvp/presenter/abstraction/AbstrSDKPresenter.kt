package com.foxy_corporation.webbrowsingsdk.mvp.presenter.abstraction

import com.foxy_corporation.webbrowsingsdk.mvp.presenter.AbstrPresenter

interface AbstrSDKPresenter : AbstrPresenter {
    fun handleGettingAlreadyLaunched()
    fun handleRememberingFacebookDeeplink(fbDeeplink: String)
    fun handleGettingSavedFacebookDeeplink(): String?
    fun handleRememberingGooglePlayReferrer(referrer: String)
    fun handleGettingSavedGooglePlayReferrer(): String?
    fun handleSendingUserAdData(
        isFirstTime: Boolean,
        application: String,
        country: String,
        tz: String,
        os: String,
        device: String,
        deviceId: String,
        referrer: String
    )

    fun handleRememberingUserId(id: String)

}