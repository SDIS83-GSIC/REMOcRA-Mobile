package fr.sdis83.remocra.mobile.ui.screens.settings

import android.app.Application
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import fr.sdis83.remocra.mobile.R
import fr.sdis83.remocra.mobile.navigation.Screens
import fr.sdis83.remocra.mobile.ui.components.GestionnaireCard
import fr.sdis83.remocra.mobile.ui.components.HeaderAppBar
import fr.sdis83.remocra.mobile.viewmodels.GestionnairesViewModel

@Composable
fun GestionnaireListingScreen(navController: NavController) {
    val context = LocalContext.current
    val gestionnairesViewModel = GestionnairesViewModel(context.applicationContext as Application, idGestionnaire = null)
    val gestionnairesList = gestionnairesViewModel.gestionnairesList.observeAsState(listOf())

    BackHandler { navController.navigate(Screens.Settings.route) }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Top,
    ) {
        HeaderAppBar(
            title = stringResource(R.string.listingGestionnaireST),
            returnAction = { navController.navigate(Screens.Settings.route) },
        )
        Column(
            Modifier.padding(horizontal = 80.dp),
        ) {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 300.dp),
            ) {
                items(gestionnairesList.value) { gestionnaire ->
                    GestionnaireCard(gestionnaire = gestionnaire, navController = navController)
                }
            }
        }
    }
}
