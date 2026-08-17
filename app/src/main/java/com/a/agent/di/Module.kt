package com.a.agent.di

import androidx.room.Room
import com.a.agent.BuildConfig
import com.a.agent.data.local.DataStore
import com.a.agent.data.local.Database
import com.a.agent.data.remote.LlmApi
import com.a.agent.data.remote.LlmApiImpl
import com.a.agent.data.repository.BackupRepositoryImpl
import com.a.agent.data.repository.ConversationRepositoryImpl
import com.a.agent.data.repository.DirectoryRepositoryImpl
import com.a.agent.data.repository.EngineRepositoryImpl
import com.a.agent.data.repository.LlmRepositoryImpl
import com.a.agent.data.repository.PermissionRepositoryImpl
import com.a.agent.data.repository.SttRepositoryImpl
import com.a.agent.data.repository.TtsRepositoryImpl
import com.a.agent.domain.repository.BackupRepository
import com.a.agent.domain.repository.ConversationRepository
import com.a.agent.domain.repository.DirectoryRepository
import com.a.agent.domain.repository.EngineRepository
import com.a.agent.domain.repository.LlmRepository
import com.a.agent.domain.repository.PermissionRepository
import com.a.agent.domain.repository.SttRepository
import com.a.agent.domain.repository.TtsRepository
import com.a.agent.presentation.conversation.ConversationViewModel
import com.a.agent.presentation.conversationmanager.ConversationManagerViewModel
import com.a.agent.presentation.home.HomeViewModel
import com.a.agent.presentation.llm.LlmViewModel
import com.a.agent.presentation.llmmanager.LlmManagerViewModel
import com.a.agent.presentation.navigation.NavigationDisplayBackStack
import com.a.agent.presentation.settings.SettingsViewModel
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

private val remoteDataSourceModule = module {
    single {
        HttpClient(CIO) {
            install(HttpTimeout) {
                requestTimeoutMillis = 60_000
                connectTimeoutMillis = 15_000
            }
        }
    }
    singleOf(::LlmApiImpl) bind LlmApi::class
}

private val localDataSourceModule = module {
    single {
        Room.databaseBuilder(
            context = androidContext(),
            klass = Database::class.java,
            name = BuildConfig.DATABASE_NAME
        ).fallbackToDestructiveMigration(false).build()
    }
    singleOf(::DataStore)
}

private val repositoryModule = module {
    singleOf(::PermissionRepositoryImpl).bind(PermissionRepository::class)
    singleOf(::DirectoryRepositoryImpl).bind(DirectoryRepository::class)
    singleOf(::BackupRepositoryImpl).bind(BackupRepository::class)

    singleOf(::LlmRepositoryImpl).bind(LlmRepository::class)
    singleOf(::ConversationRepositoryImpl).bind(ConversationRepository::class)
    singleOf(::EngineRepositoryImpl).bind(EngineRepository::class)
    singleOf(::TtsRepositoryImpl).bind(TtsRepository::class)
    singleOf(::SttRepositoryImpl).bind(SttRepository::class)
}

private val presentationModule = module {
    singleOf(::NavigationDisplayBackStack)

    viewModelOf(::HomeViewModel)
    viewModelOf(::SettingsViewModel)
    viewModelOf(::ConversationViewModel)
    viewModelOf(::ConversationManagerViewModel)
    viewModelOf(::LlmViewModel)
    viewModelOf(::LlmManagerViewModel)
}

fun getModules() = listOf(
    remoteDataSourceModule,
    localDataSourceModule,
    repositoryModule,
    presentationModule
)