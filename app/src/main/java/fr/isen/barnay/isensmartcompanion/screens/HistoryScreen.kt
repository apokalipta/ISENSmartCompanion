package fr.isen.barnay.isensmartcompanion.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import fr.isen.barnay.isensmartcompanion.database.ConversationEntity
import fr.isen.barnay.isensmartcompanion.ui.theme.ISENSmartCompanionTheme
import fr.isen.barnay.isensmartcompanion.viewmodels.ConversationViewModel
import kotlinx.coroutines.flow.collectLatest
import java.text.SimpleDateFormat
import java.util.*

/**
 * Écran d'historique des conversations
 * Affiche les conversations sauvegardées et permet de les supprimer
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen() {
    // Initialisation du ViewModel
    val viewModel: ConversationViewModel = viewModel()
    
    // État pour stocker les conversations
    val conversations = remember { mutableStateListOf<ConversationEntity>() }
    
    // État pour afficher la boîte de dialogue de confirmation
    var showDeleteAllDialog by remember { mutableStateOf(false) }
    var conversationToDelete by remember { mutableStateOf<ConversationEntity?>(null) }
    
    // Collecte des conversations depuis la base de données
    LaunchedEffect(Unit) {
        viewModel.allConversations.collectLatest { list ->
            conversations.clear()
            conversations.addAll(list)
        }
    }
    
    ISENSmartCompanionTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Historique des conversations") },
                    actions = {
                        // Bouton pour supprimer tout l'historique
                        if (conversations.isNotEmpty()) {
                            IconButton(onClick = { showDeleteAllDialog = true }) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Supprimer tout l'historique"
                                )
                            }
                        }
                    }
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                if (conversations.isEmpty()) {
                    // Message affiché quand l'historique est vide
                    Text(
                        text = "Aucune conversation dans l'historique",
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                            .wrapContentSize(Alignment.Center),
                        textAlign = TextAlign.Center,
                        color = Color.Gray
                    )
                } else {
                    // Liste des conversations
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(conversations) { conversation ->
                            ConversationItem(
                                conversation = conversation,
                                onDeleteClick = {
                                    conversationToDelete = conversation
                                }
                            )
                        }
                    }
                }
                
                // Boîte de dialogue pour confirmer la suppression d'une conversation
                if (conversationToDelete != null) {
                    AlertDialog(
                        onDismissRequest = { conversationToDelete = null },
                        title = { Text("Confirmation de suppression") },
                        text = { Text("Voulez-vous supprimer cette conversation ?") },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    conversationToDelete?.let { viewModel.deleteConversation(it) }
                                    conversationToDelete = null
                                }
                            ) {
                                Text("Supprimer")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { conversationToDelete = null }) {
                                Text("Annuler")
                            }
                        }
                    )
                }
                
                // Boîte de dialogue pour confirmer la suppression de tout l'historique
                if (showDeleteAllDialog) {
                    AlertDialog(
                        onDismissRequest = { showDeleteAllDialog = false },
                        title = { Text("Confirmation de suppression") },
                        text = { Text("Voulez-vous supprimer tout l'historique des conversations ?") },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    viewModel.deleteAllConversations()
                                    showDeleteAllDialog = false
                                }
                            ) {
                                Text("Supprimer tout")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showDeleteAllDialog = false }) {
                                Text("Annuler")
                            }
                        }
                    )
                }
            }
        }
    }
}

/**
 * Élément représentant une conversation dans la liste
 */
@Composable
fun ConversationItem(
    conversation: ConversationEntity,
    onDeleteClick: () -> Unit
) {
    // Formatage de la date
    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    val dateString = dateFormat.format(Date(conversation.timestamp))
    
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Date de la conversation
            Text(
                text = dateString,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            // Message de l'utilisateur
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Text(
                    text = "Vous : ",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1565C0)
                )
                
                Text(
                    text = conversation.userMessage,
                    modifier = Modifier.weight(1f)
                )
            }
            
            // Réponse de l'IA
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Text(
                    text = "IA : ",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2E7D32)
                )
                
                Text(
                    text = conversation.aiResponse,
                    modifier = Modifier.weight(1f)
                )
            }
            
            // Bouton de suppression
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Supprimer",
                        tint = Color.Red.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}
