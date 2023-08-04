package fr.sdis83.remocra.mobile.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sync
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screens(val route: String, val title: String? = null, val icon: ImageVector? = null) {
    object Settings : Screens("settings", "Paramètres", Icons.Filled.Settings)
    object Sync : Screens("sync", "Synchronisation",  Icons.Filled.Sync)
    object Tournees : Screens("tournees", "Tournées", Icons.Filled.Checklist)
    object TourneeHydrants : Screens("tournees/{idTournee}/hydrants")
    object Hydrant : Screens("tournees/{idTournee}/hydrants/{idHydrant}")
}
