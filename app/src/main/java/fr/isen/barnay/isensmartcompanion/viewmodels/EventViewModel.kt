package fr.isen.barnay.isensmartcompanion.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import fr.isen.barnay.isensmartcompanion.api.EventApi
import fr.isen.barnay.isensmartcompanion.models.Event
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class EventViewModel() : ViewModel() {
    private val tag = "EventViewModel"
    private val _eventsState = MutableStateFlow<EventsState>(EventsState.Loading)
    val eventsState: StateFlow<EventsState> = _eventsState.asStateFlow()

    init {
        fetchEvents()
    }

    fun refreshEvents() {
        _eventsState.value = EventsState.Loading
        fetchEvents()
    }

    private fun fetchEvents() {
        viewModelScope.launch {
            try {
                val events = EventApi.getEvents()
                
                if (events.isNotEmpty()) {
                    Log.d(tag, "Événements récupérés avec succès: ${events.size}")
                    _eventsState.value = EventsState.Success(events)
                } else {
                    Log.e(tag, "Aucun événement trouvé")
                    _eventsState.value = EventsState.Error("Aucun événement trouvé. Vérifiez la connexion et réessayez.")
                }
            } catch (e: Exception) {
                Log.e(tag, "Erreur lors de la récupération des événements", e)
                _eventsState.value = EventsState.Error("Erreur: ${e.message ?: "Erreur inconnue"}")
            }
        }
    }

    fun getEventById(id: String): Event? {
        return when (val state = eventsState.value) {
            is EventsState.Success -> state.events.find { it.id == id }
            else -> null
        }
    }

    sealed class EventsState {
        object Loading : EventsState()
        data class Success(val events: List<Event>) : EventsState()
        data class Error(val message: String) : EventsState()
    }

    class Factory() : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(EventViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return EventViewModel() as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
