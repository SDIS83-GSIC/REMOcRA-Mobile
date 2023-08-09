package fr.sdis83.remocra.mobile.ui.screens.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import fr.sdis83.remocra.mobile.R
import fr.sdis83.remocra.mobile.viewmodels.AdministrationViewModel
import fr.sdis83.remocra.mobile.viewmodels.LoginViewModel

@Composable
fun LoginScreen(viewModel: LoginViewModel, administrationViewModel: AdministrationViewModel) {
    var username: String by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    Button(
        modifier = Modifier.padding(20.dp, 20.dp),
        onClick = {
            administrationViewModel.setAdministrationScreen(true)
        },
        enabled = !viewModel.isBusy,
    ) {
        Text(stringResource(R.string.administrer))
    }
    Column(
        modifier = Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        OutlinedTextField(
            value = username,
            onValueChange = { newText: String ->
                username = newText
            },
            label = {
                Text(text = stringResource(R.string.username))
            },
            placeholder = {
                Text(text = stringResource(R.string.username))
            },
            singleLine = true,
            enabled = !viewModel.isBusy,
        )
        OutlinedTextField(
            value = password,
            onValueChange = { newText: String ->
                password = newText
            },
            label = {
                Text(text = stringResource(R.string.password))
            },
            placeholder = {
                Text(text = stringResource(R.string.password))
            },
            singleLine = true,
            visualTransformation = if (passwordVisible && !viewModel.isBusy) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                val image = if (passwordVisible && !viewModel.isBusy) {
                    Icons.Filled.VisibilityOff
                } else {
                    Icons.Filled.Visibility
                }

                IconButton(
                    enabled = !viewModel.isBusy,
                    onClick = {
                    passwordVisible = !passwordVisible
                }) {
                    Icon(imageVector = image, contentDescription = "")
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            enabled = !viewModel.isBusy,
        )
        Button(
            onClick = {
                if (!username.isNullOrBlank() && !password.isNullOrBlank() && !viewModel.isBusy) {
                    viewModel.login(username, password)
                }
            },
            enabled = !username.isNullOrBlank() && !password.isNullOrBlank() && !viewModel.isBusy,
        ) {
            Text("Connexion")
        }
        Text(viewModel.info.value)
    }
}
