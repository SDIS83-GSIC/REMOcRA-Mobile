package fr.sdis83.remocra.mobile.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import fr.sdis83.remocra.mobile.MapViewState
import fr.sdis83.remocra.mobile.ui.screens.hydrants.HydrantVisiteScreen
import fr.sdis83.remocra.mobile.ui.screens.settings.SettingScreen
import fr.sdis83.remocra.mobile.ui.screens.sync.SyncScreen
import fr.sdis83.remocra.mobile.ui.screens.tournees.TourneeScreen
import fr.sdis83.remocra.mobile.ui.screens.tournees.TourneesScreen
import fr.sdis83.remocra.mobile.viewmodels.MapViewModel
import java.util.UUID

@Composable
fun NavGraph(
    navController: NavHostController,
    mapViewModel: MapViewModel,
    mapViewState: MutableState<MapViewState>
) {
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
            TourneesScreen(navController)
        }
        composable(
            route = Screens.TourneeHydrants.route,
            arguments = listOf(navArgument("idTournee") {})
        ) {
            if (!it.arguments?.getString("idTournee").isNullOrEmpty()) {
                val idTournee = UUID.fromString(it.arguments?.getString("idTournee"))
                    ?: throw Exception("wrong idTournee")
                LaunchedEffect(Unit) {
                    mapViewState.value = MapViewState(showMapView = true, isFullscreen = false)
                }
                TourneeScreen(navController, idTournee)
            }
        }
        composable(
            route = Screens.Hydrant.route,
            arguments = listOf(navArgument("idTournee") {}, navArgument("idHydrant") {})
        ) {
            if (!it.arguments?.getString("idTournee")
                    .isNullOrEmpty() && !it.arguments?.getString("idHydrant").isNullOrEmpty()
            ) {
                val idHydrant = UUID.fromString(it.arguments?.getString("idHydrant"))
                    ?: throw Exception("wrong idHydrant")
                val idTournee = UUID.fromString(it.arguments?.getString("idTournee"))
                    ?: throw Exception("wrong idTournee")
                LaunchedEffect(Unit) {
                    mapViewState.value = MapViewState(showMapView = true, isFullscreen = false)
                }
                HydrantVisiteScreen(navController, idTournee, idHydrant, mapViewModel)
            }
        }
    }
}
