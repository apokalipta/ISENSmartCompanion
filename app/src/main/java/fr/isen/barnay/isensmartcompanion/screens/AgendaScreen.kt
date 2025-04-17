package fr.isen.barnay.isensmartcompanion.screens

import android.text.format.DateFormat
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import fr.isen.barnay.isensmartcompanion.ui.theme.ISENRed
import fr.isen.barnay.isensmartcompanion.models.Event
import fr.isen.barnay.isensmartcompanion.viewmodels.EventViewModel
import java.text.SimpleDateFormat
import java.util.*

typealias YMD = Triple<Int, Int, Int>

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgendaScreen() {
    // ViewModel and API events
    val viewModel: EventViewModel = viewModel(factory = EventViewModel.Factory())
    val eventsState by viewModel.eventsState.collectAsState()
    val apiEvents: List<Event> = (eventsState as? EventViewModel.EventsState.Success)?.events.orEmpty()

    // Current month
    var currentYM by remember { mutableStateOf(Calendar.getInstance().let { it.get(Calendar.YEAR) to it.get(Calendar.MONTH) }) }
    // Combined events map
    val eventsMap = remember { mutableStateMapOf<YMD, MutableList<String>>() }

    // Populate map when apiEvents change
    LaunchedEffect(apiEvents) {
        eventsMap.clear()
        // Parser for French dates from API (month names in French)
        val parser = SimpleDateFormat("d MMMM yyyy", Locale.FRENCH)
        apiEvents.forEach { e ->
            val parsedDate = runCatching { parser.parse(e.date) }.getOrNull()
            if (parsedDate != null) {
                val cal = Calendar.getInstance().apply { time = parsedDate }
                val y = cal.get(Calendar.YEAR)
                val m = cal.get(Calendar.MONTH)
                val d = cal.get(Calendar.DAY_OF_MONTH)
                eventsMap.getOrPut(Triple(y, m, d)) { mutableListOf() }.add(e.title)
            }
        }
    }

    // Selected date state
    var selectedYMD by remember { mutableStateOf(YMD(currentYM.first, currentYM.second, Calendar.getInstance().get(Calendar.DAY_OF_MONTH))) }
    var showViewDialog by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }
    var newEventTitle by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Header
        Text(
            text = "Agenda",
            color = ISENRed,
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Month navigation
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { changeMonth(currentYM, -1) { currentYM = it } }) {
                Text("<", color = ISENRed, fontSize = 24.sp)
            }
            Text(
                text = DateFormat.format(
                    "MMMM yyyy",
                    Calendar.getInstance().apply { set(Calendar.YEAR, currentYM.first); set(Calendar.MONTH, currentYM.second) }
                ).toString().replaceFirstChar { it.uppercaseChar() },
                color = ISENRed,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = { changeMonth(currentYM, +1) { currentYM = it } }) {
                Text(">", color = ISENRed, fontSize = 24.sp)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Weekdays header
        val weekdays = listOf("Lun","Mar","Mer","Jeu","Ven","Sam","Dim")
        Row(modifier = Modifier.fillMaxWidth()) {
            weekdays.forEach { d ->
                Text(
                    text = d,
                    modifier = Modifier.weight(1f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    fontWeight = FontWeight.Medium,
                    color = ISENRed
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Calendar grid
        val cells = remember(currentYM) { generateCalendarCells(currentYM.first, currentYM.second) }
        Column {
            cells.chunked(7).forEach { week ->
                Row(modifier = Modifier.fillMaxWidth()) {
                    week.forEach { (y, m, d) ->
                        val key = Triple(y, m, d)
                        val hasEvent = eventsMap[key]?.isNotEmpty() == true
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .padding(2.dp)
                                .background(
                                    color = if (hasEvent) ISENRed.copy(alpha = 0.2f) else Color.Transparent,
                                    shape = MaterialTheme.shapes.small
                                )
                                .clickable {
                                    selectedYMD = key
                                    if (hasEvent) showViewDialog = true else showAddDialog = true
                                },
                            contentAlignment = Alignment.TopCenter
                        ) {
                            Text(
                                text = d.toString(),
                                color = if (m == currentYM.second) Color.Black else Color.LightGray,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Quick list
        val (sy, sm, sd) = selectedYMD
        Text(
            text = "Événements du $sd/${sm + 1}/$sy",
            color = ISENRed,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 4.dp)
        )
        val todays = eventsMap[selectedYMD].orEmpty()
        if (todays.isEmpty()) {
            Text("Aucun événement", color = Color.Gray)
        } else {
            LazyColumn {
                items(todays) { ev ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Text(
                            text = ev,
                            modifier = Modifier.padding(12.dp),
                            color = Color.Black
                        )
                    }
                }
            }
        }
    }

    // Dialogs
    if (showViewDialog) {
        AlertDialog(
            onDismissRequest = { showViewDialog = false },
            title = { Text("Événements du ${selectedYMD.third}/${selectedYMD.second + 1}/${selectedYMD.first}") },
            text = { Column { eventsMap[selectedYMD]?.forEach { Text("• $it") } } },
            confirmButton = { TextButton({ showViewDialog = false }) { Text("Fermer", color = ISENRed) } }
        )
    }
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false; newEventTitle = "" },
            title = { Text("Ajouter un événement le ${selectedYMD.third}/${selectedYMD.second + 1}/${selectedYMD.first}", color = ISENRed) },
            text = {
                OutlinedTextField(
                    value = newEventTitle,
                    onValueChange = { newEventTitle = it },
                    placeholder = { Text("Titre de l'événement") }
                )
            },
            confirmButton = {
                TextButton({
                    if (newEventTitle.isNotBlank()) eventsMap.getOrPut(selectedYMD) { mutableListOf() }.add(newEventTitle)
                    showAddDialog = false
                    newEventTitle = ""
                }) { Text("Ajouter", color = ISENRed) }
            },
            dismissButton = { TextButton({ showAddDialog = false; newEventTitle = "" }) { Text("Annuler", color = ISENRed) } }
        )
    }
}

// Helper functions
// Helper functions
private fun changeMonth(currentYM: Pair<Int, Int>, delta: Int, update: (Pair<Int, Int>) -> Unit) {
    val cal = Calendar.getInstance().apply {
        set(Calendar.YEAR, currentYM.first)
        set(Calendar.MONTH, currentYM.second)
        add(Calendar.MONTH, delta)
    }
    update(cal.get(Calendar.YEAR) to cal.get(Calendar.MONTH))
}

private fun generateCalendarCells(year: Int, month: Int): List<YMD> {
    val cal = Calendar.getInstance().apply { set(Calendar.YEAR, year); set(Calendar.MONTH, month); set(Calendar.DAY_OF_MONTH, 1) }
    val dow = cal.get(Calendar.DAY_OF_WEEK)
    val offset = if (dow == Calendar.SUNDAY) 6 else dow - Calendar.MONDAY
    cal.add(Calendar.DAY_OF_MONTH, -offset)
    return List(42) {
        val y = cal.get(Calendar.YEAR)
        val m = cal.get(Calendar.MONTH)
        val d = cal.get(Calendar.DAY_OF_MONTH)
        cal.add(Calendar.DAY_OF_MONTH, 1)
        Triple(y, m, d)
    }
}
