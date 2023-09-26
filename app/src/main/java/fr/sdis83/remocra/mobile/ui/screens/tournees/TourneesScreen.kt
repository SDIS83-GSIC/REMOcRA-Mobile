package fr.sdis83.remocra.mobile.ui.screens.tournees

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import fr.sdis83.remocra.mobile.navigation.Screens
import fr.sdis83.remocra.mobile.viewmodels.MapViewModel
import fr.sdis83.remocra.mobile.viewmodels.TourneesViewModel
import kotlin.math.roundToInt

@Composable
fun TourneesScreen(navController: NavController, mapViewModel: MapViewModel) {
    val context = LocalContext.current
    val tourneesViewModel = TourneesViewModel(context.applicationContext as Application)
    val tourneeList by tourneesViewModel.tourneeList.observeAsState()

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
                    Text(
                        text = "Liste des tournées",
                        fontSize = MaterialTheme.typography.headlineLarge.fontSize,
                        fontWeight = FontWeight.Bold,
                    )
                }
                if (!tourneeList.isNullOrEmpty()) {
                    Row {
                        LazyColumn {
                            items(tourneeList!!) { tourneeItem ->
                                Row(
                                    Modifier
                                        .padding(8.dp)
                                        .fillMaxWidth()
                                        .clickable {
                                            zoomSurTournee(tourneeItem.tournee, mapViewModel)

                                            navController.navigate(
                                                Screens.TourneeHydrants.route
                                                    .replace(
                                                        oldValue = "{idTournee}",
                                                        newValue = tourneeItem.tournee.idTournee.toString(),
                                                    ),
                                            )
                                        },
                                ) {
                                    Box(
                                        modifier =
                                        Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                tourneeItem.tournee
                                                    .getColor()
                                                    .copy(alpha = 0.5f),
                                            )
                                            .padding(16.dp)
                                            .fillMaxWidth(),
                                    ) {
                                        Column {
                                            Row {
                                                Text(
                                                    text = tourneeItem.tournee.nom,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 18.sp,
                                                )
                                            }
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Column(Modifier.weight(4f)) {
                                                    Text(
                                                        text = "${tourneeItem.tournee.hydrantCount} point(s) d'eau",
                                                        fontWeight = FontWeight.Bold,
                                                    )
                                                }
                                                Column(Modifier.weight(1f)) {
                                                    Text(
                                                        text = "${(tourneeItem.progression * 100).roundToInt()}%",
                                                        fontWeight = FontWeight.Bold,
                                                    )
                                                }
                                                Column(Modifier.weight(5f)) {
                                                    LinearProgressIndicator(
                                                        progress = tourneeItem.progression,
                                                        modifier = Modifier
                                                            .height(12.dp)
                                                            .clip(RoundedCornerShape(16.dp)),
                                                        color = Color(0.1f, 0.33f, 1f, 1f),
                                                        trackColor = Color(0.5f, 0.5f, 0.5f, 0.25f),
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
