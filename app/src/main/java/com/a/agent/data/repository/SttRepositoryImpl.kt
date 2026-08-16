@file:RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)

package com.a.agent.data.repository

import android.app.Activity
import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.annotation.RequiresApi
import arrow.core.Either
import com.a.agent.data.local.DataStore
import com.a.agent.data.mapper.toMessage
import com.a.agent.domain.model.ProcessStatus
import com.a.agent.domain.repository.SttRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.util.Locale

class SttRepositoryImpl(
    private val application: Application,
    private val dataStore: DataStore
): SttRepository {
    private var sst: SpeechRecognizer? = null

    override val selectedLanguage: Flow<Locale> = dataStore.sttLanguage

    override fun getSstLanguages(): Flow<List<Locale>> {
        return callbackFlow {
            val recognizerIntent = Intent(RecognizerIntent.ACTION_GET_LANGUAGE_DETAILS)

            val speechIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            val resolveInfo = application.packageManager.resolveActivity(speechIntent, 0)
            val sttPackageName = resolveInfo?.activityInfo?.packageName

            if (sttPackageName != null) {
                recognizerIntent.setPackage(sttPackageName)
            } else {
                recognizerIntent.setPackage("com.google.android.googlequicksearchbox")
            }

            application.sendOrderedBroadcast(
                recognizerIntent,
                null,
                object : BroadcastReceiver() {
                    override fun onReceive(context: Context?, intent: Intent?) {
                        val results = getResultExtras(true)
                        val supportedLanguage = results.getStringArrayList(RecognizerIntent.EXTRA_SUPPORTED_LANGUAGES)
                        if (!supportedLanguage.isNullOrEmpty()) {
                            val locales = supportedLanguage.map {
                                Locale.forLanguageTag(it)
                            }.sortedBy { it.displayLanguage }

                            trySend(locales)
                        } else {
                            trySend(emptyList())
                        }
                        close()
                    }
                },
                null,
                Activity.RESULT_OK,
                null,
                null
            )

            awaitClose {}
        }.flowOn(Dispatchers.IO)
    }

    override fun setTtsLanguage(locale: Locale): Flow<Either<String, Unit>> {
        return flow<Either<String, Unit>> {
            dataStore.setSttLanguage(locale)
            emit(Either.Right(Unit))
        }.catch { throwable ->
            emit(Either.Left(throwable.toMessage()))
        }.flowOn(Dispatchers.IO)
    }

    override fun start(): Flow<Either<String, ProcessStatus<String>>> {
        return callbackFlow {
            sst = SpeechRecognizer.createSpeechRecognizer(application)
            sst?.setRecognitionListener(object : RecognitionListener {
                override fun onBeginningOfSpeech() {}
                override fun onBufferReceived(p0: ByteArray?) {}
                override fun onEndOfSpeech() {}
                override fun onEvent(p0: Int, p1: Bundle?) {}
                override fun onReadyForSpeech(p0: Bundle?) {}
                override fun onRmsChanged(p0: Float) {}
                override fun onPartialResults(p0: Bundle?) {}

                override fun onResults(p0: Bundle?) {
                    val matches = p0?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    if (!matches.isNullOrEmpty()) {
                        val result = matches[0]
                        trySend(Either.Right(ProcessStatus.OnProcess(result)))
                    }
                    close()
                }

                override fun onError(p0: Int) {
                    trySend(Either.Left("SST Failure"))
                    close()
                }
            })

            val sstIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, dataStore.sttLanguage.first().toLanguageTag())
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            }
            sst?.startListening(sstIntent)

            awaitClose {
                sst?.stopListening()
                sst?.destroy()
                sst = null
            }
        }.catch { throwable ->
            emit(Either.Left(throwable.toMessage()))
        }.flowOn(Dispatchers.Main)
    }

    override fun stop() {
        sst?.stopListening()
    }

    override fun destroy() {
        sst?.destroy()
        sst = null
    }
}