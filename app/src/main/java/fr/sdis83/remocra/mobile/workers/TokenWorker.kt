package fr.sdis83.remocra.mobile.workers

import android.content.Context
import android.widget.Toast
import androidx.work.Worker
import androidx.work.WorkerParameters
import fr.sdis83.remocra.mobile.authn.SessionManager
import fr.sdis83.remocra.mobile.services.AuthService

class TokenWorker constructor(
    context: Context,
    workerParams: WorkerParameters,
) : Worker(context, workerParams) {

    companion object {
        private const val TAG: String = "TokenWorker"
    }

    override fun doWork(): Result {
        val retrofitBuilder = AuthService.getRetroFitInstance(applicationContext)
        val sessionManager = SessionManager(applicationContext)

        if (sessionManager.getAuthToken().isNullOrEmpty()) {
            return Result.failure()
        }

        val tokenResponse = retrofitBuilder.checkToken().execute()

        if (!tokenResponse.isSuccessful) {
            Toast.makeText(applicationContext, "Jeton d'authenfication expiré", Toast.LENGTH_LONG)
                .show()
            sessionManager.invalidateAuthToken()
            return Result.failure()
        }

        return Result.success()
    }
}
