package com.foxy_corporation.webbrowsingsdk.mvp.presenter

import com.foxy_corporation.webbrowsingsdk.mvp.view.abstraction.AbstrView

open class BasePresenter<V: AbstrView> {

    private var view: V? = null

    fun attachView(v: V) {
        this.view = v
    }

    fun getView(): V? {
        return view
    }

    fun removeView() {
        view = null
    }
}