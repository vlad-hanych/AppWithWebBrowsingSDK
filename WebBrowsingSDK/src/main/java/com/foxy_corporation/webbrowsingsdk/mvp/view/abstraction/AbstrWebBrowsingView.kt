package com.foxy_corporation.webbrowsingsdk.mvp.view.abstraction

import com.foxy_corporation.webbrowsingsdk.mvp.view.ui.AbstrView
import org.json.JSONArray

interface AbstrWebBrowsingView: AbstrView {
    fun needToSendUserSclick(sclick: String)
    fun needToGetUserEvents()
    fun onGotUserEventsSuccessfully(events: JSONArray)
    fun onGotZeroUserEvents()
    fun needToProcessEventFacebook(goal: String,
                                   offerId: String,
                                   sum: Double,
                                   status: String,
                                   currency: String,
                                   pId: String)

    fun needToProcessEventAppMetrica(goal: String,
                                   offerId: String,
                                   depsum: Double,
                                   status: String,
                                   currency: String,
                                   pId: String)

    fun needToProcessEventAppsFlyer(goal: String,
                                   offerId: String,
                                   depsum: Double,
                                   status: String,
                                   currency: String,
                                   pId: String)

    fun needToSendEmailFromPage(email: String)
}