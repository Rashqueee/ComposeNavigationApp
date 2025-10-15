package com.example.composenavigationapp.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.composenavigationapp.ui.screens.AddScreen
import com.example.composenavigationapp.ui.screens.DetailScreen
import com.example.composenavigationapp.ui.screens.HomeScreen
import com.example.composenavigationapp.ui.screens.ProfileScreen
import com.example.composenavigationapp.ui.screens.SettingsScreen
import com.example.composenavigationapp.ui.viewmodel.ItemViewModel
import kotlinx.coroutines.launch

// --- BottomNav items ---
sealed class BottomItem(
    val route: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    data object Home : BottomItem(Routes.HOME, "Home", Icons.Filled.Home)
    data object Profile : BottomItem(Routes.PROFILE, "Profile", Icons.Filled.Person)
    data object Settings : BottomItem(Routes.SETTINGS, "Settings", Icons.Filled.Settings)
}

private val bottomItems = listOf(
    BottomItem.Home,
    BottomItem.Profile,
    BottomItem.Settings
)

@Composable
private fun BottomNavBar(navController: NavHostController) {
    val backStack by navController.currentBackStackEntryAsState()
    val dest = backStack?.destination

    NavigationBar {
        bottomItems.forEach { item ->
            val selected = isTopLevelDestination(dest, item.route)

            NavigationBarItem(
                selected = selected,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) }
            )
        }
    }
}

private fun isTopLevelDestination(dest: NavDestination?, route: String): Boolean =
    dest?.hierarchy?.any { it.route == route } == true

private fun currentRoute(navController: NavHostController): String? =
    navController.currentBackStackEntry?.destination?.route

@Composable
private fun AppDrawer(onNavigate: (String) -> Unit) {
    ModalDrawerSheet {
        Text(
            text = "Menu",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(16.dp)
        )

        NavigationDrawerItem(
            label = { Text("Home") },
            selected = false,
            onClick = { onNavigate(Routes.HOME) }
        )

        NavigationDrawerItem(
            label = { Text("Profile") },
            selected = false,
            onClick = { onNavigate(Routes.PROFILE) }
        )

        NavigationDrawerItem(
            label = { Text("Settings") },
            selected = false,
            onClick = { onNavigate(Routes.SETTINGS) }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScaffold(
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit
) {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AppDrawer(
                onNavigate = { route ->
                    navController.navigate(route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                    scope.launch { drawerState.close() }
                }
            )
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        val currentRoute = currentRoute(navController)
                        val title = when (currentRoute) {
                            Routes.HOME -> "Home"
                            Routes.PROFILE -> "Profile"
                            Routes.SETTINGS -> "Settings"
                            Routes.ADD -> "Add Item"
                            Routes.DETAIL -> "Detail"
                            else -> "Compose App"
                        }
                        Text(title)
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Filled.Home, contentDescription = "Menu")
                        }
                    }
                )

            },
            bottomBar = { BottomNavBar(navController) },
            floatingActionButton = {
                FloatingActionButton(onClick = {
                    navController.navigate(Routes.ADD) { launchSingleTop = true }
                }) {
                    Icon(Icons.Filled.Add, contentDescription = "Add")
                }
            }
        ) { padding ->
            Box(Modifier.padding(padding)) {
                MainNavHost(navController, isDarkTheme, onToggleTheme)
            }
        }
    }
}

@Composable
private fun MainNavHost(
    navController: NavHostController,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit
) {
    val viewModel: ItemViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = Routes.HOME
    ) {
        composable(Routes.HOME) { HomeScreen(navController, viewModel) }
        composable("${Routes.DETAIL}/{id}") { backStack ->
            val id = backStack.arguments?.getString("id")
            DetailScreen(navController, id)
        }
        composable(Routes.PROFILE) { ProfileScreen() }
        composable(Routes.SETTINGS) {
            SettingsScreen(
                isDarkTheme = isDarkTheme,
                onToggleTheme = onToggleTheme
            )
        }
        composable(Routes.ADD) { AddScreen(navController, viewModel) }
    }
}
