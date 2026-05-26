package fr.sdis83.remocra.mobile.ui.screens.sync

import android.app.Application
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.em
import androidx.navigation.NavController
import fr.sdis83.remocra.mobile.R
import fr.sdis83.remocra.mobile.database.TourneeDispo
import fr.sdis83.remocra.mobile.navigation.Screens
import fr.sdis83.remocra.mobile.ui.components.HeaderAppBar
import fr.sdis83.remocra.mobile.ui.components.SearchInput
import fr.sdis83.remocra.mobile.ui.components.SyncStatBadge
import fr.sdis83.remocra.mobile.utils.pxToDp
import fr.sdis83.remocra.mobile.viewmodels.ChoixTourneeViewModel
import kotlinx.coroutines.launch

@Composable
fun ChoixTourneeScreen(navController: NavController) {
    val context = LocalContext.current

    val choixTourneeViewModel = ChoixTourneeViewModel(context.applicationContext as Application)
    val listeTourneesDispo by choixTourneeViewModel.tourneesDisponibles.collectAsState(initial = listOf())
    val search by choixTourneeViewModel.search.collectAsState(initial = "")

    LaunchedEffect(Unit) {
        choixTourneeViewModel.getTourneesDisponibles()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 40.pxToDp, vertical = 30.pxToDp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        HeaderAppBar(
            title = stringResource(R.string.choisir_tournee),
            returnAction = {
                navController.popBackStack(
                    Screens.Tournees.route,
                    inclusive = false,
                )
            },
        )

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(),
            shape = RoundedCornerShape(12.pxToDp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 22.pxToDp, vertical = 20.pxToDp),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.Start,
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.pxToDp, vertical = 8.pxToDp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        SearchInput(
                            search = search,
                            onChange = choixTourneeViewModel::doSearch,
                            size = null,
                        )
                    }

                    SyncStatBadge(
                        label = "Résultats",
                        value = "${listeTourneesDispo.size}",
                        containerColor = Color(63, 191, 63).copy(alpha = 0.45f),
                        contentColor = Color.Black,
                    )
                }

                if (listeTourneesDispo.isNotEmpty()) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(1f, fill = false)
                            .padding(top = 12.pxToDp),
                        horizontalArrangement = Arrangement.spacedBy(12.pxToDp),
                        verticalArrangement = Arrangement.spacedBy(12.pxToDp),
                        contentPadding = PaddingValues(horizontal = 8.pxToDp, vertical = 8.pxToDp),
                    ) {
                        items(listeTourneesDispo) {
                            TourneeRow(tourneeDispo = it, choixTourneeViewModel)
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 18.pxToDp, bottom = 12.pxToDp),
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(0.2f),
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            Button(
                                onClick = {
                                    choixTourneeViewModel.reserveTournees(context, navController = navController)
                                },
                                modifier = Modifier.weight(1F),
                                shape = RoundedCornerShape(50.pxToDp),
                                contentPadding = PaddingValues(20.pxToDp),
                            ) {
                                Text(
                                    text = "Réserver",
                                    textAlign = TextAlign.Center,
                                )
                            }
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = stringResource(R.string.aucune_tournee_a_reserver),
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TourneeRow(tourneeDispo: TourneeDispo, choixTourneeViewModel: ChoixTourneeViewModel) {
    val checkedState = remember { mutableStateOf(tourneeDispo.choisie) }
    val coroutine = rememberCoroutineScope()
    Card(
        modifier = Modifier
            .padding(all = 10.pxToDp)
            .fillMaxWidth(),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Checkbox(
                checked = checkedState.value,
                modifier = Modifier.padding(horizontal = 2.pxToDp),
                onCheckedChange = {
                    checkedState.value = it
                    tourneeDispo.choisie = it
                    coroutine.launch {
                        choixTourneeViewModel.updateTourneeDispo(tourneeDispo)
                    }
                },
            )
            Text(tourneeDispo.nom.toString(), fontSize = 2.5.em, modifier = Modifier.padding(10.pxToDp))
        }
    }
}
