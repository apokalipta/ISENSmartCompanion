package fr.isen.barnay.isensmartcompanion.api

import android.util.Log
import fr.isen.barnay.isensmartcompanion.models.Event
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/**
 * API pour gérer les événements dans l'application
 */
object EventApi {
    private const val TAG = "EventApi"
    private const val API_URL = "https://isen-smart-companion-default-rtdb.europe-west1.firebasedatabase.app/events.json"

    suspend fun getEvents(): List<Event> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Tentative de récupération des événements depuis: $API_URL")
            
            val url = URL(API_URL)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.setRequestProperty("Accept", "application/json")
            connection.connectTimeout = 15000
            connection.readTimeout = 15000
            
            connection.connect()
            
            val responseCode = connection.responseCode
            Log.d(TAG, "Code de réponse: $responseCode")
            
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val response = connection.inputStream.bufferedReader().readText()
                Log.d(TAG, "Réponse reçue de longueur: ${response.length}")
                
                return@withContext parseEventsFromJson(response)
            } else {
                val errorStream = connection.errorStream
                val errorResponse = errorStream?.bufferedReader()?.readText() ?: "Pas de message d'erreur"
                Log.e(TAG, "Erreur HTTP $responseCode: $errorResponse")
                return@withContext emptyList<Event>()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception lors de la récupération des événements", e)
            e.printStackTrace()
            return@withContext emptyList<Event>()
        }
    }
    
    private fun parseEventsFromJson(jsonString: String): List<Event> {
        val events = mutableListOf<Event>()
        try {
            if (jsonString.isBlank() || jsonString == "null") {
                Log.e(TAG, "JSON vide ou null")
                return emptyList()
            }
            
            val jsonObject = JSONObject(jsonString)
            
            if (jsonObject.length() == 0) {
                Log.e(TAG, "Objet JSON vide")
                return emptyList()
            }
            
            val iterator = jsonObject.keys()
            while (iterator.hasNext()) {
                val key = iterator.next()
                
                val eventJson = jsonObject.optJSONObject(key)
                if (eventJson == null) {
                    Log.e(TAG, "Valeur pour la clé '$key' n'est pas un objet JSON")
                    continue
                }
                
                try {
                    val event = Event(
                        id = key,
                        title = eventJson.optString("title", ""),
                        description = eventJson.optString("description", ""),
                        date = eventJson.optString("date", ""),
                        location = eventJson.optString("location", ""),
                        category = eventJson.optString("category", "")
                    )
                    events.add(event)
                } catch (e: Exception) {
                    Log.e(TAG, "Erreur lors du parsing d'un événement", e)
                }
            }
            
            Log.d(TAG, "Total des événements parsés: ${events.size}")
        } catch (e: Exception) {
            Log.e(TAG, "Exception générale lors du parsing JSON", e)
            
            try {
                val jsonArray = JSONArray(jsonString)
                
                for (i in 0 until jsonArray.length()) {
                    try {
                        val eventJson = jsonArray.getJSONObject(i)
                        val id = eventJson.optString("id", i.toString())
                        val title = eventJson.optString("title", "")
                        val description = eventJson.optString("body", eventJson.optString("description", ""))
                        
                        events.add(
                            Event(
                                id = id,
                                title = title,
                                description = description,
                                date = eventJson.optString("date", "Non spécifié"),
                                location = eventJson.optString("location", "Non spécifié"),
                                category = eventJson.optString("category", "Général")
                            )
                        )
                    } catch (e: Exception) {
                        Log.e(TAG, "Erreur lors du parsing d'un élément du tableau", e)
                    }
                }
            } catch (e2: Exception) {
                Log.e(TAG, "Le contenu n'est ni un objet ni un tableau JSON valide", e2)
            }
        }
        
        return events
    }
}
