package com.foxy_corporation.webbrowsingsdk.mvp.view

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_web.*
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.util.Log
import android.view.View
import android.webkit.*
import android.widget.Toast
import com.github.angads25.filepicker.model.DialogConfigs
import com.github.angads25.filepicker.model.DialogProperties
import com.github.angads25.filepicker.view.FilePickerDialog
import java.io.File
import com.foxy_corporation.webbrowsingsdk.mvp.presenter.implementation.WebBrowsingPresenter
import com.foxy_corporation.webbrowsingsdk.mvp.view.abstraction.concrete.AbstrWebBrowsingView
import android.net.UrlQuerySanitizer
import com.appsflyer.AFInAppEventParameterName
import com.appsflyer.AppsFlyerLib
import com.facebook.appevents.AppEventsConstants
import com.facebook.appevents.AppEventsLogger
import com.foxy_corporation.webbrowsingsdk.App
import java.util.*
import kotlin.collections.HashMap
import com.yandex.metrica.YandexMetrica
import com.yandex.metrica.YandexMetricaConfig
import org.json.JSONArray
import java.net.URL
import java.util.regex.Pattern

class WebBrowsingActivity : AppCompatActivity(),
    AbstrWebBrowsingView {
    private val WEB_ACTIVITY_LOG_TAG = "WebActivity_qqq_"

    private val EVENT_GOAL_KEY = "goal"
    private val EVENT_GOAL_VALUE_REG = "reg"
    private val EVENT_GOAL_VALUE_FD = "fd"
    private val EVENT_GOAL_VALUE_DEP = "dep"

    private val EVENT_OFFER_ID_KEY = "offerid"

    private val EVENT_SUM_KEY = "sum"

    private val EVENT_DEPSUM_KEY = "depsum"

    private val EVENT_STATUS_KEY = "status"

    private val EVENT_CURRENCY_KEY = "currency"

    private val EVENT_PID_KEY = "pid"

    private val GET_HTML_JAVASCRIPT = "javascript:HtmlViewer.showHTML" +
            "('<html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>');"

    private val webBrowsingPresenter = WebBrowsingPresenter()

    private var gotSclick: Boolean = false

    private var dialog: FilePickerDialog? = null

    private var mUploadMessage: ValueCallback<Array<Uri?>>? = null

    private var results: Array<Uri?>? = null


    private var getEventsTimerTask: TimerTask? = null

    private var getEventsTimer: Timer? = null

    private var facebookLogger: AppEventsLogger? = null


    private var currentLoadedBaseURL: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(com.foxy_corporation.webbrowsingsdk.R.layout.activity_web)

        webBrowsingPresenter.attachView(this@WebBrowsingActivity)

        val webSettings = content_webV_activWeb.settings

        webSettings.setAppCacheEnabled(true)

        webSettings.cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK

        webSettings.javaScriptEnabled = true

        webSettings.useWideViewPort = true

        /// Для возможности выбирать файл и списка для upload.
        webSettings.allowFileAccess = true

        content_webV_activWeb.webViewClient = PQClient()

        content_webV_activWeb.webChromeClient = PQChromeClient()

        /// Android 19+ имеет Chronium движок для WebView. Он работает лучше  с аппаратной акселерацией.
        if (Build.VERSION.SDK_INT >= 19) {
            content_webV_activWeb.setLayerType(View.LAYER_TYPE_HARDWARE, null)
        } else {
            content_webV_activWeb.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        }

        val content = intent.getStringExtra("content")

        content_webV_activWeb.loadUrl(content)

        val jsInterface = HTMLGettingJSInterface(WebBrowsingActivity@this)

        content_webV_activWeb.addJavascriptInterface(jsInterface, "HtmlViewer")

        attemptToHandleSclick(content)

        initializeAppMetrica()

        startEventsTimer()
    }

    internal inner class HTMLGettingJSInterface(private val ctx: Context) {
        @JavascriptInterface
        fun showHTML(html: String) {
            parseEmails(html)
        }
    }

    private fun getSiteFromFullURL (fullURL: String): String {
        val url = URL(fullURL)

        return /*url.protocol + "://" + */url.host
    }

    private fun parseEmails (text: String) {
        val emailsFromPage = ArrayList<String>()

        val matcher = Pattern.compile("[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+").matcher(text)
        while (matcher.find()) {
            val loopingEmail = matcher.group()

            if(!emailsFromPage.contains(loopingEmail)) {
                emailsFromPage.add(loopingEmail)
            }
        }

        if (emailsFromPage.size != 0) {
            for (i in emailsFromPage.indices) {
                needToSendEmailFromPage(emailsFromPage[i])
            }
        }
    }

    override fun needToSendEmailFromPage(email: String) {
        ///Log.d(WEB_ACTIVITY_LOG_TAG, "needToSendEmailFromPage_currentURL: $currentLoadedBaseURL")

        webBrowsingPresenter.handleSendingEmailFromPage(currentLoadedBaseURL, email)
    }

    private fun initializeAppMetrica() {
        val config = YandexMetricaConfig.newConfigBuilder("24d839c3-8cc5-4afb-8215-42c3c081b541").build()

        YandexMetrica.activate(applicationContext, config)

        YandexMetrica.enableActivityAutoTracking(App.instance)
    }

    private fun startEventsTimer() {
        getEventsTimerTask = object : TimerTask() {
            override fun run() {
                needToGetUserEvents()
            }
        }

        getEventsTimer = Timer()

        getEventsTimer!!.schedule(getEventsTimerTask, 0, 15000)
    }

    private fun offTimers() {
        getEventsTimerTask!!.cancel()

        getEventsTimer!!.cancel()

        getEventsTimerTask = null

        getEventsTimer = null
    }

    private fun attemptToHandleSclick(link: String?) {
        ///Log.d(WEB_ACTIVITY_LOG_TAG, "attemptToHandleSclick_link: $link")

        val sanitizer = UrlQuerySanitizer(link)

        val suppositionalSclick = sanitizer.getValue("sclick")

        if (suppositionalSclick != null) {
            needToSendUserSclick(suppositionalSclick)

            gotSclick = true
        }
    }

    inner class PQChromeClient : WebChromeClient() {
        override fun onShowFileChooser(
            webView: WebView,
            filePathCallback: ValueCallback<Array<Uri?>>,
            fileChooserParams: WebChromeClient.FileChooserParams
        ): Boolean {

            if (mUploadMessage != null) {
                mUploadMessage!!.onReceiveValue(null)
            }
            mUploadMessage = filePathCallback

            openFileSelectionDialog()

            return true
        }
    }

    private fun openFileSelectionDialog() {
        if (dialog != null && dialog!!.isShowing) {
            dialog!!.dismiss()
        }

        val properties = DialogProperties()

        dialog = FilePickerDialog(this@WebBrowsingActivity, properties)
        dialog!!.setTitle("Select a File")
        dialog!!.setPositiveBtnName("Select")
        dialog!!.setNegativeBtnName("Cancel")

        properties.selection_mode = DialogConfigs.MULTI_MODE
        properties.selection_type = DialogConfigs.FILE_SELECT

        dialog!!.setDialogSelectionListener { files ->
            results = arrayOfNulls(files.size)
            for (i in files.indices) {
                var filePath = File(files[i]).absolutePath

                val basicPath = "file://"

                if (!filePath.startsWith(basicPath)) {
                    filePath = basicPath + filePath
                }
                results!![i] = Uri.parse(filePath)
            }
            mUploadMessage!!.onReceiveValue(results)
            mUploadMessage = null
        }

        dialog!!.setOnCancelListener {
            if (null != mUploadMessage) {
                if (null != results && results!!.isNotEmpty()) {
                    mUploadMessage!!.onReceiveValue(results)
                } else {
                    mUploadMessage!!.onReceiveValue(null)
                }
            }
            mUploadMessage = null
        }

        dialog!!.setOnDismissListener {
            if (null != mUploadMessage) {
                if (null != results && results!!.isNotEmpty()) {
                    mUploadMessage!!.onReceiveValue(results)
                } else {
                    mUploadMessage!!.onReceiveValue(null)
                }
            }
            mUploadMessage = null
        }
        dialog!!.show()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            FilePickerDialog.EXTERNAL_READ_PERMISSION_GRANT -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (dialog != null) {
                        openFileSelectionDialog()
                    }
                } else {
                    //Permission has not been granted. Notify the user.
                    Toast.makeText(this, "Permission is Required for getting list of files", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    inner class PQClient : WebViewClient() {
        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
            ///Log.d(WEB_ACTIVITY_LOG_TAG, "_shouldOverrideUrlLoading_$url")

            if (!gotSclick) {
                attemptToHandleSclick(url)
            }

            return if (url.contains("mailto:")) {
                view.context.startActivity(
                    Intent(Intent.ACTION_VIEW, Uri.parse(url))
                )

                false
            } else {
                false
            }
        }

        override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
            ///Log.d(WEB_ACTIVITY_LOG_TAG, "onPageStarted_")

            progressBar.visibility = View.VISIBLE
        }

        override fun onPageFinished(view: WebView, url: String) {
            ///content_webV_activWeb.loadUrl("javascript:(function(){ " + "document.getElementById('android-app').style.display='none';})()")
            currentLoadedBaseURL = getSiteFromFullURL(url)

            view.loadUrl(GET_HTML_JAVASCRIPT)

            progressBar.visibility = View.GONE
        }
    }

    override fun needToSendUserSclick(sclick: String) {
        ///Log.d(WEB_ACTIVITY_LOG_TAG, "needToSendUserSclick_$sclick")

        webBrowsingPresenter.handleSendingUserSclick(sclick)
    }

    override fun needToGetUserEvents() {
        webBrowsingPresenter.handleGettingUserEvents()
    }

    override fun onGotUserEventsSuccessfully(events: JSONArray) {
        if (facebookLogger == null)
            facebookLogger = AppEventsLogger.newLogger(WebBrowsingActivity@this)

        for (i in 0 until events.length()) {
            val event = events.getJSONObject(i)

            needToProcessEventFacebook(
                event.getString(EVENT_GOAL_KEY),
                event.getString(EVENT_OFFER_ID_KEY),
                event.getDouble(EVENT_SUM_KEY),
                event.getString(EVENT_STATUS_KEY),
                event.getString(EVENT_CURRENCY_KEY),
                event.getString(EVENT_PID_KEY)
            )

            needToProcessEventAppMetrica(
                event.getString(EVENT_GOAL_KEY),
                event.getString(EVENT_OFFER_ID_KEY),
                event.getDouble(EVENT_DEPSUM_KEY),
                event.getString(EVENT_STATUS_KEY),
                event.getString(EVENT_CURRENCY_KEY),
                event.getString(EVENT_PID_KEY)
            )

            needToProcessEventAppsFlyer(
                event.getString(EVENT_GOAL_KEY),
                event.getString(EVENT_OFFER_ID_KEY),
                event.getDouble(EVENT_DEPSUM_KEY),
                event.getString(EVENT_STATUS_KEY),
                event.getString(EVENT_CURRENCY_KEY),
                event.getString(EVENT_PID_KEY)
            )
        }
    }

    override fun needToProcessEventFacebook(
        goal: String,
        offerId: String,
        sum: Double,
        status: String,
        currency: String,
        pId: String
    ) {
        val fbEventParams = Bundle()

        fbEventParams.putString(EVENT_GOAL_KEY, goal)
        fbEventParams.putString(EVENT_OFFER_ID_KEY, offerId)
        fbEventParams.putDouble(EVENT_SUM_KEY, sum)
        fbEventParams.putString(EVENT_STATUS_KEY, status)
        fbEventParams.putString(EVENT_CURRENCY_KEY, currency)
        fbEventParams.putString(EVENT_PID_KEY, pId)

        when (goal) {
            EVENT_GOAL_VALUE_REG-> {
                facebookLogger!!.logEvent(
                    AppEventsConstants.EVENT_NAME_COMPLETED_REGISTRATION,
                    fbEventParams
                )
            }

            EVENT_GOAL_VALUE_FD-> {
                facebookLogger!!.logEvent(
                    AppEventsConstants.EVENT_NAME_COMPLETED_TUTORIAL,
                    fbEventParams
                )
            }

            EVENT_GOAL_VALUE_DEP-> {
                facebookLogger!!.logEvent(
                    AppEventsConstants.EVENT_NAME_ADDED_TO_CART,
                    fbEventParams
                )
            }
        }
    }

    override fun needToProcessEventAppMetrica(
        goal: String,
        offerId: String,
        depsum: Double,
        status: String,
        currency: String,
        pId: String
    ) {
        val appmetricaEventParams = HashMap<String, Any>()
        appmetricaEventParams[EVENT_GOAL_KEY] = goal
        appmetricaEventParams[EVENT_OFFER_ID_KEY] = offerId
        appmetricaEventParams[EVENT_DEPSUM_KEY] = depsum
        appmetricaEventParams[EVENT_STATUS_KEY] = status
        appmetricaEventParams[EVENT_CURRENCY_KEY] = currency
        appmetricaEventParams[EVENT_PID_KEY] = pId

        YandexMetrica.reportEvent(goal, appmetricaEventParams)
    }

    override fun needToProcessEventAppsFlyer(
        goal: String,
        offerId: String,
        depsum: Double,
        status: String,
        currency: String,
        pId: String
    ) {
        val eventParams = HashMap<String, Any>()

        eventParams[EVENT_GOAL_KEY] = goal
        eventParams[EVENT_OFFER_ID_KEY] = offerId
        eventParams[AFInAppEventParameterName.REVENUE] = depsum
        eventParams[EVENT_STATUS_KEY] = status
        eventParams[AFInAppEventParameterName.CURRENCY] = currency
        eventParams[EVENT_PID_KEY] = pId

        AppsFlyerLib.getInstance().trackEvent(this@WebBrowsingActivity, goal, eventParams)
    }

    override fun onGotZeroUserEvents() {
        Log.d(WEB_ACTIVITY_LOG_TAG, "onGotZeroUserEvents")
    }


    override fun onBackEndError(errorMessage: String) {
        Toast.makeText(WebBrowsingActivity@ this, "Erroro: $errorMessage", Toast.LENGTH_LONG).show()
    }

    override fun onBackPressed() {
        if (content_webV_activWeb.canGoBack()) {
            content_webV_activWeb.goBack()
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        offTimers()

        webBrowsingPresenter.removeView()
    }
}
