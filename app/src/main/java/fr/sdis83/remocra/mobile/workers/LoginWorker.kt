package fr.sdis83.remocra.mobile.workers

import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import fr.sdis83.remocra.mobile.authn.SessionManager
import fr.sdis83.remocra.mobile.services.AuthService

@Suppress("DEPRECATION")
class LoginWorker constructor(
    context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    companion object {
        private const val TAG: String = "LoginWorker"
    }

    override fun doWork(): Result {
        val retrofitBuilder = AuthService.getRetroFitInstance(applicationContext)
        val sessionManager = SessionManager(applicationContext)

        val versionName =
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                applicationContext.packageManager.getPackageInfo(
                    applicationContext.packageName,
                    PackageManager.PackageInfoFlags.of(0)
                ).versionName
            } else {
                applicationContext.packageManager.getPackageInfo(
                    applicationContext.packageName,
                    PackageManager.GET_META_DATA
                ).versionName
            }

        val loginResponse = retrofitBuilder.doLogin(
            inputData.getString("username") ?: throw Exception(""),
            inputData.getString("password") ?: throw Exception(""),
            versionName,
        ).execute()

        if (!loginResponse.isSuccessful) {
            Log.e(TAG, "Error executing work: " + loginResponse.errorBody().toString())
            return Result.failure()
        }

        sessionManager.saveAuthToken(loginResponse.body()!!.token)

        return Result.success()
    }
}