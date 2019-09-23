package com.foxy_corporation.webbrowsingsdk

import android.app.Application
import android.util.Log
import com.appsflyer.AppsFlyerConversionListener
import com.appsflyer.AppsFlyerLib
import com.appsflyer.AppsFlyerLibCore
import com.foxy_corporation.webbrowsingsdk.di.components.AppComponent
import com.foxy_corporation.webbrowsingsdk.di.components.DaggerAppComponent
import com.foxy_corporation.webbrowsingsdk.di.modules.AppModule

class App : Application() {
    companion object {
        lateinit var instance: App private set
    }

    lateinit var appComponent: AppComponent

    override fun onCreate() {
        super.onCreate()

        instance = this@App

        setup()

        initializeAppsFlyer()
    }

    private fun setup() {
        appComponent = DaggerAppComponent.builder()
            .appModule(AppModule(instance))
            .build()

        appComponent.inject(this@App)
    }

    private fun initializeAppsFlyer () {
        val conversionDataListener = object : AppsFlyerConversionListener {
            override fun onInstallConversionDataLoaded(conversionData: Map<String, String>) {
                for (attrName in conversionData.keys) {
                    Log.d(
                        AppsFlyerLibCore.LOG_TAG, "conversion_attribute: " + attrName + " = " +
                                conversionData[attrName]
                    )
                }
            }

            override fun onInstallConversionFailure(errorMessage: String) {
                Log.d(AppsFlyerLibCore.LOG_TAG, "error onAttributionFailure : $errorMessage")
            }

            override fun onAppOpenAttribution(conversionData: Map<String, String>) {
                for (attrName in conversionData.keys) {
                    Log.d(
                        AppsFlyerLibCore.LOG_TAG, "onAppOpen_attribute: " + attrName + " = " +
                                conversionData[attrName]
                    )
                }
            }

            override fun onAttributionFailure(errorMessage: String) {
                Log.d(AppsFlyerLibCore.LOG_TAG, "error onAttributionFailure : $errorMessage")
            }
        }
        AppsFlyerLib.getInstance().init("eVwcn4k2zgs8VuimVZhgmT", conversionDataListener, applicationContext)
        AppsFlyerLib.getInstance().startTracking(this)
    }
}