package fr.sdis83.remocra.mobile.navigation

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import fr.sdis83.remocra.mobile.MapViewState
import fr.sdis83.remocra.mobile.ui.screens.export.ExportScreen
import fr.sdis83.remocra.mobile.ui.screens.pei.PeiCreateScreen
import fr.sdis83.remocra.mobile.ui.screens.pei.PeiListScreen
import fr.sdis83.remocra.mobile.ui.screens.pei.VisiteScreen
import fr.sdis83.remocra.mobile.ui.screens.settings.ContactFormScreen
import fr.sdis83.remocra.mobile.ui.screens.settings.GestionnaireFormScreen
import fr.sdis83.remocra.mobile.ui.screens.settings.GestionnaireListScreen
import fr.sdis83.remocra.mobile.ui.screens.settings.SettingScreen
import fr.sdis83.remocra.mobile.ui.screens.sync.SyncGestionnaireScreen
import fr.sdis83.remocra.mobile.ui.screens.sync.SyncNewPeiScreen
import fr.sdis83.remocra.mobile.ui.screens.sync.SyncScreen
import fr.sdis83.remocra.mobile.ui.screens.sync.SyncTourneeScreen
import fr.sdis83.remocra.mobile.ui.screens.tournees.TourneeScreen
import fr.sdis83.remocra.mobile.ui.screens.tournees.TourneesScreen
import fr.sdis83.remocra.mobile.utils.GlobalConstants
import fr.sdis83.remocra.mobile.viewmodels.DroitViewModel
import fr.sdis83.remocra.mobile.viewmodels.MapViewModel
import fr.sdis83.remocra.mobile.viewmodels.StatsViewModel
import java.util.UUID

@Composable
fun NavGraph(
    navController: NavHostController,
    mapViewModel: MapViewModel,
    mapViewState: MutableState<MapViewState>,
) {
    val droitViewModel = DroitViewModel(LocalContext.current.applicationContext as Application)
    val listTypeDroit by droitViewModel.typesDroit.observeAsState()

    Screens.Settings.isVisible = listTypeDroit?.firstOrNull { it.code == GlobalConstants.CREATION_PEI_MOBILE_DROIT } != null ||
        listTypeDroit?.firstOrNull { it.code == GlobalConstants.CREATION_GESTIONNAIRE_MOBILE_DROIT } != null
    NavHost(
        navController = navController,
        startDestination = Screens.Tournees.route,
    ) {
        composable(route = Screens.Settings.route) {
            LaunchedEffect(Unit) {
                mapViewState.value = MapViewState(showMapView = false, isFullscreen = false)
            }
            SettingScreen(navController, droitViewModel)
        }
        composable(route = Screens.Export.route) {
            LaunchedEffect(Unit) {
                mapViewState.value = MapViewState(showMapView = false, isFullscreen = false)
            }
            ExportScreen(navController)
        }
        composable(route = Screens.PeiCreate.route) {
            LaunchedEffect(Unit) {
                mapViewState.value = MapViewState(showMapView = true, isFullscreen = false)
            }
            PeiCreateScreen(navController, mapViewModel)
        }
        composable(route = Screens.PeiList.route) {
            LaunchedEffect(Unit) {
                mapViewState.value = MapViewState(showMapView = true, isFullscreen = false)
            }
            PeiListScreen(navController, mapViewModel)
        }
        composable(route = Screens.Sync.route) {
            LaunchedEffect(Unit) {
                mapViewState.value = MapViewState(showMapView = false, isFullscreen = false)
            }
            SyncScreen(StatsViewModel(Application()), navController)
        }
        composable(route = Screens.SyncTournee.route) {
            LaunchedEffect(Unit) {
                mapViewState.value = MapViewState(showMapView = false, isFullscreen = false)
            }
            SyncTourneeScreen(navController)
        }
        composable(route = Screens.SyncNewPei.route) {
            LaunchedEffect(Unit) {
                mapViewState.value = MapViewState(showMapView = false, isFullscreen = false)
            }
            SyncNewPeiScreen(navController)
        }
        composable(route = Screens.SyncGestionnaire.route) {
            LaunchedEffect(Unit) {
                mapViewState.value = MapViewState(showMapView = false, isFullscreen = false)
            }
            SyncGestionnaireScreen(navController)
        }

        composable(route = Screens.Tournees.route) {
            LaunchedEffect(Unit) {
                mapViewState.value = MapViewState(showMapView = true, isFullscreen = false)
            }
            TourneesScreen(navController, mapViewModel)
        }
        composable(
            route = Screens.TourneePei.route,
            arguments = listOf(navArgument("tourneeId") {}),
        ) {
            if (!it.arguments?.getString("tourneeId").isNullOrEmpty()) {
                val tourneeId = UUID.fromString(it.arguments?.getString("tourneeId"))
                    ?: throw Exception("wrong tourneeId")
                LaunchedEffect(Unit) {
                    mapViewState.value = MapViewState(showMapView = true, isFullscreen = false)
                }
                TourneeScreen(navController, tourneeId, mapViewModel)
            }
        }

        composable(
            route = Screens.Pei.route,
            arguments = listOf(navArgument("tourneeId") {}, navArgument("peiId") {}),
        ) {
            if (!it.arguments?.getString("tourneeId")
                    .isNullOrEmpty() && !it.arguments?.getString("peiId").isNullOrEmpty()
            ) {
                val peiId = UUID.fromString(it.arguments?.getString("peiId"))
                    ?: throw Exception("wrong peiId")
                val tourneeId = UUID.fromString(it.arguments?.getString("tourneeId"))
                    ?: throw Exception("wrong tourneeId")
                LaunchedEffect(Unit) {
                    mapViewState.value = MapViewState(showMapView = true, isFullscreen = false)
                }
                VisiteScreen(navController, tourneeId, peiId, mapViewModel)
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
            arguments = listOf(navArgument("gestionnaireId") { nullable = true }),
        ) {
            if (!it.arguments?.getString("gestionnaireId").isNullOrEmpty()) {
                val gestionnaireId = UUID.fromString(it.arguments?.getString("gestionnaireId"))
                    ?: throw Exception("wrong gestionnaireId")
                LaunchedEffect(Unit) {
                    mapViewState.value = MapViewState(showMapView = false, isFullscreen = false)
                }
                GestionnaireFormScreen(navController, gestionnaireId)
            }
        }

        composable(
            route = Screens.CreateContact.route,
            arguments = listOf(
                navArgument("gestionnaireId") { nullable = true },
            ),
        ) {
            if (!it.arguments?.getString("gestionnaireId").isNullOrEmpty()) {
                val gestionnaireId = UUID.fromString(it.arguments?.getString("gestionnaireId"))
                    ?: throw Exception("wrong gestionnaireId")
                LaunchedEffect(Unit) {
                    mapViewState.value = MapViewState(showMapView = false, isFullscreen = false)
                }
                ContactFormScreen(navController, null, gestionnaireId)
            }
        }

        composable(
            route = Screens.EditContact.route,
            arguments = listOf(
                navArgument("gestionnaireId") { nullable = false },
                navArgument("contactId") { nullable = true },
            ),
        ) {
            if (!it.arguments?.getString("contactId").isNullOrEmpty()) {
                val contactId = UUID.fromString(it.arguments?.getString("contactId"))
                    ?: throw Exception("wrong contactId")
                if (!it.arguments?.getString("gestionnaireId").isNullOrEmpty()) {
                    val gestionnaireId =
                        UUID.fromString(it.arguments?.getString("gestionnaireId"))
                            ?: throw Exception("wrong gestionnaireId")
                    LaunchedEffect(Unit) {
                        mapViewState.value =
                            MapViewState(showMapView = false, isFullscreen = false)
                    }
                    ContactFormScreen(navController, contactId, gestionnaireId)
                }
            }
        }
    }
}
