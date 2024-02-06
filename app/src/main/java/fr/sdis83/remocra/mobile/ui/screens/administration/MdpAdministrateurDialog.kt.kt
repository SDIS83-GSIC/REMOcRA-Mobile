package fr.sdis83.remocra.mobile.ui.screens.administration

import android.app.Application
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.em
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import fr.sdis83.remocra.mobile.AdministrationActivity
import fr.sdis83.remocra.mobile.LoginActivity
import fr.sdis83.remocra.mobile.R
import fr.sdis83.remocra.mobile.utils.GlobalConstants
import fr.sdis83.remocra.mobile.utils.pxToDp
import fr.sdis83.remocra.mobile.viewmodels.AdministrationViewModel
import fr.sdis83.remocra.mobile.viewmodels.ParamConfViewModel

@Composable
fun MdpAdministrateurDialog(administrationViewModel: AdministrationViewModel, onDismiss: () -> Unit) {
    val context = LocalContext.current
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    val paramConfViewModel = ParamConfViewModel(
        (context as LoginActivity).application.applicationContext as Application,
    )
    val paramConfList by paramConfViewModel.paramsConf.observeAsState()

    Dialog(
        onDismissRequest = { onDismiss() },
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
        ),
    ) {
        Card(
            shape = RoundedCornerShape(10.pxToDp),
            modifier = Modifier
                .size(2000.pxToDp, 600.pxToDp)
                .padding(50.pxToDp),
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .background(Color.White),
                Arrangement.Center,
            ) {
                Text(
                    text = stringResource(R.string.mdp_admin),
                    modifier = Modifier.padding(10.pxToDp),
                    fontSize = 4.em,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 1.em,
                    textAlign = TextAlign.Center,
                )

                Row(
                    Modifier
                        .padding(15.pxToDp)
                        .fillMaxWidth()
                        .fillMaxHeight(0.5f),
                    Arrangement.Center,
                ) {
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
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            val image = if (passwordVisible) {
                                Icons.Filled.VisibilityOff
                            } else {
                                Icons.Filled.Visibility
                            }

                            IconButton(
                                onClick = {
                                    passwordVisible = !passwordVisible
                                },
                            ) {
                                Icon(imageVector = image, contentDescription = "")
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    )
                }
                Row(Modifier.padding(10.pxToDp).fillMaxWidth(), Arrangement.Center) {
                    Button(
                        onClick = {
                            if (password.isNotBlank()) {
                                if (password == paramConfList?.first { it.cle == GlobalConstants.MDP_ADMINISTRATEUR }?.valeur) {
                                    administrationViewModel.setAdministrationScreen(true)
                                    val intent = Intent(
                                        context,
                                        AdministrationActivity::class.java,
                                    )
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                                    ContextCompat.startActivity(
                                        context,
                                        intent,
                                        null,
                                    )
                                } else {
                                    Toast.makeText(context, "Mot de passe incorrect", Toast.LENGTH_LONG)
                                        .show()
                                }
                            }
                        },
                    ) {
                        Text(stringResource(id = R.string.valider))
                    }
                }
            }
        }
    }
}
