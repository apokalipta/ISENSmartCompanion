package fr.isen.barnay.isensmartcompanion.utils

// Fonction pour générer une réponse en fonction de la question
fun generateResponse(question: String): String {
    return when {
        question.contains("bonjour", ignoreCase = true) -> "Bonjour ! Comment puis-je vous aider aujourd'hui ?"
        question.contains("heure", ignoreCase = true) -> "Il est l'heure de consulter votre montre !"
        question.contains("isen", ignoreCase = true) -> "ISEN est une école d'ingénieurs en France."
        question.contains("android", ignoreCase = true) -> "Android est un système d'exploitation mobile développé par Google."
        question.contains("kotlin", ignoreCase = true) -> "Kotlin est un langage de programmation moderne utilisé pour le développement Android."
        question.contains("jetpack compose", ignoreCase = true) -> "Jetpack Compose est une boîte à outils moderne pour créer des interfaces utilisateur Android."
        question.length < 5 -> "Pourriez-vous me donner plus de détails ?"
        else -> "Je ne suis pas sûr de comprendre votre question. Pouvez-vous reformuler ?"
    }
}
