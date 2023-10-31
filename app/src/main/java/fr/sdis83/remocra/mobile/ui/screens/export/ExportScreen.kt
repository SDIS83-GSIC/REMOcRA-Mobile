package fr.sdis83.remocra.mobile.ui.screens.export

import android.app.Application
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import fr.sdis83.remocra.mobile.R
import fr.sdis83.remocra.mobile.navigation.Screens
import fr.sdis83.remocra.mobile.ui.components.HeaderAppBar
import fr.sdis83.remocra.mobile.viewmodels.ExportViewModel

@Composable
fun ExportScreen(navController: NavController?) {
    val context = LocalContext.current
    val exportViewModel = ExportViewModel(Application())

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Top,
    ) {
        HeaderAppBar(
            title = stringResource(R.string.diagnostics),
            returnAction = {
                navController?.popBackStack(
                    Screens.Tournees.route,
                    inclusive = false,
                )
            },
        )
        Column(
            modifier = Modifier.padding(20.dp, 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = "Cet écran vous permet d'exporter des données qui pourront être ensuite interprétées par l'équipe de développeurs." +
                    " 2 fichiers seront exportés à la racine de votre appareil (caracteristiques_remocra.log et logcat_remocra.log).",
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.width(16.dp))
            Button(
                modifier = Modifier.padding(20.dp, 20.dp),
                onClick = {
                    // Export worker
                    exportViewModel.exportDiagnostics(context)
                },
            ) {
                Text(stringResource(R.string.exporterLogs))
            }
        }
    }
}
