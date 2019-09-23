package com.foxy_corporation.webbrowsingsdk.mvp.view

import android.annotation.SuppressLint
import android.content.Context
import android.provider.Settings
import android.content.Intent
import android.widget.Toast
import com.facebook.applinks.AppLinkData
import com.foxy_corporation.webbrowsingsdk.mvp.presenter.implementation.SDKPresenter
import com.foxy_corporation.webbrowsingsdk.mvp.view.abstraction.AbstrSDKView
import java.util.*
import android.os.Build
import com.android.installreferrer.api.InstallReferrerClient
import com.android.installreferrer.api.InstallReferrerStateListener
import com.android.installreferrer.api.ReferrerDetails
import com.foxy_corporation.webbrowsingsdk.App
import com.foxy_corporation.webbrowsingsdk.mvp.view.ui.WebBrowsingActivity
import javax.inject.Inject

class WebBrowsingSDK : AbstrSDKView {
    @Inject
    lateinit var utils: Utilities

    lateinit var SDKPresenter: SDKPresenter

    lateinit var localContext: Context

    companion object {
        @SuppressLint("StaticFieldLeak")
        private val instance = WebBrowsingSDK()

        fun initialize(contx: Context) {
            App.instance.appComponent.inject(instance)

            instance.SDKPresenter = SDKPresenter()

            instance.localContext = contx

            instance.SDKPresenter.attachView(instance)

            instance.needToGetAlreadyLaunched()
        }
    }

    override fun needToGetAlreadyLaunched() {
        ///Log.d("WebBrowsingSDK", "needToGetAlreadyLaunched")

        SDKPresenter.handleGettingAlreadyLaunched()
    }

    override fun onDidntAlreadyLaunched() {
        ///Log.d("WebBrowsingSDK", "onDidntAlreadyLaunched")

        attemptToGetFacebookDeeplink()
    }

    private fun attemptToGetFacebookDeeplink(): String {
        var facebookDeeplink = "none"

        AppLinkData.fetchDeferredAppLinkData(localContext) { appLinkData ->
            if (appLinkData != null) {
                facebookDeeplink = appLinkData.targetUri.toString()

                prepareAndSendDetailedUserData(true, facebookDeeplink)

                SDKPresenter.handleRememberingFacebookDeeplink(facebookDeeplink!!)
            } else {
                getPlayMarketReferrer()
            }
        }

        return facebookDeeplink
    }

    private fun prepareAndSendDetailedUserData(isFirstTime: Boolean, link: String) {
        val packageName = localContext.packageName

        val countryCode = utils.getDeviceCountryCode(localContext)

        ///val timeZone = TimeZone.getDefault().rawOffset
        val tz = TimeZone.getDefault()
        val cal = GregorianCalendar.getInstance(tz)
        val timeZone = tz.getOffset(cal.timeInMillis)

        val androidRelease = Build.VERSION.RELEASE
        ///val androidSDKVersion = Build.VERSION.SDK_INT

        val deviceName = getDeviceName()

        val bluetoothMacAddress = Settings.Secure.getString(localContext.contentResolver, "bluetooth_address")

        SDKPresenter.handleSendingUserAdData(
            isFirstTime,
            packageName,
            countryCode,
            timeZone.toString(),
            androidRelease,
            deviceName,
            bluetoothMacAddress,
            link
        )
    }

    private fun getPlayMarketReferrer(): String {
        var referrer = "none"

        val referrerClient = InstallReferrerClient.newBuilder(localContext).build()
        referrerClient.startConnection(object : InstallReferrerStateListener {

            override fun onInstallReferrerSetupFinished(responseCode: Int) {
                when (responseCode) {
                    InstallReferrerClient.InstallReferrerResponse.OK -> {
                        val response: ReferrerDetails = referrerClient.installReferrer
                        referrer = response.installReferrer
                        response.referrerClickTimestampSeconds
                        response.installBeginTimestampSeconds

                        prepareAndSendDetailedUserData(true, referrer)

                        SDKPresenter.handleRememberingGooglePlayReferrer(referrer)

                        ///Log.d("WebBrowsingSDK", "onInstallReferrerSetupFinished_OK_$referrer")
                    }
                    InstallReferrerClient.InstallReferrerResponse.FEATURE_NOT_SUPPORTED -> {
                        ///Log.d("WebBrowsingSDK", "onInstallReferrerSetupFinished_FEATURE_NOT_SUPPORTED")
                    }
                    InstallReferrerClient.InstallReferrerResponse.SERVICE_UNAVAILABLE -> {
                        ///Log.d("WebBrowsingSDK", "onInstallReferrerSetupFinished_SERVICE_UNAVAILABLE")
                    }
                    InstallReferrerClient.InstallReferrerResponse.DEVELOPER_ERROR -> {
                        ///Log.d("WebBrowsingSDK", "onInstallReferrerSetupFinished_DEVELOPER_ERROR")
                    }
                    InstallReferrerClient.InstallReferrerResponse.SERVICE_DISCONNECTED -> {
                        ///Log.d("WebBrowsingSDK", "onInstallReferrerSetupFinished_SERVICE_DISCONNECTED")
                    }
                }

                referrerClient.endConnection()
            }
            override fun onInstallReferrerServiceDisconnected() {
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
            }
        })
        return referrer
    }

    private fun getDeviceName(): String {
        val manufacturer = Build.MANUFACTURER
        val model = Build.MODEL
        return if (model.startsWith(manufacturer)) {
            capitalize(model)
        } else {
            capitalize(manufacturer) + " " + model
        }
    }


    private fun capitalize(s: String?): String {
        if (s == null || s.length == 0) {
            return ""
        }
        val first = s[0]
        return if (Character.isUpperCase(first)) {
            s
        } else {
            Character.toUpperCase(first) + s.substring(1)
        }
    }

    override fun onAlreadyLaunched() {
        ///Log.d("WebBrowsingSDK", "onAlreadyLaunched")

        var link = SDKPresenter.handleGettingSavedFacebookDeeplink()

        if (link.equals("none"))
            link = SDKPresenter.handleGettingSavedGooglePlayReferrer()

        prepareAndSendDetailedUserData(false, link!!)
    }

    override fun onGotResultLink(resultLink: String) {
        ///Log.d("onGotResultLink_", resultLink)
        Toast.makeText(localContext, "Got result link! It's: $resultLink", Toast.LENGTH_LONG).show()

        val webBrowsingIntent = Intent(localContext, WebBrowsingActivity::class.java)
        webBrowsingIntent.putExtra("content", resultLink)

        localContext.startActivity(webBrowsingIntent)
    }


    override fun onBackEndError(errorMessage: String) {
        Toast.makeText(localContext, "Erroro: $errorMessage", Toast.LENGTH_LONG).show()
    }
}
