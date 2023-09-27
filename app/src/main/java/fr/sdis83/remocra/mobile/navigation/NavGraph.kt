package fr.sdis83.remocra.mobile.navigation

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import fr.sdis83.remocra.mobile.MapViewState
import fr.sdis83.remocra.mobile.ui.screens.hydrants.HydrantCreateScreen
import fr.sdis83.remocra.mobile.ui.screens.hydrants.HydrantListScreen
import fr.sdis83.remocra.mobile.ui.screens.hydrants.HydrantVisiteScreen
import fr.sdis83.remocra.mobile.ui.screens.settings.ContactFormScreen
import fr.sdis83.remocra.mobile.ui.screens.settings.GestionnaireFormScreen
import fr.sdis83.remocra.mobile.ui.screens.settings.GestionnaireListScreen
import fr.sdis83.remocra.mobile.ui.screens.settings.SettingScreen
import fr.sdis83.remocra.mobile.ui.screens.sync.SyncScreen
import fr.sdis83.remocra.mobile.ui.screens.tournees.TourneeScreen
import fr.sdis83.remocra.mobile.ui.screens.tournees.TourneesScreen
import fr.sdis83.remocra.mobile.viewmodels.MapViewModel
import fr.sdis83.remocra.mobile.viewmodels.SyncViewModel
import java.util.UUID

@Composable
fun NavGraph(
    navController: NavHostController,
    mapViewModel: MapViewModel,
    mapViewState: MutableState<MapViewState>,
) {
    NavHost(
        navController = navController,
        startDestination = Screens.Tournees.route,
    ) {
        composable(route = Screens.Settings.route) {
            LaunchedEffect(Unit) {
                mapViewState.value = MapViewState(showMapView = false, isFullscreen = false)
            }
            SettingScreen(navController)
        }
        composable(route = Screens.HydrantCreate.route) {
            LaunchedEffect(Unit) {
                mapViewState.value = MapViewState(showMapView = true, isFullscreen = false)
            }
            HydrantCreateScreen(navController, mapViewModel)
        }
        composable(route = Screens.HydrantList.route) {
            LaunchedEffect(Unit) {
                mapViewState.value = MapViewState(showMapView = true, isFullscreen = false)
            }
            HydrantListScreen(navController, mapViewModel)
        }
        composable(route = Screens.Sync.route) {
            LaunchedEffect(Unit) {
                mapViewState.value = MapViewState(showMapView = false, isFullscreen = false)
            }
            SyncScreen(SyncViewModel(Application()))
        }

        composable(route = Screens.Tournees.route) {
            LaunchedEffect(Unit) {
                mapViewState.value = MapViewState(showMapView = true, isFullscreen = false)
            }
            TourneesScreen(navController, mapViewModel)
        }
        composable(
            route = Screens.TourneeHydrants.route,
            arguments = listOf(navArgument("idTournee") {}),
        ) {
            if (!it.arguments?.getString("idTournee").isNullOrEmpty()) {
                val idTournee = UUID.fromString(it.arguments?.getString("idTournee"))
                    ?: throw Exception("wrong idTournee")
                LaunchedEffect(Unit) {
                    mapViewState.value = MapViewState(showMapView = true, isFullscreen = false)
                }
                TourneeScreen(navController, idTournee, mapViewModel)
            }
        }

        composable(
            route = Screens.Hydrant.route,
            arguments = listOf(navArgument("idTournee") {}, navArgument("idHydrant") {}),
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

        composable(route = Screens.ListGestionnaire.route) {
            LaunchedEffect(Unit) {
                mapViewState.value = MapViewState(showMapView = false, isFullscreen = false)
            }
            GestionnaireListScreen(navController)
        }

        composable(route = Screens.CreateGestionnaire.route) {
            LaunchedEffect(Unit) {
                mapViewState.value = MapViewState(showMapView = false, isFullscreen = false)
            }
            GestionnaireFormScreen(navController, null)
        }

        composable(
            route = Screens.EditGestionnaire.route,
            arguments = listOf(navArgument("idGestionnaire") { nullable = true }),
        ) {
            if (!it.arguments?.getString("idGestionnaire").isNullOrEmpty()) {
                val idGestionnaire = UUID.fromString(it.arguments?.getString("idGestionnaire"))
                    ?: throw Exception("wrong idGestionnaire")
                LaunchedEffect(Unit) {
                    mapViewState.value = MapViewState(showMapView = false, isFullscreen = false)
                }
                GestionnaireFormScreen(navController, idGestionnaire)
            }
        }

        composable(
            route = Screens.CreateContact.route,
            arguments = listOf(
                navArgument("idGestionnaire") { nullable = true },
            ),
        ) {
            if (!it.arguments?.getString("idGestionnaire").isNullOrEmpty()) {
                val idGestionnaire = UUID.fromString(it.arguments?.getString("idGestionnaire"))
                    ?: throw Exception("wrong idGestionnaire")
                LaunchedEffect(Unit) {
                    mapViewState.value = MapViewState(showMapView = false, isFullscreen = false)
                }
                ContactFormScreen(navController, null, idGestionnaire)
            }
        }

        composable(
            route = Screens.EditContact.route,
            arguments = listOf(
                navArgument("idGestionnaire") { nullable = false },
                navArgument("idContact") { nullable = true },
            ),
        ) {
            if (!it.arguments?.getString("idContact").isNullOrEmpty()) {
                val idContact = UUID.fromString(it.arguments?.getString("idContact"))
                    ?: throw Exception("wrong idContact")
                if (!it.arguments?.getString("idGestionnaire").isNullOrEmpty()) {
                    val idGestionnaire =
                        UUID.fromString(it.arguments?.getString("idGestionnaire"))
                            ?: throw Exception("wrong idGestionnaire")
                    LaunchedEffect(Unit) {
                        mapViewState.value =
                            MapViewState(showMapView = false, isFullscreen = false)
                    }
                    ContactFormScreen(navController, idContact, idGestionnaire)
                }
            }
        }
    }
}
