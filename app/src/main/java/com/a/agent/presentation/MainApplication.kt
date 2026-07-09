package com.a.agent.presentation

import android.app.Application
import com.a.agent.di.getModules
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class MainApplication: Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@MainApplication)
            modules(getModules())
        }
    }
}