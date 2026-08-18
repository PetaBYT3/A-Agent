package com.a.agent.presentation.llm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.a.agent.data.local.llm.LlmEntity
import com.a.agent.data.local.llm.LlmSource
import com.a.agent.domain.repository.LlmRepository
import com.a.agent.domain.repository.PermissionRepository
import com.a.agent.presentation.navigation.Effect
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

class LlmViewModel(
    private val permissionRepository: PermissionRepository,
    private val llmRepository: LlmRepository
): ViewModel() {
    private val _state = MutableStateFlow(LlmState())
    val state = _state.onStart {
        initialize()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(1_000), LlmState())

    private val _effect = Channel<Effect>(Channel.CONFLATED)
    val effect = _effect.receiveAsFlow()

    private fun initialize() {
        permissionRepository.isNotificationPermissionGranted.onEach { isGranted ->
            _state.update { it.copy(isNotificationPermissionGranted = isGranted) }
        }.launchIn(viewModelScope)

        llmRepository.activeDownloadMap.onEach { map ->
            _state.update { it.copy(downloadState = map) }
        }.launchIn(viewModelScope)

        llmRepository.getLlms().onEach { either ->
            either.onRight { llm ->
                _state.update { it.copy(allLlm = llm, isAllLlmLoading = false) }
            }.onLeft { error ->
                _state.update { it.copy(isAllLlmError = error, isAllLlmLoading = false) }
            }
        }.launchIn(viewModelScope)

        llmRepository.getLlms(LlmSource.Default).onEach { either ->
            either.onRight { llm ->
                _state.update { it.copy(defaultLlm = llm, isDefaultLlmLoading = false) }
            }.onLeft { error ->
                _state.update { it.copy(isDefaultLlmError = error, isDefaultLlmLoading = false) }
            }
        }.launchIn(viewModelScope)

        llmRepository.getLlms(LlmSource.Url).onEach { either ->
            either.onRight { llm ->
                _state.update { it.copy(urlLlm = llm, isUrlLlmLoading = false) }
            }.onLeft { error ->
                _state.update { it.copy(isUrlLlmError = error, isUrlLlmLoading = false) }
            }
        }.launchIn(viewModelScope)

        llmRepository.getLlms(LlmSource.Local).onEach { either ->
            either.onRight { llm ->
                _state.update { it.copy(localLlm = llm, isLocalLlmLoading = false) }
            }.onLeft { error ->
                _state.update { it.copy(isLocalLlmError = error, isLocalLlmLoading = false) }
            }
        }.launchIn(viewModelScope)
    }

    fun onAction(action: LlmAction) {
        when (action) {
            LlmAction.DeleteOrphanFileButton -> deleteOrphanButton()
            LlmAction.NotificationPermissionDeniedBottomSheet -> {
                _state.update { it.copy(isNotificationPermissionDeniedBottomSheetVisible = !it.isNotificationPermissionDeniedBottomSheetVisible) }
            }
            is LlmAction.ToggleDownload -> toggleDownload(action.llmEntity)
        }
    }

    private fun deleteOrphanButton() {
        llmRepository.deleteOrphanFile().onEach { either ->
            either.onRight { message ->
                _effect.send(Effect.ShowSnackBar(message))
            }.onLeft { error ->
                _effect.send(Effect.ShowSnackBar(error))
            }
        }.launchIn(viewModelScope)
    }

    private fun toggleDownload(llm: LlmEntity) {
        if (!_state.value.isNotificationPermissionGranted) {
            _state.update { it.copy(isNotificationPermissionDeniedBottomSheetVisible = true) }
        } else {
            llmRepository.toggleDownload(llm).onEach { either ->
                either.onLeft { error ->
                    _effect.send(Effect.ShowSnackBar(error))
                }
            }.launchIn(viewModelScope)
        }
    }
}