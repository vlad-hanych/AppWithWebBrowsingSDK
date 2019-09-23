package com.foxy_corporation.webbrowsingsdk.di.modules

import android.app.Application
import android.content.Context
import com.foxy_corporation.webbrowsingsdk.mvp.DataManager
import com.foxy_corporation.webbrowsingsdk.mvp.model.repositories.SharedPreferencesRepository
import com.foxy_corporation.webbrowsingsdk.mvp.view.Utilities
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class AppModule(private val application: Application) {
    @Provides
    @Singleton
    fun provideContext(): Context {
        return application.applicationContext
    }

    @Provides
    @Singleton
    fun provideUtils(): Utilities {
        return Utilities()
    }

    @Provides
    @Singleton
    fun provideDataManager(): DataManager {
        return DataManager()
    }

    @Provides
    @Singleton
    fun providePreferencesRepository(context: Context): SharedPreferencesRepository {
        return SharedPreferencesRepository(context)
    }
}