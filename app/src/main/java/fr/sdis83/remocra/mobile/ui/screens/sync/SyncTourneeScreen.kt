package fr.sdis83.remocra.mobile.ui.screens.sync

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import fr.sdis83.remocra.mobile.R
import fr.sdis83.remocra.mobile.database.TourneesDao
import fr.sdis83.remocra.mobile.navigation.Screens
import fr.sdis83.remocra.mobile.ui.components.HeaderAppBar
import fr.sdis83.remocra.mobile.utils.pxToDp
import fr.sdis83.remocra.mobile.viewmodels.SyncTourneeViewModel

/**
 * Le but de cette écran est de lister toutes les tournées qui sont en cours avec les informations suivantes :
 * * le nom de la tournée
 * * Le nombre de PEI visités / le nombre total de PEI
 * * Si la tournée est terminée, on propose un bouton "Synchroniser" qui permet de synchroniser la tournée avec le serveur
 */

@Composable
fun SyncTourneeScreen(navController: NavController) {
    val viewModel: SyncTourneeViewModel = viewModel()
    val lifecycleOwner = LocalLifecycleOwner.current

    val tourneesReservees by viewModel._tourneesSynchro.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val errorMessageSynchro by viewModel.errorMessageSynchro.collectAsState()

    LaunchedEffect(key1 = Unit) {
        viewModel.chargerTourneesASynchroniser()
    }

    androidx.compose.runtime.DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.chargerTourneesASynchroniser()
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
            title = stringResource(R.string.synchro_tournees),
            returnAction = {
                navController.popBackStack(
                    Screens.Sync.route,
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
            } else if (tourneesReservees.isEmpty()) {
                // Aucune tournée
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "Aucune tournée réservée",
                        fontSize = 2.em,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                Column(modifier = Modifier.fillMaxSize()) {
                    if (errorMessageSynchro != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.pxToDp)
                                .clip(RoundedCornerShape(8.pxToDp))
                                .background(MaterialTheme.colorScheme.errorContainer)
                                .padding(12.pxToDp),
                        ) {
                            Text(
                                text = errorMessageSynchro.toString(),
                                color = MaterialTheme.colorScheme.onErrorContainer,
                            )
                        }
                    }
                    // Liste des tournées
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 150.pxToDp),
                    ) {
                        items(tourneesReservees.sortedByDescending { it.progression }) { tourneeAvancement ->
                            TourneeReserveeItem(
                                tourneeAvancement = tourneeAvancement,
                                onSynchroniserClick = {
                                    viewModel.synchroniserTourneeReservee(tourneeAvancement.tournee.tourneeId)
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

/**
 * Composant pour afficher une tournée avec :
 * - Nom de la tournée
 * - Barre de progression (PEI visités / PEI total)
 */
@Composable
fun TourneeReserveeItem(
    tourneeAvancement: TourneesDao.TourneeAvancement,
    onSynchroniserClick: () -> Unit,
) {
    val tournee = tourneeAvancement.tournee
    val doneCount = tourneeAvancement.doneCount
    val totalCount = tournee.peiCount
    val progression = tourneeAvancement.progression
    val estTerminee = progression >= 1.0f
    val cardBackgroundColor =
        if (estTerminee) {
            MaterialTheme.colorScheme.secondaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceVariant
        }
    val titleColor =
        if (estTerminee) {
            MaterialTheme.colorScheme.onSecondaryContainer
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 160.pxToDp)
            .clip(RoundedCornerShape(8.pxToDp))
            .background(cardBackgroundColor)
            .padding(12.pxToDp),
    ) {
        // Nom de la tournée seul
        Text(
            text = tournee.nom,
            fontSize = 3.em,
            fontWeight = FontWeight.Bold,
            color = titleColor,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.pxToDp),
        )

        // Barre de progression + compteur + statut + bouton synchro
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.pxToDp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                LinearProgressIndicator(
                    progress = progression,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.pxToDp)
                        .clip(RoundedCornerShape(4.pxToDp)),
                    color = if (estTerminee) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                )
            }
            Spacer(modifier = Modifier.width(12.pxToDp))
            Text(
                text = "$doneCount / $totalCount",
                fontSize = 1.5.em,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.width(12.pxToDp))
            Text(
                text = if (estTerminee) "Terminée" else "En cours...",
                fontSize = 1.8.em,
                fontWeight = FontWeight.Bold,
                color = if (estTerminee) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary,
            )
            FilledIconButton(
                onClick = onSynchroniserClick,
                enabled = estTerminee,
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                    disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                ),
                modifier = Modifier.padding(start = 8.pxToDp),
            ) {
                Icon(
                    imageVector = Icons.Filled.Sync,
                    contentDescription = "Synchroniser",
                )
            }
        }
    }
}
