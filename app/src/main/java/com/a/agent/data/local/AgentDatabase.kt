package com.a.agent.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        ModelEntity::class,
        ConversationEntity::class,
        ChatEntity::class
    ],
    version = 8
)
@TypeConverters(Converter::class)
abstract class AgentDatabase: RoomDatabase() {
    abstract val modelDao: ModelDao
    abstract val conversationDao: ConversationDao
    abstract val chatDao: ChatDao
}
