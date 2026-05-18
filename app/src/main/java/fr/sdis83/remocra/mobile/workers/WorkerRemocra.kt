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

        var result: Result? = null
        runBlocking {
            // Si je suis en mode déconnecté, mais que le serveur est joignable, alors je vérifie que le token est valide et
            // donc je redirige vers la page d'accueil au besoin
            try {
                val connexion = retrofitBuilder.checkConnexion().execute().code()

                if (connexion.let { it == 401 || it == 403 || it == 200 }) {
                    val validAccessToken =
                        CredentialBootstrap.defaultCredential().getValidAccessToken()
                    if (validAccessToken == null || connexion.let { it == 401 || it == 403 }) {
                        CredentialBootstrap.defaultCredential().delete()
                        result = Result.failure()
                        sendLogoutBroadcast()
                    }
                }
            } catch (e: Throwable) {
                // si mode déconnecté, alors on renvoie un success
                if (isModeDeconnecte(sessionManager)) {
                    logModeDeconnecte()
                    result = Result.success()
                    return@runBlocking
                } else {
                    Log.e("WorkerRemocra", "Error executing work: " + e.message, e)
                    result = Result.failure()
                }
            }
        }
        if (result != null) {
            return result!!
        }

        return doExecute()
    }
}
