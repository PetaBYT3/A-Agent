package com.a.agent.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        ModelEntity::class,
        ChatEntity::class
    ],
    version = 3
)
@TypeConverters(Converter::class)
abstract class AgentDatabase(): RoomDatabase() {
    abstract val modelDao: ModelDao
    abstract val chatDao: ChatDao
}
