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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.sdis83.remocra.mobile.R
import fr.sdis83.remocra.mobile.viewmodels.ChoixTourneeViewModel
import fr.sdis83.remocra.mobile.viewmodels.SyncViewModel

@Composable
fun SyncScreen(syncViewModel: SyncViewModel) {
    val context = LocalContext.current

    val choixTourneeViewModel = ChoixTourneeViewModel(context.applicationContext as Application)

    val hydrantVisiteCount by syncViewModel.hydrantVisiteCount.observeAsState()
    val hydrantTourneeCount by syncViewModel.hydrantTourneeCount.observeAsState()
    val tourneeNotDoneCount by syncViewModel.tourneeNotDoneCount.observeAsState()
    val tourneeCount by syncViewModel.tourneeCount.observeAsState()

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
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(30.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start,
        ) {
            Text(
                text = stringResource(R.string.synchronisation),
                fontWeight = FontWeight.Bold,
                fontSize = 30.sp,
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(30.dp),
            horizontalArrangement = Arrangement.Start,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.Start,
            ) {
//                Text(
//                    modifier = Modifier.padding(0.dp, 0.dp, 0.dp, 20.dp),
//                    text = stringResource(R.string.derniere_synchro),
//                    fontWeight = FontWeight.Bold,
//                    fontSize = 20.sp,
//                )
                Text(
                    text = stringResource(R.string.visites_finies, hydrantVisiteCount ?: 0, hydrantTourneeCount ?: 0),
                    fontWeight = FontWeight.Normal,
                    fontSize = 18.sp,
                )
                Text(
                    text = stringResource(R.string.tournees_finies, tourneeNotDoneCount ?: 0, tourneeCount ?: 0),
                    fontWeight = FontWeight.Normal,
                    fontSize = 18.sp,
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                ) {
                    // Bouton pour récupération des tournées
                    Button(
                        modifier = Modifier
                            .padding(0.dp, 20.dp, 10.dp, 20.dp)
                            .fillMaxWidth(0.5f),
                        onClick = {
                            showCustomDialog = !showCustomDialog
                        },
                        shape = RoundedCornerShape(50.dp),
                        contentPadding = PaddingValues(10.dp),
                        enabled = !isBusy!!,
                    ) {
                        Text(
                            modifier = Modifier.padding(10.dp, 10.dp, 10.dp, 10.dp),
                            text = stringResource(R.string.choix_tournees),
                            fontSize = 20.sp,
                            textAlign = TextAlign.Center,
                        )
                    }

                    // Bouton pour synchroniser les tournées
                    Button(
                        modifier = Modifier.padding(0.dp, 20.dp, 0.dp, 20.dp).fillMaxWidth(1f),
                        onClick = {
                            syncViewModel.synchro(context.applicationContext as Application)
                        },
                        shape = RoundedCornerShape(50.dp),
                        contentPadding = PaddingValues(10.dp),
                        enabled = !isBusy!!,
                    ) {
                        Text(
                            modifier = Modifier.padding(10.dp, 10.dp, 10.dp, 10.dp),
                            text = stringResource(R.string.synchro_tournees),
                            fontSize = 20.sp,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.Start,
            ) {
                Text(
                    text = stringResource(R.string.historique_synchro),
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                )
            }
        }
    }

    if (showCustomDialog) {
        ChoixTourneeDialog(choixTourneeViewModel) {
            showCustomDialog = !showCustomDialog
        }
    }
}
