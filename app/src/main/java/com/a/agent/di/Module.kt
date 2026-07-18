package com.a.agent.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.room.Room
import com.a.agent.data.local.AgentDataStore
import com.a.agent.data.local.AgentDatabase
import com.a.agent.data.local.AgentDatabaseCallback
import com.a.agent.data.remote.ModelApi
import com.a.agent.data.remote.ModelApiImpl
import com.a.agent.data.repository.LlmModelRepositoryImpl
import com.a.agent.data.repository.LlmModelManagerRepositoryImpl
import com.a.agent.domain.repository.LlmModelRepository
import com.a.agent.domain.repository.LlmModelManagerRepository
import com.a.agent.domain.usecase.ModelUseCases
import com.a.agent.domain.usecase.model.DeleteModel
import com.a.agent.domain.usecase.model.DownloadState
import com.a.agent.domain.usecase.model.GetModel
import com.a.agent.domain.usecase.model.GetModelMetadata
import com.a.agent.domain.usecase.model.GetModels
import com.a.agent.domain.usecase.model.ToggleDownload
import com.a.agent.domain.usecase.model.UpsertModel
import com.a.agent.domain.usecase.validation.ModelInputValidation
import com.a.agent.presentation.model.ModelViewModel
import com.a.agent.presentation.modelmanager.ModelManagerViewModel
import com.a.agent.presentation.navigation.NavigationDisplayEvent
import com.a.agent.presentation.conversation.ConversationViewModel
import com.a.agent.presentation.home.HomeViewModel
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.factoryOf
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
    singleOf(::ModelApiImpl) bind ModelApi::class
}

private val localDataSourceModule = module {
    single {
        Room.databaseBuilder(
            context = androidContext(),
            klass = AgentDatabase::class.java,
            name = "agentDatabase"
        ).fallbackToDestructiveMigration(false).build()
    }
    singleOf(::AgentDatabaseCallback)
    singleOf(::AgentDataStore)
}

private val repositoryModule = module {
    singleOf(::LlmModelManagerRepositoryImpl) bind LlmModelManagerRepository::class
    singleOf(::LlmModelRepositoryImpl) bind LlmModelRepository::class
}

private val useCasesDomainModule = module {
    factoryOf(::ModelUseCases)
    factoryOf(::GetModels)
    factoryOf(::GetModel)
    factoryOf(::UpsertModel)
    factoryOf(::DeleteModel)
    factoryOf(::GetModelMetadata)
    factoryOf(::DownloadState)
    factoryOf(::ToggleDownload)

    factoryOf(::ModelInputValidation)
}

private val presentationModule = module {
    singleOf(::NavigationDisplayEvent)

    viewModelOf(::HomeViewModel)
    viewModelOf(::ModelViewModel)
    viewModelOf(::ModelManagerViewModel)

    viewModelOf(::ConversationViewModel)
}

fun getModules() = listOf(
    remoteDataSourceModule,
    localDataSourceModule,
    repositoryModule,
    useCasesDomainModule,
    presentationModule
)