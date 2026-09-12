package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "command_history")
data class CommandHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val queryText: String,
    val intentType: String,
    val responseText: String,
    val isSuccess: Boolean = true,
    val executionDetails: String = ""
)

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sender: String, // "USER" or "MYRA"
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "DELIVERED"
)
