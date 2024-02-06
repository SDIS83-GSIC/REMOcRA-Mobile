package fr.sdis83.remocra.mobile.workers

import android.content.Context
import androidx.work.Data
import androidx.work.Worker
import androidx.work.WorkerParameters
import fr.sdis83.remocra.mobile.database.ParamConf
import fr.sdis83.remocra.mobile.database.RemocraDatabase
import fr.sdis83.remocra.mobile.services.AuthService
import fr.sdis83.remocra.mobile.utils.GlobalConstants
import java.util.UUID

class MdpAdministrateurWorker constructor(
    context: Context,
    workerParams: WorkerParameters,
) : Worker(context, workerParams) {

    companion object {
        private const val TAG: String = "AdministrationWorker"
    }

    override fun doWork(): Result {
        val retrofitBuilder = AuthService.rebuildUrl(applicationContext)
        val paramConfDao = RemocraDatabase.getInstance(applicationContext).paramConfDao()

        try {
            val mdpAdminResponse = retrofitBuilder.getMdpAdministrateur().execute()
            if (!mdpAdminResponse.isSuccessful) {
                return Result.failure()
            }
            if (mdpAdminResponse.body() != null) {
                paramConfDao.deleteParamConf(GlobalConstants.MDP_ADMINISTRATEUR)
                paramConfDao.upsertParamConf(
                    ParamConf(
                        UUID.randomUUID(),
                        GlobalConstants.MDP_ADMINISTRATEUR,
                        mdpAdminResponse.body()!!,
                    ),
                )
                val outputData = Data.Builder().putString(
                    GlobalConstants.MDP_ADMINISTRATEUR,
                    mdpAdminResponse.body()!!,
                ).build()
                return Result.success(outputData)
            }
        } catch (e: Exception) {
            // Si on n'a pas réussi à contacté le serveur, c'est que l'URL n'est pas encore définie
            return Result.failure()
        }

        return Result.success()
    }
}
