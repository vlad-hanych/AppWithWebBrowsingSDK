package com.foxy_corporation.webbrowsingsdk.mvp.presenter.implementation

import com.foxy_corporation.webbrowsingsdk.App
import com.foxy_corporation.webbrowsingsdk.mvp.DataManager
import com.foxy_corporation.webbrowsingsdk.mvp.presenter.BasePresenter
import com.foxy_corporation.webbrowsingsdk.mvp.presenter.abstraction.AbstrSDKPresenter
import com.foxy_corporation.webbrowsingsdk.mvp.view.abstraction.concrete.AbstrSDKView
import okhttp3.ResponseBody
import org.json.JSONObject
import rx.Subscriber
import javax.inject.Inject

class SDKPresenter : BasePresenter<AbstrSDKView>(), AbstrSDKPresenter {
    @Inject
    lateinit var dataManager: DataManager

    init {
        App.instance.appComponent.inject(this@SDKPresenter)
    }

    override fun handleGettingAlreadyLaunched() {
        if (dataManager.manageGettingAlreadyLaunched())
            getView()!!.onAlreadyLaunched()
        else {
            getView()!!.onDidntAlreadyLaunched()

            dataManager.manageSettingAlreadyLaunched()
        }
    }

    override fun handleRememberingFacebookDeeplink(fbDeeplink: String) {
        dataManager.manageRememberingFacebookDeeplink(fbDeeplink)
    }

    override fun handleGettingSavedFacebookDeeplink(): String? {
        return dataManager.manageGettingFacebookDeeplink()
    }

    override fun handleRememberingGooglePlayReferrer(googlePlayReferrer: String) {
        dataManager.manageRememberingGooglePlayReferrer(googlePlayReferrer)
    }

    override fun handleGettingSavedGooglePlayReferrer(): String? {
        return dataManager.manageGettingGooglePlayReferrer()
    }

    override fun handleSendingUserAdData(
        isFirstTime: Boolean,
        application: String,
        country: String,
        tz: String,
        os: String,
        device: String,
        deviceId: String,
        referrer: String
    ) {
        val userId: String

        if (isFirstTime)
            userId = ""
        else
            userId = dataManager.manageGettingUserId()!!

        dataManager.manageSendingUserAdData(
            userId,
            application,
            country,
            tz,
            os,
            device,
            deviceId,
            referrer).subscribe(object : Subscriber<ResponseBody>() {
            override fun onCompleted() {

            }

            override fun onError(e: Throwable) {
                getView()!!.onBackEndError(e.localizedMessage)
            }

            override fun onNext(response: ResponseBody) {
                val resultString = response.string()

                val responseJSON = JSONObject(resultString)

                if (isFirstTime) {
                    val userId = responseJSON.getString("id")

                    dataManager.manageRememberingUserId(userId)
                }
                val resultLink = responseJSON.getString("result")

                getView()!!.onGotResultLink(resultLink)
            }
        })
    }

    override fun handleRememberingUserId(id: String) {
        dataManager.manageRememberingUserId(id)
    }

    /// TODO invoke this
    override fun destroyPresenter() {
        removeView()
    }
}