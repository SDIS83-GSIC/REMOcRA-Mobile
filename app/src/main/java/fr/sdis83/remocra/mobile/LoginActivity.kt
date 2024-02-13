package fr.sdis83.remocra.mobile

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import fr.sdis83.remocra.mobile.ui.screens.login.LoginScreen
import fr.sdis83.remocra.mobile.ui.theme.REMOcRAMobileTheme
import fr.sdis83.remocra.mobile.utils.dateAfterNow
import fr.sdis83.remocra.mobile.viewmodels.AdministrationViewModel
import fr.sdis83.remocra.mobile.viewmodels.LoginViewModel
import fr.sdis83.remocra.mobile.viewmodels.SplashViewModel

class LoginActivity : ComponentActivity() {

    private val loginViewModel: LoginViewModel by viewModels()
    private val splashViewModel: SplashViewModel by viewModels()
    private val administrationViewModel: AdministrationViewModel by viewModels()

    companion object {
        private const val TAG = "LoginActivity"
    }

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        // A l'ouverture de l'appli, on demande à l'utilisateur d'autoriser l'accès aux fichiers
        if (!Environment.isExternalStorageManager()) {
            val getpermission = Intent()
            getpermission.action = Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
            startActivity(getpermission)
        }

        val preferences = applicationContext.getSharedPreferences(
            applicationContext.getString(R.string.app_name),
            Context.MODE_PRIVATE,
        )

        val mdm = preferences.getString(applicationContext.resources.getString(R.string.preference_mdm), "false").toBoolean()

        val dateProchaineDeconnexion = preferences.getString(
            applicationContext.resources
                .getString(R.string.preference_date_prochaine_deconnexion),
            null,
        )

        splashScreen.setKeepOnScreenCondition { splashViewModel.isLoading.value }

        splashViewModel.goToMainActivity.observe(this) {
            if (it && loginViewModel.goToMainActivity.value != true) {
                startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                finish()
            }
        }

        loginViewModel.goToMainActivity.observe(this) {
            if (it) {
                startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                finish()
            }
        }

        administrationViewModel.administrationScreen.observe(this) {
            if (it && !mdm) {
                startActivity(Intent(this@LoginActivity, AdministrationActivity::class.java))
                finish()
            }
        }
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
                            loginViewModel.goToMainActivity.postValue(true)
                        } else {
                            LoginScreen(
                                viewModel = loginViewModel,
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
