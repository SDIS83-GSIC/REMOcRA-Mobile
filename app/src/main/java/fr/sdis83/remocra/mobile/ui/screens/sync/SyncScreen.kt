package fr.sdis83.remocra.mobile.ui.screens.sync

import android.app.Application
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.work.Constraints
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import fr.sdis83.remocra.mobile.R
import fr.sdis83.remocra.mobile.synchronisation.SynchroContactRoleWorker
import fr.sdis83.remocra.mobile.synchronisation.SynchroContactWorker
import fr.sdis83.remocra.mobile.synchronisation.SynchroGestionnaireWorker
import fr.sdis83.remocra.mobile.synchronisation.SynchroHydrantVisiteAnomalieWorker
import fr.sdis83.remocra.mobile.synchronisation.SynchroHydrantVisiteWorker
import fr.sdis83.remocra.mobile.synchronisation.SynchroNewHydrantWorker
import fr.sdis83.remocra.mobile.viewmodels.ChoixTourneeViewModel

@Composable
fun SyncScreen() {
    val context = LocalContext.current

    val choixTourneeViewModel = ChoixTourneeViewModel(context.applicationContext as Application)

    var showCustomDialog by remember {
        mutableStateOf(false)
    }

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
                    .fillMaxWidth(0.5f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.Start,
            ) {
                // TODO : implémenter les strings
                Text(
                    modifier = Modifier.padding(0.dp, 0.dp, 0.dp, 20.dp),
                    text = stringResource(R.string.derniere_synchro),
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                )
                Text(
                    text = stringResource(R.string.visites_finies),
                    fontWeight = FontWeight.Normal,
                    fontSize = 18.sp,
                )
                Text(
                    text = stringResource(R.string.tournees_finies),
                    fontWeight = FontWeight.Normal,
                    fontSize = 18.sp,
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                ) {
                    // Bouton pour récupération des tournées
                    Button(
                        modifier = Modifier.padding(0.dp, 20.dp, 10.dp, 20.dp),
                        onClick = {
                            showCustomDialog = !showCustomDialog
                        },
                        shape = RoundedCornerShape(50.dp),
                        contentPadding = PaddingValues(10.dp),
                    ) {
                        Text(
                            modifier = Modifier.padding(10.dp, 10.dp, 10.dp, 10.dp),
                            text = stringResource(R.string.choix_tournees),
                            fontSize = 20.sp,
                        )
                    }

                    // Bouton pour synchroniser les tournées
                    Button(
                        modifier = Modifier.padding(0.dp, 20.dp, 0.dp, 20.dp),
                        onClick = {
                            synchro(context.applicationContext as Application)
                        },
                        shape = RoundedCornerShape(50.dp),
                        contentPadding = PaddingValues(10.dp),
                    ) {
                        Text(
                            modifier = Modifier.padding(10.dp, 10.dp, 10.dp, 10.dp),
                            text = stringResource(R.string.synchro_tournees),
                            fontSize = 20.sp,
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

@Preview(showSystemUi = true, device = "spec:width=1280dp,height=800dp,dpi=480")
@Composable
fun SyncScreenPreview() {
    Box {
        SyncScreen()
    }
}

private fun synchro(application: Application) {
    val constraints = Constraints.Builder()
        .setRequiresBatteryNotLow(true)
        .build()

    // On commence par les gestionnaires
    val synchroGestionnaire = OneTimeWorkRequestBuilder<SynchroGestionnaireWorker>()
        .setConstraints(constraints)
        .build()

    val synchroContact = OneTimeWorkRequestBuilder<SynchroContactWorker>()
        .setConstraints(constraints)
        .build()

    val synchroContactRole = OneTimeWorkRequestBuilder<SynchroContactRoleWorker>()
        .setConstraints(constraints)
        .build()

    val synchroNewHydrants = OneTimeWorkRequestBuilder<SynchroNewHydrantWorker>()
        .setConstraints(constraints)
        .build()

    val synchroHydrantVisiteWorker = OneTimeWorkRequestBuilder<SynchroHydrantVisiteWorker>()
        .setConstraints(constraints)
        .build()

    val synchroHydrantVisiteAnomalieWorker = OneTimeWorkRequestBuilder<SynchroHydrantVisiteAnomalieWorker>()
        .setConstraints(constraints)
        .build()

    WorkManager.getInstance(application).let { workManager ->
        workManager
            .beginWith(synchroGestionnaire)
            .then(synchroContact)
            .then(synchroContactRole)
            .then(synchroNewHydrants)
            .then(synchroHydrantVisiteWorker)
            .then(synchroHydrantVisiteAnomalieWorker)
            .enqueue()

        workManager.getWorkInfoByIdLiveData(synchroHydrantVisiteAnomalieWorker.id).observeForever {
            when (it.state) {
                WorkInfo.State.RUNNING -> {
                    Toast.makeText(application, "Synchronisation en cours...", Toast.LENGTH_LONG)
                        .show()
                }
                WorkInfo.State.SUCCEEDED -> {
                    Toast.makeText(application, "Synchronisation terminée.", Toast.LENGTH_LONG)
                        .show()
                }
                WorkInfo.State.FAILED -> {
                    Toast.makeText(application, "Echec lors de la synchronisation.", Toast.LENGTH_LONG)
                        .show()
                }

                else -> {
                    Toast.makeText(application, "En attente...", Toast.LENGTH_LONG)
                        .show()
                }
            }
        }
    }
}
