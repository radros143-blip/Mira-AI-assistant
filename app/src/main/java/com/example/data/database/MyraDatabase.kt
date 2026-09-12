package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [CommandHistoryEntity::class, ChatMessageEntity::class],
    version = 1,
    exportSchema = false
)
abstract class MyraDatabase : RoomDatabase() {
    abstract fun commandHistoryDao(): CommandHistoryDao
    abstract fun chatMessageDao(): ChatMessageDao

    companion object {
        @Volatile
        private var INSTANCE: MyraDatabase? = null

        fun getDatabase(context: Context): MyraDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MyraDatabase::class.java,
                    "myra_assistant_database"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
