package com.a.agent.presentation.upsertlocalllm

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.a.agent.data.local.ModelSource
import com.a.agent.domain.repository.LlmModelManagerRepository
import com.a.agent.presentation.navigation.Event
import com.a.agent.presentation.navigation.NavigationDisplayEvent
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.toAndroidUri
import io.github.vinceglb.filekit.name
import io.github.vinceglb.filekit.size
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.uuid.Uuid

class UpsertLocalModelViewModel(
    val modelId: String,
    private val application: Application,
    private val llmModelManagerRepository: LlmModelManagerRepository,
    private val navigationDisplayEvent: NavigationDisplayEvent
): ViewModel() {
    private val _state = MutableStateFlow(UpsertLocalModelState())
    val state = _state.onStart {
        initialize()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UpsertLocalModelState())

    private fun initialize() {
        viewModelScope.launch {
            if (modelId.isNotBlank()) {
                llmModelManagerRepository.getModel(modelId).onStart {
                    _state.update { it.copy(isModelLoading = true) }
                }.first().onRight { modelEntity ->
                    _state.update {
                        it.copy(
                            isOnEdit = true,
                            model = modelEntity,
                            isModelSupported = true,
                            isModelLoading = false
                        )
                    }
                }.onLeft { error ->
                    _state.update { it.copy(isModelLoading = false, isModelError = error) }
                }
            }
        }
    }

    fun onAction(action: UpsertLocalModelAction) {
        when (action) {
            is UpsertLocalModelAction.NameTextField -> {
                _state.update { it.copy(model = it.model.copy(name = action.name)) }
            }
            is UpsertLocalModelAction.FilePickerButton -> filePickerButton(action.file)
            UpsertLocalModelAction.DeleteModelButton -> deleteModelButton()
            UpsertLocalModelAction.UpsertModelButton -> upsertModelButton()
        }
    }

    private fun filePickerButton(platformFile: PlatformFile?) = viewModelScope.launch {
        if (platformFile != null) {
            _state.update {
                it.copy(
                    modelPlatformFile = platformFile,
                    model = it.model.copy(
                        fileName = platformFile.name,
                        totalBytes = platformFile.size(),
                    ),
                    isModelSupported = platformFile.name.endsWith(".litertlm", ignoreCase = true)
                )
            }
        }
    }

    private fun deleteModelButton() = viewModelScope.launch {
        llmModelManagerRepository.deleteModel(_state.value.model).collect { either ->
            either.onRight {
                navigationDisplayEvent.sendEvent(Event.PopBackStack)
            }.onLeft { error ->
                navigationDisplayEvent.sendEvent(Event.ShowSnackBar(error))
            }
        }
    }

    private fun upsertModelButton() = viewModelScope.launch {
        _state.update { it.copy(isUpsertModelLoading = true) }
        val modelPlatformFile = _state.value.modelPlatformFile
        val modelEntity = when {
            modelPlatformFile != null -> {
                _state.value.model.copy(
                    id = Uuid.random().toString(),
                    path = importModel(modelPlatformFile),
                    modelSource = ModelSource.Local,
                    isDownloaded = true
                )
            }
            else -> _state.value.model
        }
        llmModelManagerRepository.upsertModel(modelEntity).collect { either ->
            either.onRight {
                _state.update { it.copy(isUpsertModelLoading = false) }
                navigationDisplayEvent.sendEvent(Event.PopBackStack)

            }.onLeft { error ->
                _state.update { it.copy(isUpsertModelLoading = false) }
                navigationDisplayEvent.sendEvent(Event.ShowSnackBar(error))
            }
        }
    }

    private suspend fun importModel(platformFile: PlatformFile): String = withContext(Dispatchers.IO) {
        val targetFile = File(application.getExternalFilesDir(null), "model" + File.separator + platformFile.name)
        val parentDir = targetFile.parentFile
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs()
        }

        application.contentResolver.openInputStream(platformFile.toAndroidUri()).use { inputStream ->
            targetFile.outputStream().use { outputStream ->
                inputStream?.copyTo(outputStream)
                outputStream.flush()
            }
        }
        targetFile.absolutePath
    }
}