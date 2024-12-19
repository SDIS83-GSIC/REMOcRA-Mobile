package fr.sdis83.remocra.mobile.ui.screens.sync

import android.app.Application
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.em
import androidx.navigation.NavController
import fr.sdis83.remocra.mobile.R
import fr.sdis83.remocra.mobile.navigation.Screens
import fr.sdis83.remocra.mobile.ui.components.HeaderAppBar
import fr.sdis83.remocra.mobile.utils.pxToDp
import fr.sdis83.remocra.mobile.viewmodels.ChoixTourneeViewModel
import fr.sdis83.remocra.mobile.viewmodels.SyncViewModel

@Composable
fun SyncScreen(syncViewModel: SyncViewModel, navController: NavController) {
    val context = LocalContext.current

    val choixTourneeViewModel = ChoixTourneeViewModel(context.applicationContext as Application)

    val visiteCount by syncViewModel.visiteCount.observeAsState()
    val lPeiTourneeCount by syncViewModel.lPeiTourneeCount.observeAsState()
    val tourneeNotDoneCount by syncViewModel.tourneeNotDoneCount.observeAsState()
    val tourneeCount by syncViewModel.tourneeCount.observeAsState()
    val PeiCreesCount by syncViewModel.PeiCreesCount.observeAsState()

    var showCustomDialog by remember {
        mutableStateOf(false)
    }

    val isBusy by syncViewModel.isBusy.observeAsState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        HeaderAppBar(
            title = stringResource(R.string.synchronisation),
            returnAction = {
                navController.popBackStack(
                    Screens.Tournees.route,
                    inclusive = false,
                )
            },
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(30.pxToDp),
            horizontalArrangement = Arrangement.Start,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.Start,
            ) {
                Text(
                    text = stringResource(R.string.visites_finies, visiteCount ?: 0, lPeiTourneeCount ?: 0),
                    fontWeight = FontWeight.Normal,
                    fontSize = 3.em,
                )
                Text(
                    text = stringResource(R.string.tournees_finies, tourneeNotDoneCount ?: 0, tourneeCount ?: 0),
                    fontWeight = FontWeight.Normal,
                    fontSize = 3.em,
                )
                Text(
                    text = stringResource(R.string.pei_crees, 0, PeiCreesCount ?: 0),
                    fontWeight = FontWeight.Normal,
                    fontSize = 3.em,
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth(1f)
                        .padding(10.pxToDp),
                    horizontalArrangement = Arrangement.SpaceAround,
                ) {
                    Column(
                        modifier = Modifier
                            .padding(20.pxToDp)
                            .fillMaxWidth(0.5f)
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.Top,
                        horizontalAlignment = Alignment.Start,
                    ) {
                        // Bouton pour récupération des tournées
                        Button(
                            modifier = Modifier
                                .padding(0.pxToDp, 20.pxToDp)
                                .fillMaxWidth(0.8f),
                            onClick = {
                                showCustomDialog = !showCustomDialog
                            },
                            shape = RoundedCornerShape(50.pxToDp),
                            contentPadding = PaddingValues(10.pxToDp),
                            enabled = !isBusy!!,
                        ) {
                            Text(
                                modifier = Modifier.padding(10.pxToDp),
                                text = stringResource(R.string.choix_tournees),
                                fontSize = 3.em,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                    Column(
                        modifier = Modifier
                            .padding(20.pxToDp)
                            .fillMaxWidth(1f)
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.Top,
                        horizontalAlignment = Alignment.Start,
                    ) {
                        // Bouton pour synchroniser les tournées
                        Button(
                            modifier = Modifier
                                .padding(0.pxToDp, 20.pxToDp)
                                .fillMaxWidth(.8f),
                            onClick = {
                                syncViewModel.synchro(context.applicationContext as Application)
                            },
                            shape = RoundedCornerShape(50.pxToDp),
                            contentPadding = PaddingValues(10.pxToDp),
                            enabled = !isBusy!!,
                        ) {
                            Text(
                                modifier = Modifier.padding(10.pxToDp),
                                text = stringResource(R.string.synchro_tournees),
                                fontSize = 3.em,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                }
            }
        }
    }

    if (showCustomDialog) {
        ChoixTourneeDialog(choixTourneeViewModel, navController = navController) {
            showCustomDialog = !showCustomDialog
        }
    }
}
