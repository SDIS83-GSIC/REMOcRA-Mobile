package fr.sdis83.remocra.mobile.workers

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.okta.authfoundationbootstrap.CredentialBootstrap
import fr.sdis83.remocra.mobile.authn.SessionManager
import fr.sdis83.remocra.mobile.services.ReferentielService
import fr.sdis83.remocra.mobile.utils.GlobalConstants
import fr.sdis83.remocra.mobile.utils.dateAfterNow
import kotlinx.coroutines.runBlocking

abstract class WorkerRemocra(
    context: Context,
    workerParams: WorkerParameters,
) : Worker(context, workerParams) {

    abstract fun doExecute(): Result

    private fun isModeDeconnecte(sessionManager: SessionManager): Boolean {
        val dateDeconnexion = sessionManager.getDateDeconnexion()
        return dateDeconnexion != null && dateAfterNow(dateDeconnexion)
    }

    private fun logModeDeconnecte() {
        Log.w("WorkerRemocra", "Mode déconnecté, on ne fait pas l'appel au serveur ET on ne redirige pas vers le login")
    }

    /**
     * Gère la redirection vers MainActivity.
     */
    private fun sendLogoutBroadcast() {
        val intent = Intent(GlobalConstants.ACTION_LOGOUT)
        applicationContext.sendBroadcast(intent)
    }

    override fun doWork(): Result {
        val retrofitBuilder = ReferentielService.getRetroFitInstance(applicationContext)
        val sessionManager = SessionManager(applicationContext)

        if (sessionManager.getAuthToken().isNullOrEmpty()) {
            if (isModeDeconnecte(sessionManager)) {
                logModeDeconnecte()
                return Result.success()
            }
            sendLogoutBroadcast()
            return Result.failure()
        }

        var result: Result? = null
        runBlocking {
            if (isModeDeconnecte(sessionManager)) {
                logModeDeconnecte()
                result = Result.success()
                return@runBlocking
            }

            // Vérifie si le token Okta est encore valide
            val validAccessToken = CredentialBootstrap.defaultCredential().getValidAccessToken()
            if (validAccessToken == null || retrofitBuilder.checkConnexion().execute().code().let { it == 401 || it == 403 }) {
                CredentialBootstrap.defaultCredential().delete()
                sessionManager.invalidateAuthToken()
                result = Result.failure()
                sendLogoutBroadcast()
            }
        }

        if (result != null) {
            return result!!
        }

        return doExecute()
    }
}
