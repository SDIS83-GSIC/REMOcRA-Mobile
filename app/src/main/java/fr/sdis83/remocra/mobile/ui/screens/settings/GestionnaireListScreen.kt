package fr.sdis83.remocra.mobile.ui.screens.settings

import android.app.Application
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavController
import fr.sdis83.remocra.mobile.R
import fr.sdis83.remocra.mobile.navigation.Screens
import fr.sdis83.remocra.mobile.ui.components.HeaderAppBar
import fr.sdis83.remocra.mobile.ui.components.SearchInput
import fr.sdis83.remocra.mobile.utils.pxToDp
import fr.sdis83.remocra.mobile.viewmodels.GestionnaireListViewModel

@Composable
fun GestionnaireListScreen(navController: NavController) {
    val context = LocalContext.current
    val gestionnairesViewModel =
        GestionnaireListViewModel(context.applicationContext as Application)

    GestionnaireList(navController, gestionnairesViewModel)
}

@Composable
fun GestionnaireList(
    navController: NavController,
    gestionnairesViewModel: GestionnaireListViewModel,
) {
    val gestionnairesList =
        gestionnairesViewModel.gestionnairesList.collectAsState(initial = listOf())
    val search by gestionnairesViewModel.search.collectAsState(initial = "")

    BackHandler { navController.navigate(Screens.Settings.route) }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Top,
    ) {
        HeaderAppBar(
            title = stringResource(R.string.listingGestionnaireST),
            returnAction = { navController.navigate(Screens.Settings.route) },
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 80.pxToDp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SearchInput(
                search = search,
                onChange = gestionnairesViewModel::doSearch,
                size = gestionnairesList.value.size,
            )
        }
        LazyColumn(
            Modifier.padding(horizontal = 80.pxToDp),
        ) {
            items(gestionnairesList.value.sortedBy { it.gestionnaireLibelle }) { gestionnaire ->
                Row(
                    Modifier
                        .padding(8.pxToDp)
                        .fillMaxWidth(),
                ) {
                    Box(
                        modifier =
                        Modifier
                            .clip(RoundedCornerShape(8.pxToDp))
                            .background(Color(0xDDE9F3FF))
                            .padding(16.pxToDp)
                            .fillMaxWidth(),
                    ) {
                        Column {
                            Row {
                                Column(
                                    Modifier
                                        .weight(1f),
                                ) {
                                    Text(
                                        text = gestionnaire.gestionnaireLibelle,
                                        fontWeight = FontWeight.Bold,
                                    )
                                    gestionnaire.gestionnaireCode?.let {
                                        Text(text = "SIREN : ${gestionnaire.gestionnaireCode}")
                                    }
                                }
                                Column {
                                    IconButton(
                                        onClick = {
                                            navController.navigate(
                                                Screens.EditGestionnaire.route
                                                    .replace(
                                                        oldValue = "{gestionnaireId}",
                                                        newValue = gestionnaire.gestionnaireId.toString(),
                                                    ),
                                            )
                                        },
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Edit,
                                            contentDescription = "EditGestionnaire",
                                            Modifier.size(30.pxToDp),
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
