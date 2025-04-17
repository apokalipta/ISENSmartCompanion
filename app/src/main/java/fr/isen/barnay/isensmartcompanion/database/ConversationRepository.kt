package fr.isen.barnay.isensmartcompanion.database

import kotlinx.coroutines.flow.Flow

class ConversationRepository(private val conversationDao: ConversationDao) {
    
    val allConversations: Flow<List<ConversationEntity>> = conversationDao.getAllConversations()
    
    suspend fun insert(userMessage: String, aiResponse: String) {
        val conversation = ConversationEntity(
            userMessage = userMessage,
            aiResponse = aiResponse
        )
        conversationDao.insert(conversation)
    }
    
    suspend fun deleteConversation(conversation: ConversationEntity) {
        conversationDao.deleteConversation(conversation)
    }
    
    suspend fun deleteAllConversations() {
        conversationDao.deleteAllConversations()
    }
}
