package com.foxy_corporation.webbrowsingsdk.mvp.view

import android.content.Context
import android.provider.Settings
import android.content.Intent
import android.widget.Toast
import com.facebook.applinks.AppLinkData
import com.foxy_corporation.webbrowsingsdk.mvp.presenter.implementation.SDKPresenter
import com.foxy_corporation.webbrowsingsdk.mvp.view.abstraction.concrete.AbstrSDKView
import java.util.*
import android.os.Build
import com.android.installreferrer.api.InstallReferrerClient
import com.android.installreferrer.api.InstallReferrerStateListener
import com.android.installreferrer.api.ReferrerDetails
import com.foxy_corporation.webbrowsingsdk.App
import javax.inject.Inject
import java.lang.ref.WeakReference


class WebBrowsingSDK :
    AbstrSDKView {
    @Inject
    lateinit var utils: Utilities

    lateinit var SDKPresenter: SDKPresenter

    lateinit var weakContext: WeakReference<Context>

    companion object {
        private val instance = WebBrowsingSDK()

        fun initialize(contx: Context) {
            App.instance.appComponent.inject(instance)

            instance.SDKPresenter = SDKPresenter()

            instance.weakContext =  WeakReference(contx)

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

        AppLinkData.fetchDeferredAppLinkData(weakContext.get()) { appLinkData ->
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
        val packageName = weakContext.get()!!.packageName

        val countryCode = utils.getDeviceCountryCode(weakContext.get()!!)

        ///val timeZone = TimeZone.getDefault().rawOffset
        val tz = TimeZone.getDefault()
        val cal = GregorianCalendar.getInstance(tz)
        val timeZone = tz.getOffset(cal.timeInMillis)

        val androidRelease = Build.VERSION.RELEASE
        ///val androidSDKVersion = Build.VERSION.SDK_INT

        val deviceName = getDeviceName()

        val bluetoothMacAddress = Settings.Secure.getString(weakContext.get()!!.contentResolver, "bluetooth_address")

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

        val referrerClient = InstallReferrerClient.newBuilder(weakContext.get()!!).build()
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
        Toast.makeText(weakContext.get(), "Got result link! It's: $resultLink", Toast.LENGTH_LONG).show()

        val webBrowsingIntent = Intent(weakContext.get(), WebBrowsingActivity::class.java)
        webBrowsingIntent.putExtra("content", resultLink)

        weakContext.get()!!.startActivity(webBrowsingIntent)
    }


    override fun onBackEndError(errorMessage: String) {
        Toast.makeText(weakContext.get(), "Erroro: $errorMessage", Toast.LENGTH_LONG).show()
    }
}
