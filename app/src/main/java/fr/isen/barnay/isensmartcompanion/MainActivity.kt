package fr.isen.barnay.isensmartcompanion

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.isen.barnay.isensmartcompanion.ai.GeminiManager
import fr.isen.barnay.isensmartcompanion.navigation.AppNavigation
import fr.isen.barnay.isensmartcompanion.ui.theme.ISENRed
import fr.isen.barnay.isensmartcompanion.ui.theme.ISENSmartCompanionTheme
import kotlinx.coroutines.launch

// Définition d'une classe pour représenter un échange de messages
data class MessageExchange(
    val userMessage: String,
    val responseMessage: String
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ISENSmartCompanionTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }
}

@Composable
fun MainScreen(modifier: Modifier = Modifier) {
    // Utilisation de mutableStateListOf pour conserver l'historique des échanges
    val messageHistory = remember { mutableStateListOf<MessageExchange>() }
    // ScrollState pour permettre le défilement
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    
    // Instance de GeminiManager
    val geminiManager = remember { GeminiManager() }
    
    // État pour suivre si une requête est en cours
    var isLoading by remember { mutableStateOf(false) }
    
    // Scope pour lancer des coroutines
    val coroutineScope = rememberCoroutineScope()
    
    // Pour faire défiler automatiquement vers le bas quand un nouveau message est ajouté
    LaunchedEffect(messageHistory.size) {
        scrollState.animateScrollTo(scrollState.maxValue)
    }
    
    Box(
        modifier = modifier.fillMaxSize()
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
                modifier = Modifier.padding(top = 100.dp)
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
                        // Message de l'utilisateur
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFFE7F9FF)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = "Vous avez demandé :",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.DarkGray
                                )
                                Text(
                                    text = exchange.userMessage,
                                    color = Color.DarkGray,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                        
                        // Réponse du système
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            shape = RoundedCornerShape(16.dp),
                            color = Color.LightGray.copy(alpha = 0.2f)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = "Smart Companion :",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.DarkGray
                                )
                                Text(
                                    text = exchange.responseMessage,
                                    color = Color.DarkGray,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
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
                .padding(16.dp),
            onMessageSent = { userMessage ->
                // Ajout du message de l'utilisateur avec un message de chargement
                val pendingMessage = MessageExchange(userMessage, "Génération de la réponse en cours...")
                messageHistory.add(pendingMessage)
                
                // Affichage de l'indicateur de chargement
                isLoading = true
                
                coroutineScope.launch {
                    try {
                        // Appel à l'API Gemini
                        val response = geminiManager.generateResponse(userMessage)
                        
                        // Mise à jour du message avec la réponse de l'IA
                        val updatedMessages = messageHistory.toMutableList()
                        updatedMessages[updatedMessages.lastIndex] = MessageExchange(userMessage, response)
                        
                        // Mise à jour de la liste des messages
                        messageHistory.clear()
                        messageHistory.addAll(updatedMessages)
                    } catch (e: Exception) {
                        // En cas d'erreur, afficher un message d'erreur
                        val updatedMessages = messageHistory.toMutableList()
                        updatedMessages[updatedMessages.lastIndex] = MessageExchange(
                            userMessage,
                            "Désolé, une erreur s'est produite: ${e.message ?: "erreur inconnue"}"
                        )
                        
                        // Mise à jour de la liste des messages
                        messageHistory.clear()
                        messageHistory.addAll(updatedMessages)
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

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    ISENSmartCompanionTheme {
        MainScreen()
    }
}