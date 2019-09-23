package com.foxy_corporation.webbrowsingsdk.mvp.view.abstraction

import com.foxy_corporation.webbrowsingsdk.mvp.view.ui.AbstrView

interface AbstrSDKView: AbstrView {
    fun needToGetAlreadyLaunched()
    fun onDidntAlreadyLaunched()
    fun onAlreadyLaunched()
    fun onGotResultLink(resultLink: String)
}