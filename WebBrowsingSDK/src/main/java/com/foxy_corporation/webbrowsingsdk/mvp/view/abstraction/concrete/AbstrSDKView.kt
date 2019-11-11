package com.foxy_corporation.webbrowsingsdk.mvp.view.abstraction.concrete

import com.foxy_corporation.webbrowsingsdk.mvp.view.abstraction.AbstrView

interface AbstrSDKView: AbstrView {
    fun needToGetAlreadyLaunched()
    fun onDidntAlreadyLaunched()
    fun onAlreadyLaunched()
    fun onGotResultLink(resultLink: String)
}