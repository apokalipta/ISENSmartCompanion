package fr.isen.barnay.isensmartcompanion.ai

import android.util.Log
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.generationConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GeminiManager {
    companion object {
        private const val TAG = "GeminiManager"
        private const val API_KEY = "AIzaSyDgDvhg8mX5l4HhOVkk4Xmfk7NF1X2sQTQ"
        private const val MODEL_NAME = "gemini-1.5-flash"
    }

    private val config = generationConfig {
        temperature = 0.7f
        topK = 32
        topP = 0.95f
        maxOutputTokens = 1024
    }

    private val generativeModel by lazy {
        GenerativeModel(
            modelName = MODEL_NAME,
            apiKey = API_KEY,
            generationConfig = config
        )
    }

    suspend fun generateResponse(inputText: String): String = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Generating response for: $inputText")
            val prompt = "Réponds en français de manière concise et informative à cette question: $inputText"
            val response = generativeModel.generateContent(prompt)
            val responseText = response.text
            Log.d(TAG, "Response received: $responseText")
            return@withContext responseText ?: "Désolé, je n'ai pas pu générer de réponse."
            
        } catch (e: Exception) {
            Log.e(TAG, "Error in generateResponse: ${e.message}", e)
            
            return@withContext "Erreur lors de la connexion à l'API Gemini: ${e.message}"
        }
    }
}
