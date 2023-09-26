package fr.sdis83.remocra.mobile.ui.screens.tournees

import android.app.Application
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import fr.sdis83.remocra.mobile.navigation.Screens
import fr.sdis83.remocra.mobile.viewmodels.MapViewModel
import fr.sdis83.remocra.mobile.viewmodels.TourneeViewModel
import java.util.UUID

@Composable
fun TourneeScreen(navController: NavController, idTournee: UUID, mapViewModel: MapViewModel) {
    val context = LocalContext.current
    val tourneeViewModel = TourneeViewModel(context.applicationContext as Application, idTournee)
    val hydrantList by tourneeViewModel.hydrantList.observeAsState()
    val tourneeData by tourneeViewModel.tourneeData.observeAsState()
    val tourneesData by tourneeViewModel.tourneesData.observeAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        Row(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                ) {
                    Button(onClick = { navController.popBackStack() }) {
                        Text(
                            text = "Retour",
                        )
                    }
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = "Liste des points d'eaux",
                        fontSize = MaterialTheme.typography.headlineLarge.fontSize,
                        fontWeight = FontWeight.Bold,
                    )
                }
                if (!tourneesData.isNullOrEmpty()) {
                    LazyRow {
                        items(tourneesData!!) { tourneeItem ->
                            Button(
                                onClick = {
                                    navController.navigate(
                                        Screens.TourneeHydrants.route
                                            .replace(
                                                oldValue = "{idTournee}",
                                                newValue = tourneeItem.tournee.idTournee.toString(),
                                            ),
                                    ) {
                                        popUpTo(Screens.TourneeHydrants.route) {
                                            inclusive = true
                                        }
                                    }

                                    zoomSurTournee(tourneeItem.tournee, mapViewModel)
                                },
                                border = BorderStroke(color = tourneeItem.tournee.getColor(), width = 4.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (tourneeItem.tournee.idTournee == idTournee) tourneeItem.tournee.getColor() else Color.Transparent,
                                    contentColor = if (tourneeItem.tournee.idTournee == idTournee) Color.White else tourneeItem.tournee.getColor(),
                                ),
                            ) {
                                Text(
                                    text = tourneeItem.tournee.nom,
                                )
                            }
                            Spacer(Modifier.width(16.dp))
                        }
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                ) {
                    Text(
                        text = tourneeData?.nom ?: "",
                        fontSize = MaterialTheme.typography.headlineLarge.fontSize,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(Modifier.width(16.dp))
                }
                if (!hydrantList.isNullOrEmpty()) {
                    Row {
                        LazyColumn {
                            items(hydrantList!!) { hydrantItem ->
                                Row(
                                    Modifier
                                        .padding(8.dp)
                                        .fillMaxWidth()
                                        .clickable {
                                            navController.navigate(
                                                Screens.Hydrant.route
                                                    .replace(
                                                        oldValue = "{idHydrant}",
                                                        newValue = hydrantItem.hydrant.idHydrant.toString(),
                                                    )
                                                    .replace(
                                                        oldValue = "{idTournee}",
                                                        newValue = idTournee.toString(),
                                                    ),
                                            ) {
                                                popUpTo(Screens.TourneeHydrants.route)
                                            }
                                        },
                                ) {
                                    Box(
                                        modifier =
                                        Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0xDDE9F3FF))
                                            .padding(16.dp)
                                            .fillMaxWidth(),
                                    ) {
                                        Column {
                                            Row(modifier = Modifier.fillMaxWidth()) {
                                                Text(text = hydrantItem.hydrant.numero ?: "N/A")
                                                Spacer(modifier = Modifier.width(16.dp))
                                                Text(text = hydrantItem.statut ?: "À faire")
                                            }
                                            Row(modifier = Modifier.fillMaxWidth()) {
                                                Text(
                                                    text = hydrantItem.hydrantNature.nom,
                                                )
                                            }
                                            Row(modifier = Modifier.fillMaxWidth()) {
                                                Text(
                                                    text = hydrantItem.hydrant.dispoTerrestre.toString(),
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
