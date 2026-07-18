package com.a.agent.data.local

import android.app.Application
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.File

class AgentDatabaseCallback(
    private val application: Application
): RoomDatabase.Callback(), KoinComponent {
    private val agentDatabase: AgentDatabase by inject()

    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)

        CoroutineScope(Dispatchers.IO).launch { 
            val initialModel = listOf(
                ModelEntity(
                    name = "Gemma 4 E2B",
                    url = "https://huggingface.co/litert-community/gemma-4-E2B-it-litert-lm/resolve/main/gemma-4-E2B-it.litertlm",
                    path = File(application.getExternalFilesDir(null), "model" + File.separator + "gemma-4-E2B-it.litertlm").absolutePath,
                    fileName = "gemma-4-E2B-it.litertlm",
                    totalBytes = 0,
                    isDefaultModel = true
                )
            )
            initialModel.forEach { modelEntity ->
                agentDatabase.modelDao.upsertModel(modelEntity)
            }
        }
    }
}