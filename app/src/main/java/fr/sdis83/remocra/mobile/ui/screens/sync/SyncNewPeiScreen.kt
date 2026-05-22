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
import fr.sdis83.remocra.mobile.database.SynchronisationDao
import fr.sdis83.remocra.mobile.navigation.Screens
import fr.sdis83.remocra.mobile.ui.components.HeaderAppBar
import fr.sdis83.remocra.mobile.ui.components.SyncStatBadge
import fr.sdis83.remocra.mobile.utils.pxToDp
import fr.sdis83.remocra.mobile.viewmodels.SyncNewPeiViewModel

/**
 * Le but de cet écran est de lister toutes les nouveaux PEI qui ont été créés sur l'application mobile
 * On remonte les quelques informations essentielles (type, nature, domaine, coordonnées) pour permettre à l'utilisateur de les identifier
 */

@Composable
fun SyncNewPeiScreen(navController: NavController) {
    val viewModel: SyncNewPeiViewModel = viewModel()
    val lifecycleOwner = LocalLifecycleOwner.current

    val listeNewPeiASynchro by viewModel._newPeiASynchro.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val errorMessageSynchro by viewModel.errorMessageSynchro.collectAsState()

    LaunchedEffect(key1 = Unit) {
        viewModel.chargerNewPeiASynchro()
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.chargerNewPeiASynchro()
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
            title = stringResource(R.string.synchro_new_pei),
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
            } else if (listeNewPeiASynchro.isEmpty()) {
                // Aucun nouveau PEI
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "Aucun nouveau PEI",
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
                        SyncStatBadge(
                            label = "PEI créés",
                            value = "${listeNewPeiASynchro.size}",
                            modifier = Modifier,
                            containerColor = Color(63, 191, 63).copy(alpha = 0.5f),
                            contentColor = Color.Black,
                        )

                        Spacer(modifier = Modifier.width(12.pxToDp))

                        FilledTonalButton(
                            onClick = {
                                // TODO
                            },
                            enabled = listeNewPeiASynchro.isNotEmpty(),
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
                            Text("Synchroniser tous les nouveaux PEI")
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

                    // Liste des PEI à synchroniser
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 150.pxToDp),
                    ) {
                        items(listeNewPeiASynchro) { pei ->
                            NewPeiItem(
                                newPei = pei,
                                onSynchroniserClick = {
                                    viewModel.synchroniserNewPei(pei.pei.peiId)
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

@Composable
private fun NewPeiItem(
    newPei: SynchronisationDao.NewPeiWithDetails,
    modifier: Modifier = Modifier,
    onSynchroniserClick: () -> Unit = {},
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.pxToDp)),
        color = MaterialTheme.colorScheme.primaryContainer,
        tonalElevation = 2.pxToDp,
    ) {
        Column(
            modifier = Modifier.padding(12.pxToDp),
        ) {
            // Ligne avec les informations et bouton synchronisation aligné verticalement
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.pxToDp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Type de PEI
                InfoBadge(
                    label = "Type",
                    value = newPei.typePeiCode ?: "N/A",
                    modifier = Modifier.weight(1f),
                )

                // Nature du PEI
                InfoBadge(
                    label = "Nature",
                    value = newPei.natureLibelle ?: "N/A",
                    modifier = Modifier.weight(1f),
                )

                // Nature de CI
                InfoBadge(
                    label = "Nature DECI",
                    value = newPei.natureDeciLibelle ?: "N/A",
                    modifier = Modifier.weight(1f),
                )

                // Domaine
                InfoBadge(
                    label = "Domaine",
                    value = newPei.domaineLibelle ?: "N/A",
                    modifier = Modifier.weight(1f),
                )

                // Coordonnée X
                InfoBadge(
                    label = "X",
                    value = newPei.pei.x.toString(),
                    modifier = Modifier.weight(1f),
                )

                // Coordonnée Y
                InfoBadge(
                    label = "Y",
                    value = newPei.pei.y.toString(),
                    modifier = Modifier.weight(1f),
                )

                // Bouton de synchronisation aligné verticalement
                FilledIconButton(
                    onClick = onSynchroniserClick,
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                    ),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Sync,
                        contentDescription = "Synchroniser",
                    )
                }
            }

            // Adresse si disponible
            if (!newPei.pei.adresseComplete.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.pxToDp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Adresse:",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 0.9.em,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                    Spacer(modifier = Modifier.width(8.pxToDp))
                    Text(
                        text = newPei.pei.adresseComplete,
                        fontSize = 0.9.em,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }
        }
    }
}

@Composable
private fun InfoBadge(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = label,
            fontWeight = FontWeight.SemiBold,
            fontSize = 1.2.em,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
        )
        Text(
            text = value,
            fontWeight = FontWeight.Bold,
            fontSize = 1.5.em,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
        )
    }
}
