package fr.isen.barnay.isensmartcompanion.models

/**
 * Classe représentant un échange de message dans la conversation
 * @param userMessage Le message de l'utilisateur
 * @param responseMessage La réponse de l'IA
 */
data class MessageExchange(
    val userMessage: String,
    val responseMessage: String
)
