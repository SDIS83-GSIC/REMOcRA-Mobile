package fr.sdis83.remocra.mobile

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
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

        splashScreen.setKeepOnScreenCondition { splashViewModel.isLoading.value }

        splashViewModel.goToMainActivity.observe(this) {
            if (it) {
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
            if (it) {
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
                        LoginScreen(viewModel = loginViewModel, administrationViewModel, application)
                    }
                }
            }
        }
    }
}
