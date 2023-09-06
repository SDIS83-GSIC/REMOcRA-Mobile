package fr.sdis83.remocra.mobile.ui.screens.settings

import android.app.Application
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import fr.sdis83.remocra.mobile.R
import fr.sdis83.remocra.mobile.database.Contact
import fr.sdis83.remocra.mobile.navigation.Screens
import fr.sdis83.remocra.mobile.ui.components.HeaderAppBar
import fr.sdis83.remocra.mobile.viewmodels.ContactsViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.UUID

@Composable
fun ContactFormScreen(navController: NavController?, idContact: UUID?, idGestionnaire: UUID) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val contactsViewModel =
        ContactsViewModel(context.applicationContext as Application, idContact, idGestionnaire)

    ContactFormScreenInner(
        contactsViewModel,
        coroutineScope,
        navController,
    )
}

@Composable
fun ContactFormScreenInner(
    contactsViewModel: ContactsViewModel,
    coroutineScope: CoroutineScope,
    navController: NavController?,
) {
    val currentContact by contactsViewModel.contactState.collectAsState() // Current Contact
    val contact by contactsViewModel.contact.observeAsState() // Infos statiques Contact
    val gestAppartenance by contactsViewModel.gestAppartenance.observeAsState() // Infos statiques gestionnaire

    /*Civilite*/
    val radioOptions = Contact.Civilite.values().toList()

    /*Roles*/
    val listRole = contactsViewModel.roleList.observeAsState(listOf())

    var mainTitle: String by remember { mutableStateOf("") }
    var contextCreation: Boolean = contact?.idContact == null

    mainTitle = if (contextCreation) {
        // Context = Création d'un contact
        "${stringResource(R.string.addContactST)} ${gestAppartenance?.nom}"
    } else {
        // Context = Modification d'un contact
        "${stringResource(R.string.editContactST)} ${contact?.nom} ${contact?.prenom}"
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp),
        verticalArrangement = Arrangement.Top,
    ) {
        HeaderAppBar(
            title = mainTitle,
            returnAction = { navController?.popBackStack(Screens.EditGestionnaire.route, inclusive = false) },
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .weight(.5f, false),
            verticalAlignment = Alignment.Top,
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(all = 15.dp),
                ) {
                    Text(
                        text = stringResource(R.string.persoInfoSubST),
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                ) {
                    OutlinedTextField(
                        modifier = Modifier
                            .fillMaxWidth(.5f),
                        value = currentContact.contact.fonction ?: "",
                        onValueChange = { it: String ->
                            contactsViewModel.updateForm(
                                currentContact.copy(
                                    contact = currentContact.contact.copy(fonction = it),
                                ),
                            )
                        },
                        label = { Text(text = stringResource(R.string.formContactFonction)) },
                        placeholder = { Text(text = stringResource(R.string.formContactFonctionPH)) },
                        singleLine = true,
                        // enabled = !viewModel.isBusy,
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .selectableGroup(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    radioOptions.forEach { civ ->
                        Column(
                            modifier = Modifier
                                .selectable(
                                    selected = (civ == currentContact.contact.civilite),
                                    onClick = {
                                        contactsViewModel.updateForm(
                                            currentContact.copy(
                                                contact = currentContact.contact.copy(
                                                    civilite = civ,
                                                ),
                                            ),
                                        )
                                    },
                                ),
                            verticalArrangement = Arrangement.Center,
                        ) {
                            Row(
                                modifier = Modifier.padding(end = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                RadioButton(
                                    selected = (civ == currentContact.contact.civilite),
                                    onClick = {
                                        contactsViewModel.updateForm(
                                            currentContact.copy(
                                                contact = currentContact.contact.copy(
                                                    civilite = civ,
                                                ),
                                            ),
                                        )
                                    },
                                )
                                Text(text = civ.toString())
                            }
                        }
                    }
                    OutlinedTextField(
                        modifier = Modifier
                            .fillMaxWidth(.5f)
                            .padding(start = 5.dp, end = 5.dp),
                        value = currentContact.contact.nom ?: "",
                        onValueChange = { it: String ->
                            contactsViewModel.updateForm(
                                currentContact.copy(
                                    contact = currentContact.contact.copy(nom = it),
                                ),
                            )
                        },
                        label = { Text(text = stringResource(R.string.formContactName)) },
                        placeholder = { Text(text = stringResource(R.string.formContactNamePH)) },
                        singleLine = true,
                        // enabled = !viewModel.isBusy,
                    )
                    OutlinedTextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 5.dp),
                        value = currentContact.contact.prenom ?: "",
                        onValueChange = { it: String ->
                            contactsViewModel.updateForm(
                                currentContact.copy(
                                    contact = currentContact.contact.copy(prenom = it),
                                ),
                            )
                        },
                        label = { Text(text = stringResource(R.string.formContactFirstName)) },
                        placeholder = { Text(text = stringResource(R.string.formContactFirstNamePH)) },
                        singleLine = true,
                        // enabled = !viewModel.isBusy,
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(all = 15.dp),
                ) {
                    Text(
                        text = stringResource(R.string.addressSubST),
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                ) {
                    OutlinedTextField(
                        modifier = Modifier
                            .fillMaxWidth(.15f)
                            .padding(end = 5.dp),
                        value = currentContact.contact.numeroVoie ?: "",
                        onValueChange = { it: String ->
                            contactsViewModel.updateForm(
                                currentContact.copy(
                                    contact = currentContact.contact.copy(numeroVoie = it),
                                ),
                            )
                        },
                        label = { Text(text = stringResource(R.string.formContactNum)) },
                        placeholder = { Text(text = stringResource(R.string.formContactNumPH)) },
                        singleLine = true,
                        // enabled = !viewModel.isBusy,
                    )
                    OutlinedTextField(
                        modifier = Modifier
                            .fillMaxWidth(.15f)
                            .padding(start = 5.dp, end = 5.dp),
                        value = currentContact.contact.suffixeVoie ?: "",
                        onValueChange = { it: String ->
                            contactsViewModel.updateForm(
                                currentContact.copy(
                                    contact = currentContact.contact.copy(suffixeVoie = it),
                                ),
                            )
                        },
                        label = { Text(text = stringResource(R.string.formContactSuffixe)) },
                        placeholder = { Text(text = stringResource(R.string.formContactSuffixePH)) },
                        singleLine = true,
                        // enabled = !viewModel.isBusy,
                    )
                    OutlinedTextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 5.dp),
                        value = currentContact.contact.voie ?: "",
                        onValueChange = { it: String ->
                            contactsViewModel.updateForm(
                                currentContact.copy(
                                    contact = currentContact.contact.copy(voie = it),
                                ),
                            )
                        },
                        label = { Text(text = stringResource(R.string.formContactVoie)) },
                        placeholder = { Text(text = stringResource(R.string.formContactVoiePH)) },
                        singleLine = true,
                        // enabled = !viewModel.isBusy,
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                ) {
                    OutlinedTextField(
                        modifier = Modifier
                            .fillMaxWidth(.15f)
                            .padding(end = 5.dp),
                        value = currentContact.contact.codePostal ?: "",
                        onValueChange = { it: String ->
                            contactsViewModel.updateForm(
                                currentContact.copy(
                                    contact = currentContact.contact.copy(codePostal = it),
                                ),
                            )
                        },
                        label = { Text(text = stringResource(R.string.formContactCP)) },
                        placeholder = { Text(text = stringResource(R.string.formContactCPPH)) },
                        singleLine = true,
                        // enabled = !viewModel.isBusy,
                    )
                    OutlinedTextField(
                        modifier = Modifier
                            .fillMaxWidth(.50f)
                            .padding(start = 5.dp, end = 5.dp),
                        value = currentContact.contact.ville ?: "",
                        onValueChange = { it: String ->
                            contactsViewModel.updateForm(
                                currentContact.copy(
                                    contact = currentContact.contact.copy(ville = it),
                                ),
                            )
                        },
                        label = { Text(text = stringResource(R.string.formContactVille)) },
                        placeholder = { Text(text = stringResource(R.string.formContactVillePH)) },
                        singleLine = true,
                        // enabled = !viewModel.isBusy,
                    )
                    OutlinedTextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 5.dp),
                        value = currentContact.contact.pays ?: "",
                        onValueChange = { it: String ->
                            contactsViewModel.updateForm(
                                currentContact.copy(
                                    contact = currentContact.contact.copy(pays = it),
                                ),
                            )
                        },
                        label = { Text(text = stringResource(R.string.formContactPays)) },
                        placeholder = { Text(text = stringResource(R.string.formContactPaysPH)) },
                        singleLine = true,
                        // enabled = !viewModel.isBusy,
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(all = 15.dp),
                ) {
                    Text(
                        text = stringResource(R.string.contactInfoSubST),
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                ) {
                    OutlinedTextField(
                        modifier = Modifier
                            .fillMaxWidth(.3f)
                            .padding(end = 5.dp),
                        value = currentContact.contact.telephone ?: "",
                        onValueChange = { it: String ->
                            contactsViewModel.updateForm(
                                currentContact.copy(
                                    contact = currentContact.contact.copy(telephone = it),
                                ),
                            )
                        },
                        label = { Text(text = stringResource(R.string.formContactTel)) },
                        placeholder = { Text(text = stringResource(R.string.formContactTelPH)) },
                        singleLine = true,
                        // enabled = !viewModel.isBusy,
                    )
                    OutlinedTextField(
                        modifier = Modifier
                            .fillMaxWidth(.50f)
                            .padding(start = 5.dp, end = 5.dp),
                        value = currentContact.contact.email ?: "",
                        onValueChange = { it: String ->
                            contactsViewModel.updateForm(
                                currentContact.copy(
                                    contact = currentContact.contact.copy(email = it),
                                ),
                            )
                        },
                        label = { Text(text = stringResource(R.string.formContactEmail)) },
                        placeholder = { Text(text = stringResource(R.string.formContactEmailPH)) },
                        singleLine = true,
                        // enabled = !viewModel.isBusy,
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(all = 15.dp),
                ) {
                    Text(
                        text = stringResource(R.string.roleSubST),
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                Row(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth(.7f),
                        horizontalAlignment = Alignment.Start,
                    ) {
                        listRole.value.forEach { role ->
                            val checked = remember { mutableStateOf(currentContact.roles.contains(role)) }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = checked.value,
                                    onCheckedChange = {
                                        checked.value = it
                                        if (it) {
                                            currentContact.copy(
                                                roles = currentContact.roles.apply { add(role) },
                                            )
                                        } else {
                                            currentContact.copy(
                                                roles = currentContact.roles.apply { remove(role) },
                                            )
                                        }
                                    },
                                )
                                Text(
                                    modifier = Modifier.padding(start = 2.dp),
                                    text = role.nom.toString(),
                                )
                            }
                        }
                    }
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(all = 20.dp),
                        verticalArrangement = Arrangement.Top,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    contactsViewModel.upsertContactWithRoles(currentContact)
                                    navController?.navigate(
                                        Screens.EditGestionnaire.route
                                            .replace(
                                                oldValue = "{idGestionnaire}",
                                                newValue = currentContact.contact.idGestionnaire.toString(),
                                            ),
                                    ) {
                                        popUpTo(Screens.Settings.route)
                                    }
                                }
                            },
                        ) {
                            Text(text = stringResource(R.string.saveContactBTN))
                        }
                    }
                }
            }
        }
    }
}
