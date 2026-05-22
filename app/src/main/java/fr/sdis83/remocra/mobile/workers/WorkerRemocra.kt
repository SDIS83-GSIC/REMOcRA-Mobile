package fr.sdis83.remocra.mobile.workers

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
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

    companion object {
        const val OUTPUT_ERROR_MESSAGE = "OUTPUT_ERROR_MESSAGE"
        const val OUTPUT_ERROR_TYPE = "OUTPUT_ERROR_TYPE"
    }

    abstract fun doExecute(): Result

    protected open val workerTag: String = this::class.java.simpleName

    protected fun failureWithError(message: String, throwable: Throwable? = null): Result {
        val safeMessage = message.ifBlank { "Erreur inconnue" }
        val contextualMessage = if (safeMessage.startsWith("[$workerTag]")) {
            safeMessage
        } else {
            "[$workerTag] $safeMessage"
        }
        // Log le message complet pour le debug
        Log.e(workerTag, contextualMessage)

        val outputData = workDataOf(
            OUTPUT_ERROR_MESSAGE to contextualMessage,
            OUTPUT_ERROR_TYPE to (throwable?.javaClass?.simpleName ?: "Unknown"),
        )
        return Result.failure(outputData)
    }

    protected fun failureWithError(throwable: Throwable, defaultMessage: String): Result {
        val detailedMessage = throwable.message?.takeIf { it.isNotBlank() } ?: defaultMessage
        return failureWithError(detailedMessage, throwable)
    }

    private fun isModeDeconnecte(sessionManager: SessionManager): Boolean {
        val dateDeconnexion = sessionManager.getDateDeconnexion()
        return dateDeconnexion != null && dateAfterNow(dateDeconnexion)
    }

    private fun logModeDeconnecte() {
        Log.w(workerTag, "Mode déconnecté, on ne fait pas l'appel au serveur ET on ne redirige pas vers le login")
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
                        result = failureWithError("Session invalide, reconnexion requise")
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
                    result = failureWithError("Session expirée, reconnexion requise")
                }
            }
        }
        result?.let { return it }

        return try {
            doExecute()
        } catch (e: Throwable) {
            Log.e(workerTag, "Erreur inattendue lors de l'exécution du worker", e)
            failureWithError(e, "Erreur inattendue lors de l'exécution du worker")
        }
    }
}
