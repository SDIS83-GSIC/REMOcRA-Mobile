package fr.sdis83.remocra.mobile.ui.screens.settings

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
import fr.sdis83.remocra.mobile.viewmodels.GestionnaireViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.UUID

@Composable
fun GestionnaireFormScreen(navController: NavController?, gestionnaireId: UUID?) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val gestionnaireViewModel =
        GestionnaireViewModel(context.applicationContext as Application, gestionnaireId)

    GestionnaireFormScreenInner(
        gestionnaireViewModel,
        coroutineScope,
        navController,
    )
}

@Composable
fun GestionnaireFormScreenInner(
    gestionnaireViewModel: GestionnaireViewModel,
    coroutineScope: CoroutineScope,
    navController: NavController?,
) {
    val currentGestionnaire by gestionnaireViewModel.gestionnaireState.collectAsState() // Current gestionnaire
    val gestionnaire by gestionnaireViewModel.gestionnaire.observeAsState() // Construction du titre
    val fonctionContactList by gestionnaireViewModel.fonctionContactList.observeAsState()
    var mainTitle: String by remember { mutableStateOf("") }
    val contextCreation: Boolean = gestionnaire?.gestionnaireId == null
    val contactsList = gestionnaireViewModel.contactsList.observeAsState(listOf())

    mainTitle = if (contextCreation) {
        // Context = Création d'un gestionnaire
        stringResource(R.string.addGestionnaireST)
    } else {
        // Context = Modification d'un gestionnaire
        "${stringResource(R.string.editGestionnaireST)} ${gestionnaire?.gestionnaireLibelle}"
    }

    gestionnaireViewModel.updateForm(currentGestionnaire)

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Top,
    ) {
        HeaderAppBar(
            title = mainTitle,
            returnAction = { navController?.popBackStack() },
        )
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.pxToDp),
            verticalAlignment = Alignment.Top,
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.pxToDp, horizontal = 20.pxToDp),
                ) {
                    OutlinedTextField(
                        modifier = Modifier
                            .fillMaxWidth(.5f)
                            .padding(end = 5.pxToDp),
                        value = currentGestionnaire.gestionnaireLibelle ?: "",
                        onValueChange = { it: String ->
                            gestionnaireViewModel.updateForm(
                                currentGestionnaire.copy(gestionnaireLibelle = it),
                            )
                        },
                        label = { Text(text = stringResource(R.string.formGestionnaireName)) },
                        placeholder = { Text(text = stringResource(R.string.formGestionnaireNamePH)) },
                        singleLine = true,
                        isError = !gestionnaireViewModel.gestionnaireValidState.value.isNomValid,
                        supportingText = {
                            if (!gestionnaireViewModel.gestionnaireValidState.value.isNomValid) {
                                Text(text = "Ce champ est obligatoire")
                            }
                        },
                    )
                    OutlinedTextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 5.pxToDp),
                        value = currentGestionnaire.gestionnaireCode ?: "",
                        onValueChange = { it: String ->
                            gestionnaireViewModel.updateForm(
                                currentGestionnaire.copy(gestionnaireCode = it),
                            )
                        },
                        label = { Text(text = stringResource(R.string.formGestionnaireSiren)) },
                        placeholder = { Text(text = stringResource(R.string.formGestionnaireSirenPH)) },
                        singleLine = true,
                    )
                }
                if (!contextCreation) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.pxToDp),
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier,
                                horizontalArrangement = Arrangement.Start,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxWidth(.5f),
                                ) {
                                    Text(
                                        modifier = Modifier.fillMaxWidth(),
                                        text = stringResource(R.string.associatedContactSubST),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 4.em,
                                    )
                                }
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.End,
                                ) {
                                    OutlinedButton(
                                        modifier = Modifier,
                                        onClick = {
                                            navController?.navigate(
                                                Screens.CreateContact.route
                                                    .replace(
                                                        oldValue = "{gestionnaireId}",
                                                        newValue = gestionnaire?.gestionnaireId.toString(),
                                                    ),
                                            )
                                        },
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Add,
                                            contentDescription = "CreateContact",
                                        )
                                        Text(
                                            modifier = Modifier,
                                            text = stringResource(R.string.addContactClickText),
                                            textAlign = TextAlign.Center,
                                        )
                                    }
                                }
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(250.pxToDp),
                            ) {
                                if (contactsList.value.isEmpty()) {
                                    Column(Modifier.padding(horizontal = 80.pxToDp)) {
                                        Text(text = stringResource(R.string.noContact))
                                    }
                                } else {
                                    LazyColumn(
                                        Modifier.padding(horizontal = 80.pxToDp),
                                    ) {
                                        items(contactsList.value) { contact ->
                                            if (navController != null) {
                                                Row(
                                                    Modifier
                                                        .padding(8.pxToDp)
                                                        .fillMaxWidth(),
                                                ) {
                                                    Box(
                                                        modifier =
                                                        Modifier
                                                            .clip(RoundedCornerShape(8.pxToDp))
                                                            .background(Color(0xDDE9F3FF))
                                                            .padding(16.pxToDp)
                                                            .fillMaxWidth(),
                                                    ) {
                                                        Column {
                                                            Row {
                                                                Column(
                                                                    Modifier
                                                                        .weight(1f),
                                                                ) {
                                                                    Text(
                                                                        text = "${contact.contactNom ?: ""} ${contact.contactPrenom ?: ""}",
                                                                        fontWeight = FontWeight.Bold,
                                                                    )
                                                                    contact.contactFonctionContactId?.let {
                                                                        Text(text = "Fonction : ${fonctionContactList?.find{ f -> f.fonctionContactId == it}?.fonctionContactLibelle}")
                                                                    }
                                                                }
                                                                Column {
                                                                    IconButton(
                                                                        onClick = {
                                                                            navController.navigate(
                                                                                Screens.EditContact.route
                                                                                    .replace(
                                                                                        oldValue = "{gestionnaireId}",
                                                                                        newValue = contact.gestionnaireId.toString(),
                                                                                    )
                                                                                    .replace(
                                                                                        oldValue = "{contactId}",
                                                                                        newValue = contact.contactId.toString(),
                                                                                    ),
                                                                            )
                                                                        },
                                                                    ) {
                                                                        Icon(
                                                                            imageVector = Icons.Filled.Edit,
                                                                            contentDescription = "EditContact",
                                                                            Modifier.size(30.pxToDp),
                                                                        )
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(all = 10.pxToDp),
                    verticalAlignment = Alignment.Bottom,
                ) {
                    Button(
                        // "Enregistrer ce gestionnaire"
                        enabled = gestionnaireViewModel.gestionnaireValidState.value.isValid,
                        onClick = {
                            coroutineScope.launch {
                                gestionnaireViewModel.upsertGestionnaire(currentGestionnaire)
                                if (contextCreation) { // Création
                                    navController?.popBackStack(
                                        Screens.Settings.route,
                                        inclusive = false,
                                    )
                                } else { // Modification
                                    navController?.navigate(Screens.ListGestionnaire.route)
                                }
                            }
                        },
                    ) {
                        Text(text = stringResource(R.string.saveGestionnaireBTN))
                    }
                    if (contextCreation) {
                        Spacer(modifier = Modifier.width(16.pxToDp))
                        Button(
                            enabled = gestionnaireViewModel.gestionnaireValidState.value.isValid,
                            onClick = {
                                coroutineScope.launch {
                                    gestionnaireViewModel.upsertGestionnaire(currentGestionnaire)
                                    navController?.navigate(
                                        Screens.CreateContact.route
                                            .replace(
                                                oldValue = "{gestionnaireId}",
                                                newValue = currentGestionnaire.gestionnaireId.toString(),
                                            ),
                                    ) {
                                        popUpTo(Screens.Settings.route)
                                    }
                                }
                            },
                        ) {
                            Text(text = "Ajouter un contact")
                        }
                    }
                }
            }
        }
    }
}
