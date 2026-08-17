@file:OptIn(ExperimentalCoroutinesApi::class)

package com.a.agent.presentation.conversation

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.a.agent.domain.model.ProcessStatus
import com.a.agent.domain.repository.ConversationRepository
import com.a.agent.domain.repository.EngineRepository
import com.a.agent.domain.repository.PermissionRepository
import com.a.agent.domain.repository.SttRepository
import com.a.agent.domain.repository.TtsRepository
import com.a.agent.presentation.navigation.Effect
import com.a.agent.presentation.util.saveToCacheDir
import io.github.vinceglb.filekit.PlatformFile
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale

class ConversationViewModel(
    val conversationId: String,
    private val application: Application,
    private val permissionRepository: PermissionRepository,
    private val engineRepository: EngineRepository,
    private val conversationRepository: ConversationRepository,
    private val ttsRepository: TtsRepository,
    private val sttRepository: SttRepository
): ViewModel() {
    private val _state = MutableStateFlow(ConversationState())
    val state = _state.onStart {
        initialize()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ConversationState())

    private val _effect = Channel<Effect>(Channel.CONFLATED)
    val effect = _effect.receiveAsFlow()

    private fun initialize() {
        permissionRepository.isMicrophonePermissionGranted.onEach { isGranted ->
            _state.update { it.copy(isMicrophonePermissionGranted = isGranted) }
        }.launchIn(viewModelScope)

        engineRepository.initializeConversation(conversationId).onEach { either ->
            either.onRight {
                _state.update {
                    it.copy(
                        isEngineConversationLoading = false,
                        isEngineConversationReady = true
                    )
                }
            }.onLeft { error ->
                _state.update {
                    it.copy(
                        isEngineConversationLoading = false,
                        isEngineConversationError = error,
                        isEngineConversationReady = false
                    )
                }
            }
        }.launchIn(viewModelScope)

        conversationRepository.getConversation(conversationId).onEach { either ->
            either.onRight { conversationEntity ->
                _state.update { it.copy(isConversationLoading = false, conversation = conversationEntity) }
            }.onLeft { error ->
                _state.update { it.copy(isConversationLoading = false, isConversationError = error) }
            }
        }.launchIn(viewModelScope)

        conversationRepository.getChats(conversationId).onEach { either ->
            either.onRight { chatEntities ->
                _state.update { it.copy(isChatLoading = false, chats = chatEntities) }
            }.onLeft { error ->
                _state.update { it.copy(isChatLoading = false, isChatError = error) }
            }
        }.launchIn(viewModelScope)

        ttsRepository.selectedLanguage.onEach { locale ->
            _state.update { it.copy(selectedTtsLanguage = locale) }
        }.launchIn(viewModelScope)

        ttsRepository.initialize().onEach { either ->
            either.onRight { processStatus ->
                when (processStatus) {
                    is ProcessStatus.OnProcess -> {
                        val convertedAndSortedLocale = processStatus.process.toList().sortedBy { locale ->
                            locale.displayLanguage
                        }
                        _state.update { it.copy(ttsLanguages = convertedAndSortedLocale, sttLanguage = convertedAndSortedLocale) }
                    }
                    ProcessStatus.OnCompletion -> {
                        _state.update { it.copy(isTtsLoading = false) }
                    }
                }
            }.onLeft { error ->
                _state.update { it.copy(isTtsError = error, isTtsLoading = false) }
            }
        }.launchIn(viewModelScope)

        sttRepository.selectedLanguage.onEach { locale ->
            _state.update { it.copy(selectedSttLanguage = locale) }
        }.launchIn(viewModelScope)
    }

    fun onAction(action: ConversationAction) {
        when (action) {
            ConversationAction.TtsLanguageBottomSheet -> {
                _state.update { it.copy(isTtsLanguageBottomSheetVisible = !it.isTtsLanguageBottomSheetVisible) }
            }
            is ConversationAction.SetTtsLanguageButton -> setTtsLanguageButton(action.locale)
            ConversationAction.SttLanguageBottomSheet -> {
                _state.update { it.copy(isSttLanguageBottomSheetVisible = !it.isSttLanguageBottomSheetVisible) }
            }
            is ConversationAction.SetSttLanguageButton -> setSttLanguageButton(action.locale)
            ConversationAction.DeleteChatBottomSheet -> {
                _state.update { it.copy(isDeleteChatBottomSheetVisible = !it.isDeleteChatBottomSheetVisible) }
            }
            ConversationAction.DeleteChatButton -> deleteChatButton()
            is ConversationAction.StartTtsButton -> startTtsButton(action.text)
            ConversationAction.StopTtsButton -> stopTtsButton()
            ConversationAction.MicrophonePermissionDeniedBottomSheet -> {
                _state.update { it.copy(isMicrophonePermissionDeniedBottomSheetVisible = !it.isMicrophonePermissionDeniedBottomSheetVisible) }
            }
            is ConversationAction.ToggleSttButton -> toggleSttButton(action.isRunning)
            is ConversationAction.ImagePickerButton -> imagePickerButton(action.image)
            is ConversationAction.PromptTextField -> {
                _state.update { it.copy(promptTextField = action.prompt) }
            }
            ConversationAction.GenerateButton -> generateButton()
        }
    }

    private fun setTtsLanguageButton(locale: Locale) {
        ttsRepository.setLanguage(locale).onEach { either ->
            either.onLeft { error ->
                _effect.send(Effect.ShowSnackBar(error))
            }
        }.launchIn(viewModelScope)
    }

    private fun setSttLanguageButton(locale: Locale) {
        sttRepository.setTtsLanguage(locale).onEach { either ->
            either.onLeft { error ->
                _effect.send(Effect.ShowSnackBar(error))
            }
        }.launchIn(viewModelScope)
    }

    private fun deleteChatButton() {
        conversationRepository.clearChats(conversationId).onEach { either ->
            either.onLeft { error ->
                _effect.send(Effect.ShowSnackBar(error))
            }
        }.launchIn(viewModelScope)
    }

    private fun startTtsButton(text: String) {
        ttsRepository.start(text).onEach { either ->
            either.onRight { processStatus ->
                when (processStatus) {
                    is ProcessStatus.OnProcess -> {
                        _state.update { it.copy(isTtsRunning = true) }
                    }
                    ProcessStatus.OnCompletion -> {
                        _state.update { it.copy(isTtsRunning = false) }
                    }
                }
            }.onLeft { error ->
                _effect.send(Effect.ShowSnackBar(error))
            }
        }.launchIn(viewModelScope)
    }

    private fun stopTtsButton() {
        _state.update { it.copy(isTtsRunning = false) }
        ttsRepository.stop()
    }

    private fun toggleSttButton(isRunning: Boolean) {
        if (isRunning) {
            sttRepository.start().onStart {
                _state.update { it.copy(isSttRunning = true) }
                stopTtsButton()
            }.onEach { either ->
                either.onRight { processStatus ->
                    if (processStatus is ProcessStatus.OnProcess) {
                        if (processStatus.process.isNotBlank()) {
                            _state.update {
                                it.copy(
                                    promptTextField = processStatus.process,
                                    isSttRunning = false
                                )
                            }
                        }
                    }
                }.onLeft { error ->
                    _state.update { it.copy(isSttRunning = false) }
                }
            }.launchIn(viewModelScope)
        } else {
            _state.update { it.copy(isSttRunning = false) }
            sttRepository.stop()
        }
    }

    private fun imagePickerButton(image: PlatformFile?) {
        viewModelScope.launch {
            val imagePath = image?.saveToCacheDir(application, ".jpg")
            _state.update { it.copy(imagePicker = imagePath) }
        }
    }

    private fun generateButton() {
        engineRepository.generateResponse(
            conversationId = conversationId,
            prompt = _state.value.promptTextField,
            image = _state.value.imagePicker
        ).onStart {
            _state.update { it.copy(isGenerating = true) }
        }.onEach { either ->
            either.onRight { processStatus ->
                when (processStatus) {
                    is ProcessStatus.OnProcess -> {
                        _state.update { it.copy(imagePicker = null, promptTextField = "") }
                    }
                    ProcessStatus.OnCompletion -> {
                        _state.update { it.copy(isGenerating = false) }
                    }
                }
            }.onLeft { error ->
                _effect.send(Effect.ShowSnackBar(error))
                _state.update { it.copy(isGenerating = false) }
            }
        }.launchIn(viewModelScope)
    }

    override fun onCleared() {
        engineRepository.destroyConversation()
        ttsRepository.destroy()
    }
}