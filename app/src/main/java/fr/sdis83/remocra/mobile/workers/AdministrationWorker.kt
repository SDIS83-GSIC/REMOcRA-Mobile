package fr.sdis83.remocra.mobile.workers

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import fr.sdis83.remocra.mobile.database.RemocraDatabase
import fr.sdis83.remocra.mobile.services.AuthService
import fr.sdis83.remocra.mobile.utils.GlobalConstants

class AdministrationWorker constructor(
    context: Context,
    workerParams: WorkerParameters,
) : Worker(context, workerParams) {

    companion object {
        private const val TAG: String = "AdministrationWorker"
    }

    override fun doWork(): Result {
        val retrofitBuilder = AuthService.rebuildUrl(applicationContext)
        val paramConfDao = RemocraDatabase.getInstance(applicationContext).paramConfDao()

        val connexionTestResponse = retrofitBuilder.checkUrl().execute()

        if (!connexionTestResponse.isSuccessful) {
            return Result.failure()
        }

        // On sauvegarde dans la base de données le mot de passe
        paramConfDao.updateParamConf(
            cle = GlobalConstants.MDP_ADMINISTRATEUR,
            valeur = connexionTestResponse.body().toString(),
        )

        return Result.success()
    }
}
