package fr.sdis83.remocra.mobile.workers

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import fr.sdis83.remocra.mobile.database.Parametre
import fr.sdis83.remocra.mobile.database.RemocraDatabase
import fr.sdis83.remocra.mobile.services.AuthService
import fr.sdis83.remocra.mobile.utils.GlobalConstants
import java.util.UUID

class AdministrationWorker constructor(
    context: Context,
    workerParams: WorkerParameters,
) : Worker(context, workerParams) {

    companion object {
        private const val TAG: String = "AdministrationWorker"
    }

    override fun doWork(): Result {
        val retrofitBuilder = AuthService.rebuildUrl(applicationContext)
        val paramConfDao = RemocraDatabase.getInstance(applicationContext).parametreDao()

        val connexionTestResponse = retrofitBuilder.checkUrl().execute()

        if (!connexionTestResponse.isSuccessful) {
            return Result.failure()
        }

        if (paramConfDao.getParametreList().value?.map { it.parametreCode }?.contains(GlobalConstants.MDP_ADMINISTRATEUR) == true) {
            // On sauvegarde dans la base de données le mot de passe
            paramConfDao.updateParametre(
                cle = GlobalConstants.MDP_ADMINISTRATEUR,
                valeur = connexionTestResponse.body().toString(),
            )
        } else {
            paramConfDao.insertParamConf(
                Parametre(
                    parametreId = UUID.randomUUID(),
                    parametreCode = GlobalConstants.MDP_ADMINISTRATEUR,
                    parametreValeur = connexionTestResponse.body().toString(),
                ),
            )
        }

        return Result.success()
    }
}
