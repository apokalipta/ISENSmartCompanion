package fr.isen.barnay.isensmartcompanion.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.isen.barnay.isensmartcompanion.ai.GeminiManager
import fr.isen.barnay.isensmartcompanion.models.MessageExchange
import fr.isen.barnay.isensmartcompanion.ui.theme.ISENRed
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.lifecycle.viewmodel.compose.viewModel
import fr.isen.barnay.isensmartcompanion.viewmodels.ConversationViewModel
import androidx.compose.ui.text.input.ImeAction


@Composable
fun HomeScreen() {
    val messageHistory = remember { mutableStateListOf<MessageExchange>() }
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    
    // Initialiser GeminiManager
    val geminiManager = remember { GeminiManager() }
    
    // Initialiser le ViewModel pour la persistance des conversations
    val conversationViewModel: ConversationViewModel = viewModel()
    
    // État pour suivre si une requête est en cours
    var isLoading by remember { mutableStateOf(false) }
    
    // Scope pour lancer des coroutines
    val coroutineScope = rememberCoroutineScope()
    
    // Pour faire défiler automatiquement vers le bas quand un nouveau message est ajouté
    LaunchedEffect(messageHistory.size) {
        scrollState.animateScrollTo(scrollState.maxValue)
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Titre en haut
            Text(
                text = "ISEN",
                color = ISENRed,
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 40.dp)
            )
            
            Text(
                text = "Smart Companion",
                color = Color.Gray,
                fontSize = 20.sp,
                modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
            )
            
            // Affichage de l'historique des échanges
            if (messageHistory.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    messageHistory.forEach { exchange ->
                        // Message de l'utilisateur (aligné à droite)
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalAlignment = Alignment.End
                        ) {
                            Text(
                                text = "Vous :",
                                fontWeight = FontWeight.Bold,
                                color = Color.DarkGray,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(end = 8.dp, top = 8.dp)
                            )
                            
                            Surface(
                                modifier = Modifier
                                    .padding(start = 60.dp, end = 0.dp, bottom = 4.dp),
                                shape = RoundedCornerShape(16.dp),
                                color = Color(0xFFE7F9FF)
                            ) {
                                Text(
                                    text = exchange.userMessage,
                                    color = Color.DarkGray,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }
                        
                        // Réponse du système (aligné à gauche)
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalAlignment = Alignment.Start
                        ) {
                            Text(
                                text = "Smart Companion :",
                                fontWeight = FontWeight.Bold,
                                color = Color.DarkGray,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(start = 8.dp, top = 8.dp)
                            )
                            
                            Surface(
                                modifier = Modifier
                                    .padding(end = 60.dp, start = 0.dp, bottom = 4.dp),
                                shape = RoundedCornerShape(16.dp),
                                color = Color.LightGray.copy(alpha = 0.2f)
                            ) {
                                // Utilisation du composant d'animation de texte
                                AnimatedText(
                                    text = exchange.responseMessage,
                                    color = Color.DarkGray,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            } else {
                // Message d'information lorsqu'il n'y a pas encore de messages
                Text(
                    text = "Posez une question pour commencer la conversation avec l'IA Gemini",
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .padding(32.dp)
                        .fillMaxWidth()
                )
            }
            
            // Espace en bas pour éviter que le champ de texte ne cache le dernier message
            Spacer(modifier = Modifier.height(80.dp))
        }
        
        // Indicateur de chargement
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = ISENRed,
                    modifier = Modifier.padding(bottom = 100.dp)
                )
            }
        }
        
        // Champ de texte avec bouton en bas
        ChatInputField(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            onMessageSent = { userMessage ->
                // Ajout immédiat du message utilisateur avec une réponse temporaire
                messageHistory.add(MessageExchange(userMessage, "Génération de la réponse en cours..."))
                
                // Affichage de l'indicateur de chargement
                isLoading = true
                
                coroutineScope.launch {
                    try {
                        // Obtention de la réponse via Gemini
                        val response = geminiManager.generateResponse(userMessage)
                        
                        // Mise à jour du message avec la vraie réponse
                        val updatedMessages = messageHistory.toMutableList()
                        updatedMessages[updatedMessages.lastIndex] = MessageExchange(userMessage, response)
                        messageHistory.clear()
                        messageHistory.addAll(updatedMessages)
                        
                        // Sauvegarde de la conversation dans la base de données
                        conversationViewModel.insertConversation(userMessage, response)
                    } catch (e: Exception) {
                        // Gestion des erreurs
                        val errorMessages = messageHistory.toMutableList()
                        errorMessages[errorMessages.lastIndex] = MessageExchange(
                            userMessage, 
                            "Désolé, une erreur s'est produite: ${e.message ?: "erreur inconnue"}"
                        )
                        messageHistory.clear()
                        messageHistory.addAll(errorMessages)
                    } finally {
                        // Désactivation de l'indicateur de chargement
                        isLoading = false
                    }
                }
            },
            isEnabled = !isLoading
        )
    }
}

@Composable
fun AnimatedText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.DarkGray,
    animationDurationMillis: Long = 20 // Durée par caractère en millisecondes
) {
    var visibleCharacters by remember { mutableIntStateOf(0) }
    val visibleText = text.take(visibleCharacters)
    
    // Animation pour afficher progressivement le texte
    LaunchedEffect(text) {
        visibleCharacters = 0
        val textLength = text.length
        
        while (visibleCharacters < textLength) {
            delay(animationDurationMillis)
            visibleCharacters++
        }
    }
    
    Text(
        text = visibleText,
        color = color,
        modifier = modifier
    )
}

@Composable
fun ChatInputField(
    modifier: Modifier = Modifier,
    onMessageSent: (String) -> Unit,
    isEnabled: Boolean = true
) {
    var text by remember { mutableStateOf("") }
    val context = LocalContext.current
    
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = Color.LightGray.copy(alpha = 0.2f)
    ) {
        TextField(
            value = text,
            onValueChange = { text = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Posez votre question...") },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            enabled = isEnabled,
            trailingIcon = {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = if (!isEnabled) Color.Gray else ISENRed,
                    modifier = Modifier.padding(2.dp)
                ) {
                    IconButton(
                        onClick = {
                            if (text.isNotEmpty() && isEnabled) {
                                Toast.makeText(context, "Question envoyée", Toast.LENGTH_SHORT).show()
                                onMessageSent(text)
                                text = ""
                            }
                        },
                        enabled = isEnabled
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Send",
                            tint = Color.White
                        )
                    }
                }
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
            keyboardActions = KeyboardActions(
                onSend = {
                    if (text.isNotEmpty() && isEnabled) {
                        Toast.makeText(context, "Question envoyée", Toast.LENGTH_SHORT).show()
                        onMessageSent(text)
                        text = ""
                    }
                }
            ),
            shape = RoundedCornerShape(28.dp)
        )
    }
}
