package com.foxy_corporation.webbrowsingsdk.mvp.presenter.implementation

import android.util.Log
import com.foxy_corporation.webbrowsingsdk.App
import com.foxy_corporation.webbrowsingsdk.mvp.DataManager
import com.foxy_corporation.webbrowsingsdk.mvp.presenter.BasePresenter
import com.foxy_corporation.webbrowsingsdk.mvp.presenter.abstraction.AbstrWebBrowsingPresenter
import com.foxy_corporation.webbrowsingsdk.mvp.view.abstraction.concrete.AbstrWebBrowsingView
import okhttp3.ResponseBody
import org.json.JSONArray
import rx.Subscriber
import javax.inject.Inject

class WebBrowsingPresenter : BasePresenter<AbstrWebBrowsingView>(), AbstrWebBrowsingPresenter {
    @Inject
    lateinit var dataManager: DataManager

    init {
        App.instance.appComponent.inject(this@WebBrowsingPresenter)
    }

    override fun handleSendingUserSclick(sclick: String) {
        ///Log.d("WebBrowsingPresenter_", "handleSendingUserSclick: $sclick")

        val id = dataManager.manageGettingUserId()

        dataManager.manageSendingUserSclick(id!!, sclick).subscribe(object : Subscriber<ResponseBody>() {
            override fun onCompleted() {

            }

            override fun onError(e: Throwable) {
                getView()!!.onBackEndError(e.localizedMessage)
            }

            override fun onNext(response: ResponseBody) {
                /*val resultString = response.string()

                Log.d("handleSendingUserSclick_resultString", resultString)*/
            }
        })
    }

    override fun handleGettingUserEvents() {
        dataManager.manageGettingUserEvents(dataManager.manageGettingUserId()!!).subscribe(object : Subscriber<ResponseBody>() {
            override fun onCompleted() {

            }

            override fun onError(e: Throwable) {
                getView()!!.onBackEndError(e.localizedMessage)
            }

            override fun onNext(response: ResponseBody) {
                val resultString = response.string()

                Log.d("handleGettingUserEvents_resultString", resultString)

                val events = JSONArray(resultString)

                val view = getView()

                if (events.length() != 0)
                    view!!.onGotUserEventsSuccessfully(events)
                else
                    view!!.onGotZeroUserEvents()
            }
        })
    }

    override fun handleSendingEmailFromPage(site: String, email: String) {
        val id = dataManager.manageGettingUserId()

        dataManager.manageSendingEmailFromPage(id!!, site, email).subscribe(object : Subscriber<ResponseBody>() {
            override fun onCompleted() {

            }

            override fun onError(e: Throwable) {
                getView()!!.onBackEndError(e.localizedMessage)
            }

            override fun onNext(response: ResponseBody) {
                val resultString = response.string()

                Log.d("handleSendingEmailFromPage_resultString", resultString)
            }
        })
    }

    /// TODO invoke this
    override fun destroyPresenter() {
        removeView()
    }
}