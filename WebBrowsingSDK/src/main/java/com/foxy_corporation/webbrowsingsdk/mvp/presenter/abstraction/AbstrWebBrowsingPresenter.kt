package com.foxy_corporation.webbrowsingsdk.mvp.presenter.abstraction

import com.foxy_corporation.webbrowsingsdk.mvp.presenter.AbstrPresenter

interface AbstrWebBrowsingPresenter: AbstrPresenter {
    fun handleSendingUserSclick(sclick: String)
    fun handleGettingUserEvents()
    fun handleSendingEmailFromPage(site: String, email: String)
}