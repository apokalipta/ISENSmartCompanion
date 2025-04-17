package fr.isen.barnay.isensmartcompanion.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "conversations")
data class ConversationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userMessage: String,
    val aiResponse: String,
    val timestamp: Long = System.currentTimeMillis()
)
