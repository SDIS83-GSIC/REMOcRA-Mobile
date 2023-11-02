package fr.sdis83.remocra.mobile.ui.screens.tournees

import android.app.Application
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.navigation.NavController
import fr.sdis83.remocra.mobile.R
import fr.sdis83.remocra.mobile.database.HydrantVisite
import fr.sdis83.remocra.mobile.navigation.Screens
import fr.sdis83.remocra.mobile.utils.pxToDp
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
                        .padding(10.pxToDp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Button(onClick = { navController.popBackStack() }) {
                        Text(
                            text = "Retour",
                        )
                    }
                    Spacer(Modifier.width(10.pxToDp))
                    Text(
                        text = "Liste des points d'eau",
                        fontSize = 5.em,
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
                                border = BorderStroke(color = tourneeItem.tournee.getColor(), width = 4.pxToDp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (tourneeItem.tournee.idTournee == idTournee) tourneeItem.tournee.getColor() else Color.Transparent,
                                    contentColor = if (tourneeItem.tournee.idTournee == idTournee) Color.White else tourneeItem.tournee.getColor(),
                                ),
                            ) {
                                Text(
                                    text = tourneeItem.tournee.nom,
                                    fontSize = 2.em,
                                )
                            }
                            Spacer(Modifier.width(16.pxToDp))
                        }
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.pxToDp),
                ) {
                    Text(
                        text = tourneeData?.nom ?: "",
                        fontSize = 4.em,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(Modifier.width(16.pxToDp))
                }
                if (!hydrantList.isNullOrEmpty()) {
                    Row {
                        LazyColumn {
                            items(
                                hydrantList!!.sortedWith(
                                    compareBy {
                                        if (it.statut == HydrantVisite.HydrantVisiteStatut.TERMINE.toString()) {
                                            it.statut
                                        } else {
                                            null
                                        }
                                    },
                                ),
                            ) { hydrantItem ->
                                val estTerminee = hydrantItem.statut == HydrantVisite.HydrantVisiteStatut.TERMINE.toString()
                                Row(
                                    Modifier
                                        .padding(8.pxToDp)
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
                                            .clip(RoundedCornerShape(8.pxToDp))
                                            .background(Color(if (estTerminee) 0xDDE9F2DE else 0xDDE9F3FF))
                                            .padding(16.pxToDp)
                                            .fillMaxWidth(),
                                    ) {
                                        Column {
                                            Row(modifier = Modifier.fillMaxWidth()) {
                                                Text(text = hydrantItem.hydrant.numero ?: "N/A")
                                                Spacer(modifier = Modifier.width(16.pxToDp))
                                                Text(
                                                    text = hydrantItem.statut ?: "À faire",
                                                    color = if (estTerminee) Color(0xDD31861E) else Color.Black,
                                                    fontWeight = if (estTerminee) FontWeight.Bold else FontWeight.Normal,
                                                )
                                                Spacer(modifier = Modifier.width(16.pxToDp))
                                                if (estTerminee) {
                                                    Image(
                                                        painterResource(id = R.drawable.baseline_check_24),
                                                        contentDescription = "check",
                                                    )
                                                }
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
