package fr.sdis83.remocra.mobile.workers

import android.content.Context
import android.util.Log
import androidx.work.Data
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import fr.sdis83.remocra.mobile.authn.SessionManager
import fr.sdis83.remocra.mobile.services.AuthService
import fr.sdis83.remocra.mobile.utils.getVersionName

@Suppress("DEPRECATION")
class LoginWorker constructor(
    context: Context,
    workerParams: WorkerParameters,
) : Worker(context, workerParams) {

    companion object {
        private const val TAG: String = "LoginWorker"
    }

    override fun doWork(): Result {
        val retrofitBuilder = AuthService.getRetroFitInstance(applicationContext)
        val sessionManager = SessionManager(applicationContext)

        val versionName = getVersionName(applicationContext)

        val loginResponse = retrofitBuilder.doLogin(
            inputData.getString("username") ?: throw Exception(""),
            inputData.getString("password") ?: throw Exception(""),
            versionName,
        ).execute()

        if (!loginResponse.isSuccessful) {
            // Si la version n'est pas compatible, on met un message
            val json = JsonParser()
            val errorMessage: JsonObject? = json.parse(loginResponse.errorBody()?.string()).asJsonObject
            if (errorMessage != null && errorMessage.get("message").toString().contains("version", ignoreCase = true)) {
                // On renvoie le message à logger !
                val outputData = Data.Builder()
                    .putString("VERSION_INCOMPATIBLE", errorMessage.get("message").toString())
                    .build()

                return Result.failure(outputData)
            } else {
                Log.e(TAG, "Error executing work: " + loginResponse.errorBody().toString())
            }
            return Result.failure()
        }

        sessionManager.saveAuthToken(loginResponse.body()!!.token)

        return Result.success()
    }
}
