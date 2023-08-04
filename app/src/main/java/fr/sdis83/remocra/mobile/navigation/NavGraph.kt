package fr.sdis83.remocra.mobile.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import fr.sdis83.remocra.mobile.MapViewState
import fr.sdis83.remocra.mobile.ui.screens.settings.SettingScreen
import fr.sdis83.remocra.mobile.ui.screens.sync.SyncScreen
import fr.sdis83.remocra.mobile.ui.screens.tournees.TourneesScreen

@Composable
fun NavGraph(navController: NavHostController, mapViewState: MutableState<MapViewState>) {
    NavHost(
        navController = navController,
        startDestination = Screens.Tournees.route
    ) {
        composable(route = Screens.Settings.route) {
            LaunchedEffect(Unit) {
                mapViewState.value = MapViewState(showMapView = false, isFullscreen = false)
            }
            SettingScreen()
        }

        composable(route = Screens.Sync.route) {
            LaunchedEffect(Unit) {
                mapViewState.value = MapViewState(showMapView = false, isFullscreen = false)
            }
            SyncScreen()
        }
        composable(route = Screens.Tournees.route) {
            LaunchedEffect(Unit) {
                mapViewState.value = MapViewState(showMapView = true, isFullscreen = false)
            }
            TourneesScreen()
        }
    }
}
