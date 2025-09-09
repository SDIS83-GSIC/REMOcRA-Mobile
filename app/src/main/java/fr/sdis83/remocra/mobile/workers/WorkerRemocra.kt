package fr.sdis83.remocra.mobile.workers

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.okta.authfoundationbootstrap.CredentialBootstrap
import fr.sdis83.remocra.mobile.MainActivity
import fr.sdis83.remocra.mobile.authn.SessionManager
import fr.sdis83.remocra.mobile.services.ReferentielService
import fr.sdis83.remocra.mobile.utils.dateAfterNow
import kotlinx.coroutines.runBlocking

abstract class WorkerRemocra(
    context: Context,
    workerParams: WorkerParameters,
) : Worker(context, workerParams) {

    abstract fun doExecute(): Result

    /**
     * Gère la redirection vers MainActivity.
     */
    fun redirectMainActivity() {
        val intent = Intent(
            applicationContext,
            MainActivity::class.java,
        )
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        ContextCompat.startActivity(
            applicationContext,
            intent,
            null,
        )
    }

    override fun doWork(): Result {
        val retrofitBuilder = ReferentielService.getRetroFitInstance(applicationContext)
        val sessionManager = SessionManager(applicationContext)

        if (sessionManager.getAuthToken().isNullOrEmpty()) {
            return Result.failure()
        }

        var result: Result? = null
        runBlocking {
            if (sessionManager.getDateDeconnexion() != null &&
                dateAfterNow(sessionManager.getDateDeconnexion()!!)
            ) {
                Log.w("WorkerRemocra", "Mode déconnecté, on ne fait pas l'appel au serveur ET on ne redirige pas vers le login")
                result = Result.success()
                return@runBlocking
            }

            // Vérifie si le token Okta est encore valide
            val validAccessToken = CredentialBootstrap.defaultCredential().getValidAccessToken()
            if (validAccessToken == null || retrofitBuilder.checkConnexion().execute().code().let { it == 401 || it == 403 }) {
                CredentialBootstrap.defaultCredential().delete()
                sessionManager.invalidateAuthToken()
                result = Result.failure()
                redirectMainActivity()
            }
        }

        if (result != null) {
            return result!!
        }

        return doExecute()
    }
}
