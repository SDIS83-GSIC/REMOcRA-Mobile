package fr.sdis83.remocra.mobile

import android.annotation.SuppressLint
import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.RestrictionsManager
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.widget.Toast
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.compose.rememberNavController
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import fr.sdis83.remocra.mobile.navigation.NavGraph
import fr.sdis83.remocra.mobile.services.AuthService
import fr.sdis83.remocra.mobile.ui.components.MapView
import fr.sdis83.remocra.mobile.ui.layout.Layout
import fr.sdis83.remocra.mobile.ui.screens.login.LoginScreen
import fr.sdis83.remocra.mobile.ui.theme.REMOcRAMobileTheme
import fr.sdis83.remocra.mobile.utils.dateAfterNow
import fr.sdis83.remocra.mobile.utils.getVersionName
import fr.sdis83.remocra.mobile.utils.pxToDp
import fr.sdis83.remocra.mobile.viewmodels.AdministrationViewModel
import fr.sdis83.remocra.mobile.viewmodels.AuthentViewModel
import fr.sdis83.remocra.mobile.viewmodels.DroitViewModel
import fr.sdis83.remocra.mobile.viewmodels.MapViewModel
import fr.sdis83.remocra.mobile.viewmodels.ParametreViewModel
import fr.sdis83.remocra.mobile.viewmodels.SplashViewModel
import fr.sdis83.remocra.mobile.workers.AdministrationWorker

data class MapViewState(
    val showMapView: Boolean = true,
    val isFullscreen: Boolean = false,
)

class MainActivity : ComponentActivity() {

    private val authentViewModel: AuthentViewModel by viewModels()
    private val splashViewModel: SplashViewModel by viewModels()
    private val administrationViewModel: AdministrationViewModel by viewModels()

    companion object {
        private const val TAG = "MainActivity"
    }

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        if (authentViewModel.keycloakManager.getKeycloakUrl() != null) {
            authentViewModel.keycloakManager.initKeycloakConf(
                AuthService.KeycloakConfig(
                    url = authentViewModel.keycloakManager.getKeycloakUrl()!!,
                    clientId = authentViewModel.keycloakManager.getKeycloakClientId()!!,
                ),
                context = applicationContext,
            )
        }

        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        val preferences: SharedPreferences = applicationContext.getSharedPreferences(
            applicationContext.getString(R.string.app_name),
            MODE_PRIVATE,
        )
        // On va chercher les restrictions
        val myRestrictionsMgr =
            this.getSystemService(RESTRICTIONS_SERVICE) as RestrictionsManager

        val appRestrictions: Bundle = myRestrictionsMgr.applicationRestrictions

        if (appRestrictions.containsKey("preference_url_api")) {
            preferences.edit()
                .putString(getString(R.string.url_api), appRestrictions.getString("preference_url_api"))
                .apply()

            preferences.edit()
                .putBoolean(getString(R.string.preference_mdm), appRestrictions.getBoolean("preference_mdm"))
                .apply()
        }

        splashScreen.setKeepOnScreenCondition { splashViewModel.isLoading.value }
    }

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onResume() {
        super.onResume()

        val preferences: SharedPreferences = applicationContext.getSharedPreferences(
            applicationContext.getString(R.string.app_name),
            MODE_PRIVATE,
        )

        val mdm = preferences.getBoolean(
            applicationContext.resources
                .getString(R.string.preference_mdm),
            false,
        )
        val dateProchaineDeconnexion = preferences.getString(
            applicationContext.resources
                .getString(R.string.preference_date_prochaine_deconnexion),
            null,
        )

        val myRestrictionsMgr =
            this.getSystemService(RESTRICTIONS_SERVICE) as RestrictionsManager
        val appRestrictions: Bundle = myRestrictionsMgr.applicationRestrictions

        // Si elles ont changé, on les recharge
        val restrictionsFilter = IntentFilter(Intent.ACTION_APPLICATION_RESTRICTIONS_CHANGED)

        val restrictionsReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                preferences.edit()
                    .putString(
                        getString(R.string.url_api),
                        appRestrictions.getString("preference_url_api"),
                    )
                    .apply()

                preferences.edit()
                    .putBoolean(
                        getString(R.string.preference_mdm),
                        appRestrictions.getBoolean("preference_mdm"),
                    )
                    .apply()
            }
        }
        registerReceiver(restrictionsReceiver, restrictionsFilter)

        // A l'ouverture de l'appli, on demande à l'utilisateur d'autoriser l'accès aux fichiers
        if (!Environment.isExternalStorageManager()) {
            val getpermission = Intent()
            getpermission.action = Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
            startActivity(getpermission)
        }

        splashViewModel.goToMainActivity.observe(this) {
            buildScreen(it, dateProchaineDeconnexion, mdm)
        }

        authentViewModel.goToMainActivity.observe(this) {
            buildScreen(it, dateProchaineDeconnexion, mdm)
        }

        administrationViewModel.administrationScreen.observe(this) {
            if (it && !mdm) {
                startActivity(Intent(this@MainActivity, AdministrationActivity::class.java))
                finish()
            }
        }
    }
    private fun logout(context: Context) {
        authentViewModel.logoutOfBrowser(context)
        authentViewModel.sessionManager.invalidateAuthToken()
    }

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    private fun buildScreen(goToMain: Boolean, dateProchaineDeconnexion: String?, mdm: Boolean) {
        if (goToMain) {
            val mapViewModel = MapViewModel(application)
            val droitViewModel = DroitViewModel(application)
            val parametreViewModel = ParametreViewModel(applicationContext as Application)

            // On affiche la barre de navigation du périphérique
            val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
            windowInsetsController.show(WindowInsetsCompat.Type.systemBars())
            windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

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
                            logout = { context ->
                                // Si on est pas en mode déconnecté
                                if (dateProchaineDeconnexion.isNullOrBlank()) {
                                    logout(context)
                                    startActivity(
                                        Intent(
                                            this@MainActivity,
                                            MainActivity::class.java,
                                        ),
                                    )
                                } else {
                                    val administrationWorker = OneTimeWorkRequestBuilder<AdministrationWorker>().build()

                                    WorkManager.getInstance(getApplication()).let { workManager ->
                                        workManager.enqueue(administrationWorker)
                                        workManager.getWorkInfoByIdLiveData(administrationWorker.id).observeForever {
                                            when (it.state) {
                                                WorkInfo.State.RUNNING -> {
                                                    Toast.makeText(applicationContext, "Déconnexion en cours", Toast.LENGTH_SHORT).show()
                                                }

                                                WorkInfo.State.SUCCEEDED -> {
                                                    logout(context)
                                                    startActivity(
                                                        Intent(
                                                            this@MainActivity,
                                                            MainActivity::class.java,
                                                        ),
                                                    )
                                                }

                                                WorkInfo.State.FAILED -> {
                                                    Toast.makeText(
                                                        applicationContext,
                                                        "Echec de la connexion au serveur",
                                                        Toast.LENGTH_SHORT,
                                                    ).show()
                                                }

                                                else -> {}
                                            }
                                        }
                                    }
                                }
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
                                        MapView(mapViewModel, droitViewModel, mapViewState, navController)
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
        } else {
            setContent {
                REMOcRAMobileTheme {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background,
                    ) {
                        Scaffold {
                            if (dateProchaineDeconnexion != null &&
                                dateAfterNow(dateProchaineDeconnexion)
                            ) {
                                authentViewModel.goToMainActivity.postValue(true)
                            } else {
                                LoginScreen(
                                    viewModel = authentViewModel,
                                    administrationViewModel,
                                    application,
                                    mdm,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
