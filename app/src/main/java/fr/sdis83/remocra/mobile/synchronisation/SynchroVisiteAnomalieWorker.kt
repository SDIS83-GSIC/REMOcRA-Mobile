package fr.sdis83.remocra.mobile.synchronisation

import android.content.Context
import android.util.Log
import androidx.work.WorkerParameters
import fr.sdis83.remocra.mobile.database.RemocraDatabase
import fr.sdis83.remocra.mobile.services.SynchronisationService
import fr.sdis83.remocra.mobile.workers.WorkerRemocra

class SynchroVisiteAnomalieWorker constructor(
    context: Context,
    workerParams: WorkerParameters,
) : WorkerRemocra(context, workerParams) {
    private val TAG = "SynchroVisiteAnomalieWorker"

    override fun doExecute(): Result = try {
        val synchronisationDao = RemocraDatabase.getInstance(applicationContext).synchronisationDao()
        val retrofitBuilder = SynchronisationService.getRetroFitInstance(applicationContext)

        val tournees = synchronisationDao.getAllTournee().filter { it.progression == 1f }.map { it.tournee }
        val visitesAnomalie = synchronisationDao.getAllVisiteAnomalie().filter { tournees.map { it.tourneeId }.contains(it.tourneeId) }

        visitesAnomalie.forEach { visiteAnomalie ->
            val res = retrofitBuilder.postVisiteAnomalie(
                visiteId = visiteAnomalie.visiteId,
                anomalieId = visiteAnomalie.anomalieId,
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
