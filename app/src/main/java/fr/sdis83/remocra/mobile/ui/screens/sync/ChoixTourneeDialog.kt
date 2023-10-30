package fr.sdis83.remocra.mobile.ui.screens.sync

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavController
import fr.sdis83.remocra.mobile.R
import fr.sdis83.remocra.mobile.database.TourneeDispo
import fr.sdis83.remocra.mobile.viewmodels.ChoixTourneeViewModel
import kotlinx.coroutines.launch

@Composable
fun ChoixTourneeDialog(choixTourneeViewModel: ChoixTourneeViewModel, navController: NavController, onDismiss: () -> Unit) {
    val context = LocalContext.current

    val listeTourneesDispo = choixTourneeViewModel.tourneesDisponibles.observeAsState(listOf())

    LaunchedEffect(key1 = Unit) {
        choixTourneeViewModel.getTourneesDisponibles()
    }

    Dialog(
        onDismissRequest = { onDismiss() },
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
        ),
    ) {
        Card(
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.size(1600.dp, 300.dp),
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .background(Color.White),
            ) {
                Text(
                    text = stringResource(R.string.tournees_a_embarquer),
                    modifier = Modifier.padding(8.dp),
                    fontSize = 20.sp,
                )

                if (!listeTourneesDispo.value.isNullOrEmpty()) {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 225.dp),
                        modifier = Modifier.weight(1f, fill = false),
                    ) {
                        items(listeTourneesDispo.value) {
                            TourneeRow(tourneeDispo = it, choixTourneeViewModel)
                        }
                    }

                    Row(Modifier.padding(top = 10.dp)) {
                        OutlinedButton(
                            onClick = { onDismiss() },
                            Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                                .weight(1F),
                        ) {
                            Text(text = stringResource(R.string.annuler))
                        }

                        Button(
                            onClick = {
                                // Fait un appel pour réserver les tournées selectionnées
                                choixTourneeViewModel.reserveTournees(context, navController = navController)
                                onDismiss()
                            },
                            Modifier
                                .padding(8.dp)
                                .weight(1F),
                        ) {
                            Text(
                                text = "Réserver",
                            )
                        }
                    }
                } else {
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = stringResource(R.string.aucune_tournee_a_reserver),
                        )

                        Button(
                            onClick = {
                                onDismiss()
                            },
                            Modifier
                                .padding(10.dp),
                        ) {
                            Text(
                                text = "Retour",
                            )
                        }
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
            .padding(all = 10.dp)
            .fillMaxWidth(),
    ) {
        Row {
            Checkbox(
                checked = checkedState.value,
                modifier = Modifier.padding(horizontal = 2.dp),
                onCheckedChange = {
                    checkedState.value = it
                    tourneeDispo.choisie = it
                    coroutine.launch {
                        choixTourneeViewModel.updateTourneeDispo(tourneeDispo)
                    }
                },
            )
            Text(tourneeDispo.nom.toString(), fontSize = 15.sp, modifier = Modifier.padding(10.dp))
        }
    }
}
