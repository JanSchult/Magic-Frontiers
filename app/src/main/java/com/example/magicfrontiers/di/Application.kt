package com.example.magicfrontiers.di

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext.startKoin

class MagicFrontierApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@MagicFrontierApplication)
            modules(appModule)
        }
    }
}