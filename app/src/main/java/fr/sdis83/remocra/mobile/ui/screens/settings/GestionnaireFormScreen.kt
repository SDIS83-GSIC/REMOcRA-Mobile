package fr.sdis83.remocra.mobile.ui.screens.settings

import android.app.Application
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import fr.sdis83.remocra.mobile.R
import fr.sdis83.remocra.mobile.navigation.Screens
import fr.sdis83.remocra.mobile.ui.components.ContactCard
import fr.sdis83.remocra.mobile.ui.components.HeaderAppBar
import fr.sdis83.remocra.mobile.viewmodels.GestionnairesViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.UUID

@Composable
fun GestionnaireFormScreen(navController: NavController?, idGestionnaire: UUID?) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val gestionnairesViewModel =
        GestionnairesViewModel(context.applicationContext as Application, idGestionnaire)

    GestionnaireFormScreenInner(
        gestionnairesViewModel,
        coroutineScope,
        navController,
    )
}

@Composable
fun GestionnaireFormScreenInner(
    gestionnairesViewModel: GestionnairesViewModel,
    coroutineScope: CoroutineScope,
    navController: NavController?,
) {
    val currentGestionnaire by gestionnairesViewModel.gestionnaireState.collectAsState() // Current gestionnaire
    val gestionnaire by gestionnairesViewModel.gestionnaire.observeAsState() // Construction du titre
    var mainTitle: String by remember { mutableStateOf("") }
    val contextCreation: Boolean = gestionnaire?.idGestionnaire == null
    val contactsList = gestionnairesViewModel.contactsList.observeAsState(listOf())

    mainTitle = if (contextCreation) {
        // Context = Création d'un gestionnaire
        stringResource(R.string.addGestionnaireST)
    } else {
        // Context = Modification d'un gestionnaire
        "${stringResource(R.string.editGestionnaireST)} ${gestionnaire?.nom}"
    }

    gestionnairesViewModel.updateForm(currentGestionnaire)

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
                .padding(10.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp, horizontal = 20.dp),
                ) {
                    OutlinedTextField(
                        modifier = Modifier
                            .fillMaxWidth(.5f)
                            .padding(end = 5.dp),
                        value = currentGestionnaire.nom ?: "",
                        onValueChange = { it: String ->
                            gestionnairesViewModel.updateForm(
                                currentGestionnaire.copy(nom = it),
                            )
                        },
                        label = { Text(text = stringResource(R.string.formGestionnaireName)) },
                        placeholder = { Text(text = stringResource(R.string.formGestionnaireNamePH)) },
                        singleLine = true,
                        isError = !gestionnairesViewModel.gestionnaireValidState.value.isNomValid,
                        supportingText = {
                            if (!gestionnairesViewModel.gestionnaireValidState.value.isNomValid) {
                                Text(text = "Ce champ est obligatoire")
                            }
                        },
                    )
                    OutlinedTextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 5.dp),
                        value = currentGestionnaire.code ?: "",
                        onValueChange = { it: String ->
                            gestionnairesViewModel.updateForm(
                                currentGestionnaire.copy(code = it),
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
                            .padding(horizontal = 20.dp),
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
                                        fontSize = 25.sp,
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
                                                        oldValue = "{idGestionnaire}",
                                                        newValue = gestionnaire?.idGestionnaire.toString(),
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
                                    .height(250.dp),
                            ) {
                                Column(Modifier.padding(horizontal = 80.dp)) {
                                    if (contactsList.value.isEmpty()) {
                                        Text(text = stringResource(R.string.noContact))
                                    } else {
                                        LazyVerticalGrid(
                                            modifier = Modifier.border(color = Color.Black, width = .5.dp),
                                            columns = GridCells.Adaptive(minSize = 350.dp),
                                        ) {
                                            items(contactsList.value) { contact ->
                                                if (navController != null) {
                                                    ContactCard(
                                                        contact = contact,
                                                        navController = navController,
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
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(all = 20.dp),
                    verticalAlignment = Alignment.Bottom,
                ) {
                    Button( // "Enregistrer ce gestionnaire"
                        enabled = gestionnairesViewModel.gestionnaireValidState.value.isValid,
                        onClick = {
                            coroutineScope.launch {
                                gestionnairesViewModel.upsertGestionnaire(currentGestionnaire)
                                if (contextCreation) { // Création
                                    navController?.navigate(
                                        Screens.CreateContact.route
                                            .replace(
                                                oldValue = "{idGestionnaire}",
                                                newValue = currentGestionnaire.idGestionnaire.toString(),
                                            ),
                                    )
                                } else { // Modification
                                    navController?.navigate(Screens.ListGestionnaire.route)
                                }
                            }
                        },
                    ) {
                        Text(text = stringResource(R.string.saveGestionnaireBTN))
                    }
                }
            }
        }
    }
}
