package fr.sdis83.remocra.mobile.viewmodels

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.okta.authfoundation.client.OidcClientResult
import com.okta.authfoundationbootstrap.CredentialBootstrap
import com.okta.webauthenticationui.WebAuthenticationClient.Companion.createWebAuthenticationClient
import fr.sdis83.remocra.mobile.authn.KeycloakManager
import fr.sdis83.remocra.mobile.authn.SessionManager
import fr.sdis83.remocra.mobile.utils.GlobalConstants
import fr.sdis83.remocra.mobile.workers.ReferentielWorker
import kotlinx.coroutines.launch

class AuthentViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "AuthentViewModel"

        enum class JobStatus {
            WAITING,
            LOADING,
            ERROR,
        }
    }
    val sessionManager = SessionManager(application)
    val keycloakManager = KeycloakManager(application)

    var referentielStatus = mutableStateOf(JobStatus.WAITING)

    var info = mutableStateOf("")
        private set

    val goToMainActivity = MutableLiveData(false)

    fun login(context: Context) {
        Log.i(TAG, "Tentative de connexion")
        viewModelScope.launch {
            val result = CredentialBootstrap.oidcClient.createWebAuthenticationClient().login(
                context = context,
                redirectUrl = GlobalConstants.KEYCLOAK_LOGIN,
            )

            Log.i(TAG, "$result")
            when (result) {
                is OidcClientResult.Error -> {
                    info.value = "Impossible de se connecter"
                    Log.i(TAG, "Erreur de connexion : ${result.exception.message} - ${result.exception.cause}")
                    sessionManager.invalidateAuthToken()
                }

                is OidcClientResult.Success -> {
                    Log.i(TAG, "Connexion réussie")
                    val credential = CredentialBootstrap.defaultCredential()
                    credential.storeToken(token = result.result)

                    val referentielWorker = OneTimeWorkRequestBuilder<ReferentielWorker>().build()
                    sessionManager.saveAuthToken(credential)
                    // Calculer et sauvegarder la date de prochaine déconnexion
                    sessionManager.calculateAndSaveDateDeconnexion()

                    WorkManager.getInstance(getApplication()).let { workManager ->
                        workManager.beginWith(referentielWorker).enqueue()
                        workManager.getWorkInfoByIdLiveData(referentielWorker.id).observeForever {
                            when (it.state) {
                                WorkInfo.State.RUNNING -> {
                                    info.value = "Récupération du référentiel"
                                    referentielStatus.value = JobStatus.LOADING
                                }

                                WorkInfo.State.SUCCEEDED -> {
                                    info.value = ""
                                    referentielStatus.value = JobStatus.WAITING
                                    goToMainActivity.postValue(true)
                                }
                                WorkInfo.State.FAILED -> {
                                    info.value = if (!it.outputData.getString("VERSION_INCOMPATIBLE").isNullOrEmpty()) {
                                        "La version de la tablette n'est pas à jour, veuillez contacter votre SDIS."
                                    } else {
                                        "Erreur de connexion"
                                    }
                                    sessionManager.invalidateAuthToken()
                                    logoutOfBrowser(context)
                                    referentielStatus.value = JobStatus.ERROR
                                }
                                else -> {
                                    referentielStatus.value = JobStatus.WAITING
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    fun logoutOfBrowser(context: Context) {
        viewModelScope.launch {
            val result = CredentialBootstrap.oidcClient.createWebAuthenticationClient().logoutOfBrowser(
                context = context,
                redirectUrl = GlobalConstants.KEYCLOAK_LOGOUT,
                CredentialBootstrap.defaultCredential().token?.idToken ?: "",
            )
            when (result) {
                is OidcClientResult.Error -> {
                    info.value = "Impossible de se connecter"
                }
                is OidcClientResult.Success -> {
                    CredentialBootstrap.defaultCredential().delete()
                    sessionManager.invalidateAuthToken()
                    goToMainActivity.postValue(false)
                }
            }
        }
    }
}
