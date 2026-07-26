package com.a.agent.di

import androidx.room.Room
import com.a.agent.data.local.AgentDataStore
import com.a.agent.data.local.AgentDatabase
import com.a.agent.data.remote.ModelApi
import com.a.agent.data.remote.ModelApiImpl
import com.a.agent.data.repository.ConversationRepositoryImpl
import com.a.agent.data.repository.LlmModelEngineRepositoryImpl
import com.a.agent.data.repository.LlmModelManagerRepositoryImpl
import com.a.agent.domain.repository.ConversationRepository
import com.a.agent.domain.repository.LlmModelEngineRepository
import com.a.agent.domain.repository.LlmModelManagerRepository
import com.a.agent.presentation.conversation.ConversationViewModel
import com.a.agent.presentation.conversationmanager.ConversationManagerViewModel
import com.a.agent.presentation.home.HomeViewModel
import com.a.agent.presentation.model.ModelViewModel
import com.a.agent.presentation.modelmanager.ModelManagerViewModel
import com.a.agent.presentation.navigation.NavigationDisplayEvent
import com.a.agent.presentation.upsertlocalllm.UpsertLocalModelViewModel
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
    singleOf(::AgentDataStore)
}

private val repositoryModule = module {
    singleOf(::ConversationRepositoryImpl) bind ConversationRepository::class
    singleOf(::LlmModelManagerRepositoryImpl) bind LlmModelManagerRepository::class
    singleOf(::LlmModelEngineRepositoryImpl) bind LlmModelEngineRepository::class
}

private val presentationModule = module {
    singleOf(::NavigationDisplayEvent)

    viewModelOf(::HomeViewModel)
    viewModelOf(::ConversationViewModel)
    viewModelOf(::ConversationManagerViewModel)
    viewModelOf(::ModelViewModel)
    viewModelOf(::ModelManagerViewModel)
    viewModelOf(::UpsertLocalModelViewModel)

}

fun getModules() = listOf(
    remoteDataSourceModule,
    localDataSourceModule,
    repositoryModule,
    presentationModule
)