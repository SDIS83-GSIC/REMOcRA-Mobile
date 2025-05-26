package fr.sdis83.remocra.mobile.ui.screens.login

import android.app.Application
import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import fr.sdis83.remocra.mobile.MainActivity
import fr.sdis83.remocra.mobile.R
import fr.sdis83.remocra.mobile.ui.screens.administration.MdpAdministrateurDialog
import fr.sdis83.remocra.mobile.utils.getVersionName
import fr.sdis83.remocra.mobile.utils.pxToDp
import fr.sdis83.remocra.mobile.viewmodels.AdministrationViewModel
import fr.sdis83.remocra.mobile.viewmodels.AuthentViewModel
import fr.sdis83.remocra.mobile.viewmodels.ExportViewModel
import fr.sdis83.remocra.mobile.viewmodels.ParametreViewModel

@Composable
fun LoginScreen(viewModel: AuthentViewModel, administrationViewModel: AdministrationViewModel, application: Application, isMdm: Boolean) {
    val context = LocalContext.current
    val exportViewModel = ExportViewModel(context.applicationContext as Application)

    val parametreViewModel = ParametreViewModel((context as MainActivity).application.applicationContext as Application)
    val mdpAdmin by parametreViewModel.mdpAdmin.observeAsState()

    val url = context.resources.getString(R.string.url_api)

    val preferences = context.getSharedPreferences(
        context.getString(R.string.app_name),
        Context.MODE_PRIVATE,
    )

    var showCustomDialog by remember {
        mutableStateOf(false)
    }

    Row(
        modifier = Modifier.padding(20.pxToDp),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (!isMdm) {
            Button(
                onClick = {
                    if (!mdpAdmin.isNullOrBlank()) {
                        showCustomDialog = true
                    } else {
                        administrationViewModel.setAdministrationScreen(true)
                    }
                },
                enabled = (
                    viewModel.referentielStatus.value == AuthentViewModel.Companion.JobStatus.WAITING ||
                        viewModel.referentielStatus.value == AuthentViewModel.Companion.JobStatus.ERROR
                    ),
            ) {
                Text(stringResource(R.string.administrer))
            }
        }
        Button(
            modifier = Modifier.padding(10.pxToDp, 0.pxToDp),
            onClick = {
                exportViewModel.exportDiagnostics(context)
            },
        ) {
            Text(stringResource(R.string.exporterLogs))
        }
        Text(
            modifier = Modifier.padding(10.pxToDp, 0.pxToDp),
            text = "Version : ${getVersionName(applicationContext = application)}",
            fontWeight = FontWeight.Bold,
        )
    }
    Column(
        modifier = Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Button(
            onClick = {
                viewModel.login(context)
            },
            enabled = (
                viewModel.referentielStatus.value == AuthentViewModel.Companion.JobStatus.WAITING ||
                    viewModel.referentielStatus.value == AuthentViewModel.Companion.JobStatus.ERROR
                ) && !preferences.getString(url, "").isNullOrBlank(),
        ) {
            Text(
                if (preferences.getString(url, "").isNullOrBlank()) {
                    "Configurer l'URL via le bouton Administrer"
                } else {
                    "Se connecter"
                },
            )
        }
        Text(viewModel.info.value)
    }
    if (!mdpAdmin.isNullOrBlank() && showCustomDialog) {
        MdpAdministrateurDialog(administrationViewModel) {
            showCustomDialog = !showCustomDialog
        }
    }
}
