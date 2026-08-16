package com.a.agent.data.repository

import android.app.Application
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import arrow.core.Either
import com.a.agent.data.local.DataStore
import com.a.agent.data.mapper.toMessage
import com.a.agent.domain.model.ProcessStatus
import com.a.agent.domain.repository.TtsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.update
import java.util.Locale
import kotlin.time.Clock

class TtsRepositoryImpl(
    private val application: Application,
    private val dataStore: DataStore
): TtsRepository {
    private var tts: TextToSpeech? = null

    private val _isTtsOnline = MutableStateFlow(false)
    override val isTtsOnline: Flow<Boolean> = _isTtsOnline.asStateFlow()

    override val selectedLanguage: Flow<Locale> = dataStore.ttsLanguage

    override fun setLanguage(locale: Locale): Flow<Either<String, Unit>> {
        return flow<Either<String, Unit>> {
            dataStore.setTtsLanguage(locale)
            tts?.language = dataStore.ttsLanguage.first()
            emit(Either.Right(Unit))
        }.catch { throwable ->
            emit(Either.Left(throwable.toMessage()))
        }.flowOn(Dispatchers.IO)
    }

    override fun initialize(): Flow<Either<String, ProcessStatus<Set<Locale>>>> {
        return callbackFlow {
            if (tts != null && _isTtsOnline.value) {
                trySend(Either.Right(ProcessStatus.OnCompletion))
                close()
                return@callbackFlow
            }
            val ttsLanguage = dataStore.ttsLanguage.first()
            val ttsConfig = TextToSpeech(application) { ttsStatus ->
                if (ttsStatus == TextToSpeech.SUCCESS) {
                    val availableLanguage: Set<Locale> = tts?.availableLanguages ?: emptySet()
                    trySend(Either.Right(ProcessStatus.OnProcess(availableLanguage)))

                    tts?.language = ttsLanguage
                    _isTtsOnline.update { true }
                    trySend(Either.Right(ProcessStatus.OnCompletion))
                    close()
                } else {
                    _isTtsOnline.update { false }
                    trySend(Either.Left("Tts Initialize Failure"))
                    close()
                }
            }
            tts = ttsConfig
            awaitClose()
        }.catch { throwable ->
            emit(Either.Left(throwable.toMessage()))
        }.flowOn(Dispatchers.IO)
    }

    override fun start(text: String): Flow<Either<String, ProcessStatus<Unit>>> {
        return callbackFlow {
            if (tts == null && !_isTtsOnline.value) {
                trySend(Either.Left("Tts Not Initialized"))
                close()
                return@callbackFlow
            }

            val utterancePrefix = "tts${Clock.System.now().toEpochMilliseconds()}"
            val maxLength = TextToSpeech.getMaxSpeechInputLength()
            val textChunks = text.chunked(maxLength - 100)

            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    val currentUtteranceId = "$utterancePrefix${textChunks.indices.first}"
                    if (utteranceId == currentUtteranceId) {
                        trySend(Either.Right(ProcessStatus.OnProcess(Unit)))
                    }
                }

                override fun onDone(utteranceId: String?) {
                    val currentUtteranceId = "$utterancePrefix${textChunks.indices.last}"
                    if (utteranceId == currentUtteranceId) {
                        trySend(Either.Right(ProcessStatus.OnCompletion))
                        close()
                    }
                }

                @Deprecated("Deprecated in Java")
                override fun onError(p0: String?) {
                    trySend(Either.Left("TTS Failure"))
                    close()
                }
            })

            textChunks.forEachIndexed { index, chunk ->
                val utteranceId = "$utterancePrefix$index"
                if (index == 0) {
                    tts?.speak(chunk, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
                } else {
                    tts?.speak(chunk, TextToSpeech.QUEUE_ADD, null, utteranceId)
                }
            }

            awaitClose { tts?.setOnUtteranceProgressListener(null) }
        }.catch { throwable ->
            emit(Either.Left(throwable.toMessage()))
        }.flowOn(Dispatchers.IO)
    }

    override fun stop() {
        tts?.stop()
    }

    override fun destroy() {
        tts?.stop()
        tts?.shutdown()
        tts = null
    }
}