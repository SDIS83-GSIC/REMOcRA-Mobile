package fr.sdis83.remocra.mobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import fr.sdis83.remocra.mobile.navigation.NavGraph
import fr.sdis83.remocra.mobile.ui.components.MapView
import fr.sdis83.remocra.mobile.ui.layout.Layout
import fr.sdis83.remocra.mobile.ui.theme.REMOcRAMobileTheme
import fr.sdis83.remocra.mobile.viewmodels.MapViewModel

data class MapViewState(
    val showMapView: Boolean = true,
    val isFullscreen: Boolean = false,
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val mapViewModel = MapViewModel(applicationContext)

        setContent {
            REMOcRAMobileTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    val navController = rememberNavController()
                    val mapViewState = remember { mutableStateOf(MapViewState()) }
                    Layout(navController) {
                        Row(modifier = Modifier.fillMaxSize()) {
                            if (mapViewState.value.showMapView) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .weight(1f)
                                        .animateContentSize(
                                            animationSpec = tween(
                                                durationMillis = 100,
                                                easing = LinearOutSlowInEasing,
                                            ),
                                        ),
                                ) {
                                    MapView(mapViewModel, mapViewState)
                                }
                            }
                            Column(
                                modifier =
                                if (mapViewState.value.isFullscreen) {
                                    Modifier
                                        .fillMaxHeight()
                                        .width(0.dp)
                                        .animateContentSize(
                                            animationSpec = tween(
                                                durationMillis = 500,
                                                easing = LinearOutSlowInEasing,
                                            ),
                                        )
                                } else {
                                    Modifier
                                        .fillMaxHeight()
                                        .weight(1f)
                                        .animateContentSize(
                                            animationSpec = tween(
                                                durationMillis = 500,
                                                easing = LinearOutSlowInEasing,
                                            ),
                                        )
                                },
                            ) {
                                NavGraph(
                                    navController = navController,
                                    mapViewModel = mapViewModel,
                                    mapViewState = mapViewState,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
