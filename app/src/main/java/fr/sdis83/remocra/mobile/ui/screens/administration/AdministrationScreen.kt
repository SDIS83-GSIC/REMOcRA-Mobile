package fr.sdis83.remocra.mobile.ui.screens.administration

import android.app.Application
import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
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
import fr.sdis83.remocra.mobile.R
import fr.sdis83.remocra.mobile.utils.pxToDp
import fr.sdis83.remocra.mobile.viewmodels.AdministrationViewModel
import fr.sdis83.remocra.mobile.viewmodels.ExportViewModel

@Composable
fun AdministrationScreen(viewModel: AdministrationViewModel) {
    val context = LocalContext.current
    val url = context.resources.getString(R.string.url_api)
    val preferences = context.getSharedPreferences(
        context.getString(R.string.app_name),
        Context.MODE_PRIVATE,
    )

    val administrationViewModel = AdministrationViewModel(context.applicationContext as Application)
    val exportViewModel = ExportViewModel(context.applicationContext as Application)

    var urlSaisie: String? by remember { mutableStateOf(null) }

    Row(Modifier.padding(20.pxToDp)) {
        Button(
            modifier = Modifier.padding(10.pxToDp, 0.pxToDp),
            onClick = {
                viewModel.setAdministrationScreen(true)
            },
        ) {
            Text(stringResource(R.string.retour))
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        OutlinedTextField(
            value = if (urlSaisie == null) preferences.getString(url, "")!! else urlSaisie!!,
            onValueChange = { newText: String ->
                urlSaisie = newText
            },
            label = {
                Text(text = stringResource(R.string.server))
            },
            placeholder = {
                Text(text = stringResource(R.string.server_placeholder))
            },
            singleLine = true,
        )
        Button(
            modifier = Modifier.padding(20.pxToDp),
            onClick = {
                if (urlSaisie != null) {
                    preferences.edit().putString(url, urlSaisie).apply()
                }

                administrationViewModel.checkUrl(context)
            },
        ) {
            Text(stringResource(R.string.verifier_connexion))
        }
    }
}
