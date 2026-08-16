package com.a.agent.presentation

import android.app.Application
import com.a.agent.di.getModules
import com.a.agent.domain.repository.DirectoryRepository
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class MainApplication: Application() {
    private val directoryRepository: DirectoryRepository by inject()

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@MainApplication)
            modules(getModules())
        }

        directoryRepository.initializeDirectory()
    }
}