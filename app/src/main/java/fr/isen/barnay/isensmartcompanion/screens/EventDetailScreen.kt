package fr.isen.barnay.isensmartcompanion.screens

import android.content.Context
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.core.app.NotificationCompat
import android.os.Handler
import android.os.Looper
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import fr.isen.barnay.isensmartcompanion.NotificationReceiver
import fr.isen.barnay.isensmartcompanion.ui.theme.ISENRed
import fr.isen.barnay.isensmartcompanion.viewmodels.EventViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailScreen(
    navController: NavController,
    eventId: String? = null,
) {
    val context = LocalContext.current
    val viewModel: EventViewModel = viewModel(factory = EventViewModel.Factory())
    val eventsState by viewModel.eventsState.collectAsState()
    val event = if (eventId != null && eventsState is EventViewModel.EventsState.Success) {
        viewModel.getEventById(eventId)
    } else null

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(event?.title ?: "Détail de l'événement") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Retour",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    if (event != null) {
                        val isReminded = remember { mutableStateOf(getReminderPreference(context, event.id)) }
                        IconButton(onClick = {
                            val newState = !isReminded.value
                            isReminded.value = newState
                            saveReminderPreference(context, event.id, newState)
                            if (newState) {
                                scheduleNotification(
                                    context = context,
                                    title = event.title,
                                    message = "Attention, l'événement ${event.title} va commencer !",
                                    delayInSeconds = 10
                                )
                            }
                        }) {
                            Icon(
                                imageVector = if (isReminded.value) Icons.Filled.Notifications else Icons.Filled.NotificationsNone,
                                contentDescription = "Rappel",
                                tint = Color.White
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ISENRed,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { innerPadding ->
        when (eventsState) {
            is EventViewModel.EventsState.Loading -> {
                Box(
                    modifier = Modifier
                        .padding(innerPadding)
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = ISENRed,
                        modifier = Modifier.size(50.dp)
                    )
                }
            }
            else -> {
                if (event != null) {
                    Column(
                        modifier = Modifier
                            .padding(innerPadding)
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Titre et catégorie
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = event.title,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = ISENRed,
                                modifier = Modifier.weight(1f)
                            )
                            Surface(
                                color = ISENRed.copy(alpha = 0.1f),
                                shape = MaterialTheme.shapes.small
                            ) {
                                Text(
                                    text = event.category,
                                    fontSize = 14.sp,
                                    color = ISENRed,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        // Carte détails : date, lieu, catégorie
                        Card(
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                // Date
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CalendarToday,
                                        contentDescription = "Date",
                                        tint = ISENRed,
                                        modifier = Modifier.padding(end = 16.dp)
                                    )
                                    Column {
                                        Text(
                                            text = "Date",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                        )
                                        Text(
                                            text = event.date,
                                            fontSize = 16.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                                // Lieu
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = "Lieu",
                                        tint = ISENRed,
                                        modifier = Modifier.padding(end = 16.dp)
                                    )
                                    Column {
                                        Text(
                                            text = "Lieu",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                        )
                                        Text(
                                            text = event.location,
                                            fontSize = 16.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                                // Catégorie
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Category,
                                        contentDescription = "Catégorie",
                                        tint = ISENRed,
                                        modifier = Modifier.padding(end = 16.dp)
                                    )
                                    Column {
                                        Text(
                                            text = "Catégorie",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                        )
                                        Text(
                                            text = event.category,
                                            fontSize = 16.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }

                        // Description
                        Card(
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Description",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ISENRed,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                                Text(
                                    text = event.description,
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    lineHeight = 24.sp
                                )
                            }
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .padding(innerPadding)
                            .fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Événement non trouvé",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 18.sp
                        )
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------
// Helpers pour les rappels en SharedPreferences + Handler
// ----------------------------------------------------

fun saveReminderPreference(context: Context, eventId: String, isEnabled: Boolean) {
    val prefs = context.getSharedPreferences("reminders", Context.MODE_PRIVATE)
    prefs.edit().putBoolean(eventId, isEnabled).apply()
}

fun getReminderPreference(context: Context, eventId: String): Boolean {
    val prefs = context.getSharedPreferences("reminders", Context.MODE_PRIVATE)
    return prefs.getBoolean(eventId, false)
}

fun scheduleNotification(
    context: Context,
    title: String,
    message: String,
    delayInSeconds: Int
) {
    Handler(Looper.getMainLooper()).postDelayed({
        // Récupère l'Application Context
        val appContext = context.applicationContext
        val nm = appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Crée le canal (Android 8+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "default",
                "Rappels d'événements",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            nm.createNotificationChannel(channel)
        }

        // Construit et envoie la notification
        val notif = NotificationCompat.Builder(appContext, "default")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        nm.notify(title.hashCode(), notif)
    }, delayInSeconds * 1_000L)
}
