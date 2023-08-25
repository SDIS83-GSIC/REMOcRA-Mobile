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
import fr.sdis83.remocra.mobile.ui.screens.administration.AdministrationScreen
import fr.sdis83.remocra.mobile.ui.theme.REMOcRAMobileTheme
import fr.sdis83.remocra.mobile.viewmodels.AdministrationViewModel

class AdministrationActivity : ComponentActivity() {

    private val administrationViewModel: AdministrationViewModel by viewModels()

    companion object {
        private const val TAG = "AdministrationActivity"
    }

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        administrationViewModel.administrationScreen.observe(this) {
            if (it) {
                startActivity(Intent(this@AdministrationActivity, LoginActivity::class.java))
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
                        AdministrationScreen(administrationViewModel)
                    }
                }
            }
        }
    }
}
