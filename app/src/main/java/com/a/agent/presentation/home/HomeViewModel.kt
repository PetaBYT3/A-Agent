package com.a.agent.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.a.agent.data.local.ConversationEntity
import com.a.agent.data.local.ModelEntity
import com.a.agent.domain.model.LlmModelEngineBackend
import com.a.agent.domain.model.LlmModelFilter
import com.a.agent.domain.repository.LlmModelManagerRepository
import com.a.agent.domain.repository.LlmModelEngineRepository
import com.a.agent.presentation.navigation.Event
import com.a.agent.presentation.navigation.NavigationDisplayEvent
import com.a.agent.presentation.util.toMegaByte
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.time.Duration.Companion.milliseconds

class HomeViewModel(
    private val llmModelManagerRepository: LlmModelManagerRepository,
    private val llmModelEngineRepository: LlmModelEngineRepository,
    private val navigationDisplayEvent: NavigationDisplayEvent
): ViewModel() {
    private val _state = MutableStateFlow(HomeState())
    val state = _state.onStart {
        initialize()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeState())

    private fun initialize() {
        viewModelScope.launch {
            llmModelManagerRepository.activeDownloadInfo.collect { map ->
                _state.update { it.copy(totalDownloadingProgress = map.size) }
            }
        }

        viewModelScope.launch {
            llmModelManagerRepository.getLlmModelEngineConfiguration().collectLatest { either ->
                either.onRight { pair ->
                    _state.update { it.copy(llmModelEngineConfiguration = pair.first, selectedModelEntity = pair.second) }
                }.onLeft { error ->
                    _state.update { it.copy(isLlmModelEngineConfigurationError = error) }
                }
                _state.update { it.copy(isLlmModelEngineConfigurationLoading = false) }
            }
        }

        viewModelScope.launch {
            llmModelManagerRepository.getModels(LlmModelFilter.Downloaded).collect { either ->
                either.onRight { modelEntities ->
                    _state.update { it.copy(downloadedModelEntities = modelEntities) }
                }.onLeft { error ->
                    _state.update { it.copy(downloadedModelError = error) }
                }
            }
        }

        viewModelScope.launch {
            llmModelEngineRepository.isLlmModelEngineOnline.collect { isModelEngineOnline ->
                _state.update { it.copy(isModelEngineOnline = isModelEngineOnline) }
            }
        }

        viewModelScope.launch {
            llmModelEngineRepository.getConversations().collect { either ->
                either.onRight { conversationEntities ->
                    _state.update { it.copy(conversationEntities = conversationEntities, isConversationsLoading = false) }
                }.onLeft { error ->
                    _state.update { it.copy(conversationError = error, isConversationsLoading = false) }
                }
            }
        }
    }

    fun onAction(action: HomeAction) {
        when (action) {
            HomeAction.DownloadedModelBottomSheet -> {
                _state.update { it.copy(downloadedModelBottomSheet = !it.downloadedModelBottomSheet) }
            }
            is HomeAction.SelectModel -> selectModel(action.modelEntity)
            HomeAction.UpsertConversationBottomSheet -> {
                _state.update { it.copy(upsertConversationBottomSheet = !it.upsertConversationBottomSheet) }
            }
            HomeAction.LlmModelEngineConfigurationBottomSheet -> {
                _state.update { it.copy(llmModelEngineConfigurationBottomSheet = !it.llmModelEngineConfigurationBottomSheet) }
            }
            is HomeAction.ProcessBackendChip -> processBackendChip(action.backend)
            is HomeAction.VisionBackendChip -> visionBackendChip(action.backend)
            is HomeAction.MaxNumTokens -> maxNumTokensChip(action.tokens)
            HomeAction.ToggleModelEngine -> toggleModelEngine()
            is HomeAction.ConversationNameTextField -> {
                _state.update { it.copy(conversationNameTextField = action.name) }
            }
            HomeAction.UpsertConversationButton -> upsertConversationButton()
        }
    }

    private fun selectModel(modelEntity: ModelEntity) = viewModelScope.launch {
        val llmModelEngineConfiguration = _state.value.llmModelEngineConfiguration.copy(
            selectedModelId = modelEntity.id
        )
        llmModelManagerRepository.upsertLlmModelEngineConfiguration(llmModelEngineConfiguration).collect { either ->
            either.onLeft { error ->
                navigationDisplayEvent.sendEvent(Event.ShowSnackBar(error))
            }
        }
    }

    private fun processBackendChip(backend: LlmModelEngineBackend) = viewModelScope.launch {
        val llmModelEngineConfiguration = _state.value.llmModelEngineConfiguration.copy(
            processingBackend = backend
        )
        llmModelManagerRepository.upsertLlmModelEngineConfiguration(llmModelEngineConfiguration).collect { either ->
            either.onLeft { error ->
                navigationDisplayEvent.sendEvent(Event.ShowSnackBar(error))
            }
        }
    }

    private fun maxNumTokensChip(token: Int) = viewModelScope.launch {
        val llmModelEngineConfiguration = _state.value.llmModelEngineConfiguration.copy(
            maxNumTokens = token
        )
        llmModelManagerRepository.upsertLlmModelEngineConfiguration(llmModelEngineConfiguration).collect { either ->
            either.onLeft { error ->
                navigationDisplayEvent.sendEvent(Event.ShowSnackBar(error))
            }
        }
    }

    private fun visionBackendChip(backend: LlmModelEngineBackend) = viewModelScope.launch {
        val llmModelEngineConfiguration = _state.value.llmModelEngineConfiguration.copy(
            visionBackend = backend
        )
        llmModelManagerRepository.upsertLlmModelEngineConfiguration(llmModelEngineConfiguration).collect { either ->
            either.onLeft { error ->
                navigationDisplayEvent.sendEvent(Event.ShowSnackBar(error))
            }
        }
    }

    private fun toggleModelEngine() = viewModelScope.launch {
        _state.update { it.copy(isModelEngineLoading = true) }
        when (_state.value.isModelEngineOnline) {
            true -> {
                llmModelEngineRepository.uninitializeEngine().collect { either ->
                    either.onRight {

                    }
                    _state.update { it.copy(isModelEngineLoading = false) }
                }
            }
            false -> {
                llmModelEngineRepository.initializeEngine(File(_state.value.selectedModelEntity.path)).collect { either ->
                    either.onRight {

                    }
                    _state.update { it.copy(isModelEngineLoading = false) }
                }
            }
        }
    }

    private fun upsertConversationButton() = viewModelScope.launch {
        val conversationEntity = ConversationEntity(
            title = _state.value.conversationNameTextField
        )
        llmModelEngineRepository.upsertConversation(conversationEntity).collect { either ->
            either.onRight {
                navigationDisplayEvent.sendEvent(Event.ShowSnackBar("Engine Initialized"))
            }.onLeft { error ->
                navigationDisplayEvent.sendEvent(Event.ShowSnackBar(error))
            }
            _state.update { it.copy(conversationNameTextField = "") }
        }
    }
}