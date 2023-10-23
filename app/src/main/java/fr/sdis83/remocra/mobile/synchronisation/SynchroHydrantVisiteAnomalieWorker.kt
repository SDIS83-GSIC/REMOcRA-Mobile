package fr.sdis83.remocra.mobile.synchronisation

import android.content.Context
import android.util.Log
import androidx.work.WorkerParameters
import fr.sdis83.remocra.mobile.database.RemocraDatabase
import fr.sdis83.remocra.mobile.services.SynchronisationService
import fr.sdis83.remocra.mobile.workers.WorkerRemocra

class SynchroHydrantVisiteAnomalieWorker constructor(
    context: Context,
    workerParams: WorkerParameters,
) : WorkerRemocra(context, workerParams) {
    private val TAG = "SynchroHydrantVisiteAnomalieWorker"

    override fun doExecute(): Result = try {
        val synchronisationDao = RemocraDatabase.getInstance(applicationContext).synchronisationDao()
        val retrofitBuilder = SynchronisationService.getRetroFitInstance(applicationContext)

        val hydrantsVisitesAnomalie = synchronisationDao.getAllHydrantVisiteAnomalie()

        hydrantsVisitesAnomalie.forEach { hydrantVisiteAnomalie ->
            val res = retrofitBuilder.postHydrantVisiteAnomalie(
                idHydrantVisite = hydrantVisiteAnomalie.idHydrantVisite,
                idAnomalie = hydrantVisiteAnomalie.idAnomalie,
            ).execute()

            when (res.code()) {
                200, 201, 409 -> Unit
                else -> throw IllegalArgumentException(res.message())
            }
        }
        Result.success()
    } catch (e: Throwable) {
        Log.e(TAG, "Error executing work: " + e.message, e)
        Result.failure()
    }
}
