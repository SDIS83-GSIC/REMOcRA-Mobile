package fr.sdis83.remocra.mobile.workers

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import fr.sdis83.remocra.mobile.LoginActivity
import fr.sdis83.remocra.mobile.authn.SessionManager
import fr.sdis83.remocra.mobile.services.AuthService
import fr.sdis83.remocra.mobile.utils.dateAfterNow
import java.lang.Exception

abstract class WorkerRemocra constructor(
    context: Context,
    workerParams: WorkerParameters,
) : Worker(context, workerParams) {

    abstract fun doExecute(): Result

    final override fun doWork(): Result {
        val retrofitBuilder = AuthService.getRetroFitInstance(applicationContext)
        val sessionManager = SessionManager(applicationContext)

        if (sessionManager.getAuthToken().isNullOrEmpty()) {
            return Result.failure()
        }

        try {
            val tokenResponse = retrofitBuilder.checkToken().execute()
            if (!tokenResponse.isSuccessful) {
                val intent = Intent(
                    applicationContext,
                    LoginActivity::class.java,
                )
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                ContextCompat.startActivity(
                    applicationContext,
                    intent,
                    null,
                )

                return Result.failure()
            }
        } catch (e: Exception) {
            if (sessionManager.getDateDeconnexion() != null &&
                dateAfterNow(sessionManager.getDateDeconnexion()!!)
            ) {
                Log.w("WorkerRemocra", "Mode déconnecté, on ne fait pas l'appel au serveur ET on ne redirige pas vers le login")
                return Result.failure()
            }
        }

        return doExecute()
    }
}
