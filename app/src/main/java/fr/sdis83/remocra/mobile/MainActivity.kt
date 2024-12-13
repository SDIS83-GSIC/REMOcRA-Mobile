package fr.sdis83.remocra.mobile

import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.compose.rememberNavController
import fr.sdis83.remocra.mobile.navigation.NavGraph
import fr.sdis83.remocra.mobile.ui.components.MapView
import fr.sdis83.remocra.mobile.ui.layout.Layout
import fr.sdis83.remocra.mobile.ui.theme.REMOcRAMobileTheme
import fr.sdis83.remocra.mobile.utils.getVersionName
import fr.sdis83.remocra.mobile.utils.pxToDp
import fr.sdis83.remocra.mobile.viewmodels.LoginViewModel
import fr.sdis83.remocra.mobile.viewmodels.MapViewModel
import fr.sdis83.remocra.mobile.viewmodels.ParametreViewModel

data class MapViewState(
    val showMapView: Boolean = true,
    val isFullscreen: Boolean = false,
)

class MainActivity : ComponentActivity() {

    private val loginViewModel: LoginViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val mapViewModel = MapViewModel(applicationContext)
        val parametreViewModel = ParametreViewModel(applicationContext as Application)

        // On affiche la barre de navigation du périphérique
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.show(WindowInsetsCompat.Type.systemBars())
        windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        val preferences = applicationContext.getSharedPreferences(
            applicationContext.getString(R.string.app_name),
            Context.MODE_PRIVATE,
        )

        val dateProchaineDeconnexion = preferences.getString(
            applicationContext.resources
                .getString(R.string.preference_date_prochaine_deconnexion),
            null,
        )
        setContent {
            REMOcRAMobileTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    val navController = rememberNavController()
                    val mapViewState = remember { mutableStateOf(MapViewState()) }
                    parametreViewModel.paramAffichageIndispo.observe(this) {
                        mapViewModel.setAffichageIndispo(it.toBoolean())
                    }
                    parametreViewModel.paramAffichageSymbolesNormalises.observe(this) {
                        mapViewModel.setAffichageSymbolesNormalises(it.toBoolean())
                    }
                    Layout(
                        navController,
                        getVersionName(applicationContext),
                        modeDeconnecte = !dateProchaineDeconnexion.isNullOrBlank(),
                        logout = {
                            loginViewModel.sessionManager.invalidateAuthToken()
                            startActivity(
                                Intent(
                                    this@MainActivity,
                                    LoginActivity::class.java,
                                ),
                            )
                        },
                    ) {
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
                                    MapView(mapViewModel, mapViewState, navController)
                                }
                            }
                            Column(
                                modifier =
                                if (mapViewState.value.isFullscreen) {
                                    Modifier
                                        .padding(8.pxToDp)
                                        .fillMaxHeight()
                                        .width(0.pxToDp)
                                        .animateContentSize(
                                            animationSpec = tween(
                                                durationMillis = 500,
                                                easing = LinearOutSlowInEasing,
                                            ),
                                        )
                                } else {
                                    Modifier
                                        .padding(8.pxToDp)
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
