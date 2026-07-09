package com.a.agent.di

import androidx.room.Room
import com.a.agent.data.local.AgentDatabase
import com.a.agent.data.remote.ModelApi
import com.a.agent.data.remote.ModelApiImpl
import com.a.agent.data.repository.LiteRepositoryImpl
import com.a.agent.data.repository.ModelRepositoryImpl
import com.a.agent.domain.repository.LiteRepository
import com.a.agent.domain.repository.ModelRepository
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
import com.a.agent.presentation.texttotext.TextToTextViewModel
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
}

private val repositoryModule = module {
    singleOf(::ModelRepositoryImpl) bind ModelRepository::class
    singleOf(::LiteRepositoryImpl) bind LiteRepository::class
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

    viewModelOf(::ModelViewModel)
    viewModelOf(::ModelManagerViewModel)

    viewModelOf(::TextToTextViewModel)
}

fun getModules() = listOf(
    remoteDataSourceModule,
    localDataSourceModule,
    repositoryModule,
    useCasesDomainModule,
    presentationModule
)