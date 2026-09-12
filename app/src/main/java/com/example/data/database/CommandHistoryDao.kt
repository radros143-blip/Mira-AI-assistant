package com.example.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CommandHistoryDao {
    @Query("SELECT * FROM command_history ORDER BY timestamp DESC LIMIT 50")
    fun getAllHistory(): Flow<List<CommandHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(item: CommandHistoryEntity): Long

    @Query("DELETE FROM command_history")
    suspend fun clearHistory()
}
