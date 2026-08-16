package com.a.agent.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.a.agent.data.local.chat.ChatDao
import com.a.agent.data.local.chat.ChatEntity
import com.a.agent.data.local.conversation.ConversationDao
import com.a.agent.data.local.conversation.ConversationEntity
import com.a.agent.data.local.llm.LlmDao
import com.a.agent.data.local.llm.LlmEntity

@Database(
    entities = [
        LlmEntity::class,
        ConversationEntity::class,
        ChatEntity::class
    ],
    version = 9
)
@TypeConverters(Converter::class)
abstract class Database: RoomDatabase() {
    abstract val llmDao: LlmDao
    abstract val conversationDao: ConversationDao
    abstract val chatDao: ChatDao
}
