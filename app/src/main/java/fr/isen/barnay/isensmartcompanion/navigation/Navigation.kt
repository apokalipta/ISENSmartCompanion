package fr.isen.barnay.isensmartcompanion.navigation

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.History
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import fr.isen.barnay.isensmartcompanion.screens.AgendaScreen
import fr.isen.barnay.isensmartcompanion.screens.EventDetailScreen
import fr.isen.barnay.isensmartcompanion.screens.EventsScreen
import fr.isen.barnay.isensmartcompanion.screens.HistoryScreen
import fr.isen.barnay.isensmartcompanion.screens.HomeScreen
import fr.isen.barnay.isensmartcompanion.ui.theme.ISENRed

// Définition des destinations de navigation
sealed class Screen(val route: String, val icon: ImageVector, val label: String) {
    object Home : Screen("home", Icons.Filled.Home, "Accueil")
    object Events : Screen("events", Icons.Outlined.Event, "Événements")
    object Agenda : Screen("agenda", Icons.Outlined.DateRange, "Agenda")
    object History : Screen("history", Icons.Outlined.History, "Historique")
    
    // Routes pour les écrans de détail (sans icône dans la barre de navigation)
    object EventDetail : Screen("event_detail/{eventId}", Icons.Outlined.Event, "Détail Événement") {
        fun createRoute(eventId: String) = "event_detail/$eventId"
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    Scaffold(
        bottomBar = { BottomNavBar(navController = navController) }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen()
            }
            composable(Screen.Events.route) {
                EventsScreen(navController)
            }
            composable(Screen.Agenda.route) {
                AgendaScreen()
            }
            composable(Screen.History.route) {
                HistoryScreen()
            }
            composable(
                route = Screen.EventDetail.route,
                arguments = listOf(navArgument("eventId") { type = NavType.StringType })
            ) { backStackEntry ->
                val eventId = backStackEntry.arguments?.getString("eventId")
                EventDetailScreen(navController, eventId)
            }
        }
    }
}

@Composable
fun BottomNavBar(navController: NavHostController) {
    val screens = listOf(
        Screen.Home,
        Screen.Events,
        Screen.Agenda,
        Screen.History
    )
    
    NavigationBar(
        containerColor = Color.White,
        contentColor = Color.Gray
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination
        
        screens.forEach { screen ->
            AddItem(
                screen = screen,
                currentDestination = currentDestination,
                navController = navController
            )
        }
    }
}

@Composable
fun RowScope.AddItem(
    screen: Screen,
    currentDestination: NavDestination?,
    navController: NavHostController
) {
    val selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true

    NavigationBarItem(
        icon = {
            Icon(
                imageVector = screen.icon,
                contentDescription = screen.label
            )
        },
        label = {
            Text(text = screen.label)
        },
        selected = selected,
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = ISENRed,
            selectedTextColor = ISENRed,
            indicatorColor = Color.White
        ),
        onClick = {
            navController.navigate(screen.route) {
                popUpTo(navController.graph.findStartDestination().id)
                launchSingleTop = true
            }
        }
    )
}
