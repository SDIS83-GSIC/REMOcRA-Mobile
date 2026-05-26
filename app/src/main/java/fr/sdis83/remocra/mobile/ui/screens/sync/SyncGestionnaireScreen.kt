package fr.sdis83.remocra.mobile.ui.screens.sync

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import fr.sdis83.remocra.mobile.R
import fr.sdis83.remocra.mobile.database.Contact
import fr.sdis83.remocra.mobile.database.SynchronisationDao
import fr.sdis83.remocra.mobile.navigation.Screens
import fr.sdis83.remocra.mobile.ui.components.HeaderAppBar
import fr.sdis83.remocra.mobile.ui.components.SyncStatBadge
import fr.sdis83.remocra.mobile.utils.pxToDp
import fr.sdis83.remocra.mobile.viewmodels.SyncGestionnaireViewModel
import java.util.UUID

/**
 * Le but de cet écran est de permettre la synchronisation gestionnaires par gestionnaires :
 * On affiche juste les propriétés des gestionnaires et des contacts qui ont été modifiés.
 */

@Composable
fun SyncGestionnaireScreen(navController: NavController) {
    val viewModel: SyncGestionnaireViewModel = viewModel()
    val lifecycleOwner = LocalLifecycleOwner.current

    val gestionnairesWithContacts by viewModel._gestionnaireASynchro.collectAsState()
    val groupedGestionnaires = gestionnairesWithContacts.toGestionnaireRows()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val errorMessageSynchro by viewModel.errorMessageSynchro.collectAsState()

    LaunchedEffect(key1 = Unit) {
        viewModel.chargerGestionnaireASynchro()
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.chargerGestionnaireASynchro()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(30.pxToDp)
            .background(MaterialTheme.colorScheme.surface),
    ) {
        HeaderAppBar(
            title = stringResource(R.string.synchro_gestionnaire_contact),
            returnAction = {
                navController.popBackStack(
                    Screens.Tournees.route,
                    inclusive = false,
                )
            },
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 15.pxToDp, vertical = 16.pxToDp),
        ) {
            // État de chargement
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            } else if (errorMessage != null) {
                // Affichage erreur
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.pxToDp)
                        .clip(RoundedCornerShape(8.pxToDp))
                        .background(MaterialTheme.colorScheme.errorContainer)
                        .padding(12.pxToDp),
                ) {
                    Text(
                        text = "Erreur : ${errorMessage ?: "Erreur inconnue"}",
                        color = MaterialTheme.colorScheme.onErrorContainer,
                    )
                }
            } else if (gestionnairesWithContacts.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "Aucune modification pour les gestionnaires ou leurs contacts",
                        fontSize = 2.em,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                Column(modifier = Modifier.fillMaxSize()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 30.pxToDp)
                            .padding(horizontal = 150.pxToDp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(
                            modifier = Modifier.weight(1f).padding(horizontal = 8.pxToDp),
                            horizontalArrangement = Arrangement.spacedBy(8.pxToDp),
                        ) {
                            SyncStatBadge(
                                label = "Gestionnaires édités",
                                value = "${
                                    gestionnairesWithContacts.distinctBy { it.gestionnaireId }
                                        .count { it.gestionnaireEdited }
                                }",
                                modifier = Modifier,
                                containerColor = Color(63, 191, 63).copy(alpha = 0.5f),
                                contentColor = Color.Black,
                            )
                            SyncStatBadge(
                                label = "Contacts édités",
                                value = "${gestionnairesWithContacts.count { it.contactEdited == true }}",
                                modifier = Modifier,
                                containerColor = Color(191, 63, 191).copy(alpha = 0.5f),
                                contentColor = Color.Black,
                            )
                        }
                        Spacer(modifier = Modifier.width(12.pxToDp))

                        FilledTonalButton(
                            onClick = {
                                viewModel.synchroniserTousGestionnaires(
                                    groupedGestionnaires.map { it.gestionnaireId },
                                )
                            },
                            enabled = gestionnairesWithContacts.isNotEmpty(),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            ),
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Sync,
                                contentDescription = null,
                            )
                            Spacer(modifier = Modifier.width(8.pxToDp))
                            Text("Synchroniser tous les gestionnaires et leurs contacts")
                        }
                    }

                    if (errorMessageSynchro != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.pxToDp)
                                .clip(RoundedCornerShape(8.pxToDp))
                                .background(MaterialTheme.colorScheme.errorContainer),
                        ) {
                            Text(
                                text = errorMessageSynchro.toString(),
                                color = MaterialTheme.colorScheme.onErrorContainer,
                            )
                        }
                    }

                    // Liste des gestionnaires à synchroniser
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 150.pxToDp),
                    ) {
                        items(
                            items = groupedGestionnaires,
                            key = { it.gestionnaireId },
                        ) { gestionnaireRow ->
                            GestionnaireSyncItem(
                                item = gestionnaireRow,
                                onSynchroniserClick = {
                                    viewModel.synchroniserGestionnaire(gestionnaireRow.gestionnaireId)
                                },
                            )
                            Spacer(modifier = Modifier.height(12.pxToDp))
                        }
                    }
                }
            }
        }
    }
}

private data class GestionnaireRowUi(
    val gestionnaireId: UUID,
    val gestionnaireLibelle: String,
    val gestionnaireCode: String,
    val gestionnaireEdited: Boolean,
    val contacts: List<ContactRowUi>,
)

private data class ContactRowUi(
    val contactId: UUID,
    val contactCivilite: Contact.Civilite?,
    val contactNom: String?,
    val contactPrenom: String?,
    val contactTelephone: String?,
    val contactEmail: String?,
    val contactCommuneText: String?,
)

private fun List<SynchronisationDao.GestionnaireWithContactRow>.toGestionnaireRows(): List<GestionnaireRowUi> {
    return groupBy { it.gestionnaireId }
        .map { (_, rows) ->
            GestionnaireRowUi(
                gestionnaireId = rows.first().gestionnaireId,
                gestionnaireLibelle = rows.first().gestionnaireLibelle,
                gestionnaireCode = rows.first().gestionnaireCode,
                gestionnaireEdited = rows.first().gestionnaireEdited,
                contacts = rows
                    .mapNotNull { row ->
                        row.contactId?.let {
                            ContactRowUi(
                                contactId = it,
                                contactCivilite = row.contactCivilite,
                                contactNom = row.contactNom,
                                contactPrenom = row.contactPrenom,
                                contactTelephone = row.contactTelephone,
                                contactEmail = row.contactEmail,
                                contactCommuneText = row.contactCommuneText,
                            )
                        }
                    }
                    .distinctBy { it.contactId },
            )
        }
        .sortedBy { it.gestionnaireLibelle }
}

@Composable
private fun GestionnaireSyncItem(
    item: GestionnaireRowUi,
    onSynchroniserClick: () -> Unit,
) {
    val cardColor = MaterialTheme.colorScheme.secondaryContainer

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.pxToDp),
        color = cardColor,
        tonalElevation = 2.pxToDp,
    ) {
        Column(modifier = Modifier.padding(12.pxToDp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.pxToDp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = item.gestionnaireLibelle,
                    modifier = Modifier.weight(2f),
                    fontSize = 2.2.em,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "Code : ${item.gestionnaireCode}",
                    modifier = Modifier.weight(1f),
                    fontSize = 2.2.em,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                FilledIconButton(
                    onClick = onSynchroniserClick,
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                    ),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Sync,
                        contentDescription = "Synchroniser le gestionnaire",
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.pxToDp))

            if (item.contacts.isNotEmpty()) {
                item.contacts.forEach { contact ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.pxToDp),
                        shape = RoundedCornerShape(6.pxToDp),
                        color = MaterialTheme.colorScheme.surface,
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.pxToDp),
                            horizontalArrangement = Arrangement.spacedBy(10.pxToDp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = contact.fullName(),
                                modifier = Modifier.weight(2f),
                                fontSize = 2.2.em,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Text(
                                text = "Tel : ${contact.contactTelephone.orEmpty().ifBlank { "-" }}",
                                modifier = Modifier.weight(1f),
                                fontSize = 2.em,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                text = "Email : ${contact.contactEmail.orEmpty().ifBlank { "-" }}",
                                modifier = Modifier.weight(1.4f),
                                fontSize = 2.em,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                text = "Commune : ${contact.contactCommuneText.orEmpty().ifBlank { "-" }}",
                                modifier = Modifier.weight(1.2f),
                                fontSize = 2.em,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun ContactRowUi.fullName(): String {
    val civilite =
        when (contactCivilite) {
            Contact.Civilite.M -> "M."
            Contact.Civilite.MME -> "Mme"
            null -> null
        }

    return listOfNotNull(civilite, contactPrenom?.trim().takeUnless { it.isNullOrBlank() }, contactNom?.trim().takeUnless { it.isNullOrBlank() })
        .joinToString(" ")
        .ifBlank { "Contact sans nom" }
}
