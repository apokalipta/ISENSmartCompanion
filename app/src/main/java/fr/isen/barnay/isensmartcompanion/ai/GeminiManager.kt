package fr.isen.barnay.isensmartcompanion.ai

import android.util.Log
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.generationConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Gestionnaire pour l'API Gemini utilisant le SDK Google AI Client
 */
class GeminiManager {
    companion object {
        private const val TAG = "GeminiManager"
        private const val API_KEY = "AIzaSyDgDvhg8mX5l4HhOVkk4Xmfk7NF1X2sQTQ"
        // Utilisation du modèle Gemini 1.5 flash comme demandé dans l'énoncé
        private const val MODEL_NAME = "gemini-1.5-flash"
    }

    // Configuration du modèle
    private val config = generationConfig {
        temperature = 0.7f
        topK = 32
        topP = 0.95f
        maxOutputTokens = 1024
    }

    // Initialisation du modèle Gemini avec le SDK Google AI Client
    private val generativeModel by lazy {
        GenerativeModel(
            modelName = MODEL_NAME,
            apiKey = API_KEY,
            generationConfig = config
        )
    }

    /**
     * Génère une réponse à partir d'un texte d'entrée en utilisant l'API Gemini
     */
    suspend fun generateResponse(inputText: String): String = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Generating response for: $inputText")
            
            // Construction du prompt en français
            val prompt = "Réponds en français de manière concise et informative à cette question: $inputText"
            
            // Appel à l'API Gemini via le SDK Google AI Client
            val response = generativeModel.generateContent(prompt)
            
            // Récupération du texte de la réponse
            val responseText = response.text
            
            Log.d(TAG, "Response received: $responseText")
            
            return@withContext responseText ?: "Désolé, je n'ai pas pu générer de réponse."
            
        } catch (e: Exception) {
            Log.e(TAG, "Error in generateResponse: ${e.message}", e)
            
            // Message d'erreur détaillé mais convivial
            return@withContext "Erreur lors de la connexion à l'API Gemini: ${e.message}"
        }
    }
}
