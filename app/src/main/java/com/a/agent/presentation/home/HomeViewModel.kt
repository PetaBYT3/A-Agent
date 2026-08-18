package com.a.agent.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.a.agent.data.local.llm.LlmEntity
import com.a.agent.domain.model.LlmBackend
import com.a.agent.domain.model.ProcessStatus
import com.a.agent.domain.repository.ConversationRepository
import com.a.agent.domain.repository.EngineRepository
import com.a.agent.domain.repository.LlmRepository
import com.a.agent.domain.repository.PermissionRepository
import com.a.agent.presentation.navigation.Effect
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

class HomeViewModel(
    private val permissionRepository: PermissionRepository,
    private val engineRepository: EngineRepository,
    private val llmRepository: LlmRepository,
    private val conversationRepository: ConversationRepository
): ViewModel() {
    private val _state = MutableStateFlow(HomeState())
    val state = _state.onStart {
        initialize()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(1_000), HomeState())

    private val _effect = Channel<Effect>(Channel.CONFLATED)
    val effect = _effect.receiveAsFlow()

    private fun initialize() {
        viewModelScope.launch { llmRepository.validateSelectedLlm() }

        permissionRepository.isNotificationPermissionGranted.onEach { isNotificationPermissionGranted ->
            _state.update { it.copy(isNotificationPermissionGranted = isNotificationPermissionGranted) }
        }.launchIn(viewModelScope)

        permissionRepository.isStoragePermissionGranted.onEach { isStoragePermissionGranted ->
            _state.update { it.copy(isStoragePermissionGranted = isStoragePermissionGranted) }
        }.launchIn(viewModelScope)

        permissionRepository.isMicrophonePermissionGranted.onEach { isMicrophonePermissionGranted ->
            _state.update { it.copy(isMicrophonePermissionGranted = isMicrophonePermissionGranted) }
        }.launchIn(viewModelScope)

        engineRepository.isEngineOnline.onEach { bool ->
            _state.update { it.copy(isEngineOnline = bool) }
        }.launchIn(viewModelScope)

        engineRepository.getConfiguration().onEach { either ->
            either.onRight { pair ->
                _state.update { it.copy(configuration = pair.first, selectedLlm = pair.second, isConfigurationLoading = false) }
            }.onLeft { error ->
                _state.update { it.copy(isConfigurationError = error, isConfigurationLoading = false) }
            }
        }.launchIn(viewModelScope)

        llmRepository.getLlms().onEach { either ->
            either.onRight { modelEntities ->
                val downloadedLlm = modelEntities.filter { it.isDownloaded }
                _state.update { it.copy(downloadedLlm = downloadedLlm) }
            }.onLeft { error ->
                _state.update { it.copy(isDownloadedLlmError = error) }
            }
        }.launchIn(viewModelScope)

        conversationRepository.getConversationDetails().onEach { either ->
            either.onRight { conversationEntities ->
                _state.update { it.copy(conversationDetails = conversationEntities, isConversationsLoading = false) }
            }.onLeft { error ->
                _state.update { it.copy(isConversationsError = error, isConversationsLoading = false) }
            }
        }.launchIn(viewModelScope)
    }

    fun onAction(action: HomeAction) {
        when (action) {
            HomeAction.UpdatePermission -> permissionRepository.updatePermission()
            HomeAction.ToggleEngine -> toggleEngine()
            HomeAction.DownloadedLlmBottomSheetVisibility -> {
                _state.update { it.copy(isDownloadedLlmBottomSheetVisible = !it.isDownloadedLlmBottomSheetVisible) }
            }
            is HomeAction.SelectModel -> selectModel(action.llm)
            HomeAction.ConfigurationBottomSheetVisibility -> {
                _state.update { it.copy(isConfigurationBottomSheetVisible = !it.isConfigurationBottomSheetVisible) }
            }
            is HomeAction.AutomaticSwitch -> automaticSwitch(action.isAutomatic)
            is HomeAction.ProcessBackendChip -> processBackendChip(action.backend)
            is HomeAction.VisionBackendChip -> visionBackendChip(action.backend)
        }
    }

    private fun toggleEngine() {
        _state.update { it.copy(isEngineLoading = true) }
        when (_state.value.isEngineOnline) {
            true -> {
                viewModelScope.launch {
                    _state.update { it.copy(engineTitle = "Shutting Down", engineStatus = "Clearing Memory...") }
                    delay(2.seconds)
                    engineRepository.destroyEngine()
                    _state.update { it.copy(isEngineLoading = false) }
                }
            }
            false -> {
                engineRepository.initializeEngine(_state.value.selectedLlm!!).onStart {
                    _state.update { it.copy(engineTitle = "Enabling") }
                }.onEach { either ->
                    either.onRight { processStatus ->
                        when (processStatus) {
                            is ProcessStatus.OnProcess -> {
                                _state.update { it.copy(engineStatus = processStatus.process) }
                            }
                            ProcessStatus.OnCompletion -> {
                                _state.update { it.copy(engineStatus = "Complete", isEngineLoading = false) }
                            }
                        }
                    }.onLeft { error ->
                        _state.update { it.copy(isEngineLoading = false) }
                        _effect.send(Effect.ShowSnackBar(error))
                    }
                }.launchIn(viewModelScope)
            }
        }
    }

    private fun selectModel(llm: LlmEntity) {
        val configuration = _state.value.configuration?.copy(
            selectedLlmId = llm.id
        ) ?: return
        engineRepository.setConfiguration(configuration).onEach { either ->
            either.onLeft { error ->
                _effect.send(Effect.ShowSnackBar(error))
            }
        }.launchIn(viewModelScope)
    }

    private fun automaticSwitch(isAutomatic: Boolean) {
        val configuration = _state.value.configuration?.copy(
            isAutomatic = isAutomatic
        ) ?: return
        engineRepository.setConfiguration(configuration).onEach { either ->
            either.onLeft { error ->
                _effect.send(Effect.ShowSnackBar(error))
            }
        }.launchIn(viewModelScope)
    }

    private fun processBackendChip(backend: LlmBackend) {
        val configuration = _state.value.configuration?.copy(
            processing = backend
        ) ?: return
        engineRepository.setConfiguration(configuration).onEach { either ->
            either.onLeft { error ->
                _effect.send(Effect.ShowSnackBar(error))
            }
        }.launchIn(viewModelScope)
    }

    private fun visionBackendChip(backend: LlmBackend) {
        val configuration = _state.value.configuration?.copy(
            vision = backend
        ) ?: return
        engineRepository.setConfiguration(configuration).onEach { either ->
            either.onLeft { error ->
                _effect.send(Effect.ShowSnackBar(error))
            }
        }.launchIn(viewModelScope)
    }
}