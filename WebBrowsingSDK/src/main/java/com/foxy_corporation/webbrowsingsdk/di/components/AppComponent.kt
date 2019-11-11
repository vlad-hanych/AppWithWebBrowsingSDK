package com.foxy_corporation.webbrowsingsdk.di.components

import com.foxy_corporation.webbrowsingsdk.App
import com.foxy_corporation.webbrowsingsdk.di.modules.AppModule
import com.foxy_corporation.webbrowsingsdk.di.modules.RetrofitModule
import com.foxy_corporation.webbrowsingsdk.mvp.DataManager
import com.foxy_corporation.webbrowsingsdk.mvp.model.repositories.ServerRepository
import com.foxy_corporation.webbrowsingsdk.mvp.presenter.implementation.SDKPresenter
import com.foxy_corporation.webbrowsingsdk.mvp.presenter.implementation.WebBrowsingPresenter
import com.foxy_corporation.webbrowsingsdk.mvp.view.WebBrowsingSDK
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [AppModule::class, RetrofitModule::class])
interface AppComponent {
    fun inject(serverRepository: ServerRepository)
    fun inject(dataManager: DataManager)
    fun inject(SDKPresenter: SDKPresenter)
    fun inject(webBrowsingPresenter: WebBrowsingPresenter)
    fun inject(webBrowsingSDK: WebBrowsingSDK)
}