package com.foldersmith.mobile.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.foldersmith.mobile.data.FolderSmithRepository
import com.foldersmith.mobile.ui.screens.DownloadsScreen
import com.foldersmith.mobile.ui.screens.DuplicatesScreen
import com.foldersmith.mobile.ui.screens.HistoryScreen
import com.foldersmith.mobile.ui.screens.HomeScreen
import com.foldersmith.mobile.ui.screens.OrganizedFolderScreen
import com.foldersmith.mobile.ui.screens.PhotoEventsScreen
import com.foldersmith.mobile.ui.screens.ReviewScreen
import com.foldersmith.mobile.ui.screens.ScanScreen
import com.foldersmith.mobile.ui.screens.ScreenshotsScreen
import com.foldersmith.mobile.ui.screens.SettingsScreen

private data class Destination(
    val route: String,
    val label: String
)

private val bottomDestinations = listOf(
    Destination("home", "Home"),
    Destination("scan", "Scan"),
    Destination("review", "Review"),
    Destination("history", "History"),
    Destination("settings", "Settings")
)

@Composable
fun FolderSmithApp(repository: FolderSmithRepository) {
    val viewModel: AppViewModel = viewModel(factory = AppViewModelFactory(repository))
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            NavigationBar {
                bottomDestinations.forEach { destination ->
                    NavigationBarItem(
                        selected = currentRoute == destination.route,
                        onClick = {
                            navController.navigate(destination.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Text(destination.label.take(1)) },
                        label = { Text(destination.label) }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("home") {
                HomeScreen(
                    state = state,
                    onStartScan = { navController.navigate("scan") },
                    onReviewPlan = { navController.navigate("review") },
                    onOpenDuplicates = { navController.navigate("duplicates") },
                    onOpenScreenshots = { navController.navigate("screenshots") },
                    onOpenDownloads = { navController.navigate("downloads") },
                    onOpenPhotoEvents = { navController.navigate("photoEvents") },
                    onOpenOrganizedFolder = { navController.navigate("organized") }
                )
            }
            composable("scan") {
                ScanScreen(state = state, onStartScan = viewModel::startScan, onCancel = viewModel::cancelScan)
            }
            composable("review") {
                ReviewScreen(state = state)
            }
            composable("history") {
                HistoryScreen(state = state, onUndo = viewModel::undoSession)
            }
            composable("settings") {
                SettingsScreen()
            }
            composable("duplicates") {
                DuplicatesScreen(state = state)
            }
            composable("screenshots") {
                ScreenshotsScreen(state = state)
            }
            composable("downloads") {
                DownloadsScreen(state = state)
            }
            composable("photoEvents") {
                PhotoEventsScreen(state = state)
            }
            composable("organized") {
                OrganizedFolderScreen(state = state)
            }
        }
    }
}
