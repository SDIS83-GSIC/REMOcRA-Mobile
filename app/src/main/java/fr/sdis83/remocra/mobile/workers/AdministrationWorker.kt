package fr.sdis83.remocra.mobile.workers

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
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

        val connexionTestResponse = retrofitBuilder.checkUrl().execute()

        if (!connexionTestResponse.isSuccessful) {
            return Result.failure()
        }

        return Result.success()
    }
}
