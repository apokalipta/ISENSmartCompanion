package fr.isen.barnay.isensmartcompanion.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import fr.isen.barnay.isensmartcompanion.database.AppDatabase
import fr.isen.barnay.isensmartcompanion.database.ConversationEntity
import fr.isen.barnay.isensmartcompanion.database.ConversationRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

/**
 * ViewModel pour gérer les conversations avec l'IA
 */
class ConversationViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository: ConversationRepository
    val allConversations: Flow<List<ConversationEntity>>
    
    init {
        val conversationDao = AppDatabase.getDatabase(application).conversationDao()
        repository = ConversationRepository(conversationDao)
        allConversations = repository.allConversations
    }
    
    /**
     * Insère une nouvelle conversation dans la base de données
     */
    fun insertConversation(userMessage: String, aiResponse: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insert(userMessage, aiResponse)
        }
    }
    
    /**
     * Supprime une conversation spécifique
     */
    fun deleteConversation(conversation: ConversationEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteConversation(conversation)
        }
    }
    
    /**
     * Supprime toutes les conversations
     */
    fun deleteAllConversations() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteAllConversations()
        }
    }
}
