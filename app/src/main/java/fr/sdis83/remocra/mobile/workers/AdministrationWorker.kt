package fr.sdis83.remocra.mobile.workers

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import fr.sdis83.remocra.mobile.authn.KeycloakManager
import fr.sdis83.remocra.mobile.authn.SessionManager
import fr.sdis83.remocra.mobile.services.AuthService

class AdministrationWorker constructor(
    context: Context,
    workerParams: WorkerParameters,
) : Worker(context, workerParams) {

    companion object {
        private const val TAG: String = "AdministrationWorker"
    }

    override fun doWork(): Result {
        val retrofitBuilder = AuthService.rebuildUrl(applicationContext)

        val sessionManager = SessionManager(applicationContext)
        val keycloakManager = KeycloakManager(applicationContext)

        val connexionTestResponse = retrofitBuilder.checkUrl().execute()

        if (!connexionTestResponse.isSuccessful) {
            keycloakManager.invalidateKeycloakConfig()
            return Result.failure()
        }

        sessionManager.saveDateDeconnexion(connexionTestResponse.body()?.dateProchaineConnexion)

        val response = connexionTestResponse.body()?.keycloakConfig
        if (response != null) {
            keycloakManager.initKeycloakConf(response, applicationContext)
        }

        return Result.success()
    }
}
