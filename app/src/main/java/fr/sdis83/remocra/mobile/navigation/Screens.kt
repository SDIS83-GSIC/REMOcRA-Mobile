package fr.sdis83.remocra.mobile.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.ImportExport
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sync
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screens(val route: String, val title: String? = null, val icon: ImageVector? = null, var isVisible: Boolean = true) {
    object Settings : Screens("settings", "Ajout de données", Icons.Filled.Settings)
    object PeiList : Screens("settings/pei")
    object PeiCreate : Screens("settings/pei/create")
    object Sync : Screens("sync", "Synchronisation", Icons.Filled.Sync)
    object Tournees : Screens("tournees", "Tournées", Icons.Filled.Checklist)
    object TourneePei : Screens("tournees/{tourneeId}/pei")
    object Pei : Screens("tournees/{tourneeId}/pei/{peiId}")
    object ListGestionnaire : Screens("listing_gest_screen")
    object CreateGestionnaire : Screens("gestionnaire_screen")
    object EditGestionnaire : Screens("gestionnaire_screen/{gestionnaireId}")
    object CreateContact : Screens("contact_screen/{gestionnaireId}")
    object EditContact : Screens("contact_screen/{gestionnaireId}/{contactId}")
    object Export : Screens("export", "Diagnostics", Icons.Filled.ImportExport)
}
