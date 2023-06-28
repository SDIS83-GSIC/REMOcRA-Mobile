package fr.sdis83.remocra.mobile.navigation

sealed class Screens(val route: String) {
    object Settings : Screens("settings_screen")
    object Sync : Screens("sync_screen")
    object Tournees : Screens("tournees_screen")
    object Hydrants : Screens("hydrants_screen")
}
