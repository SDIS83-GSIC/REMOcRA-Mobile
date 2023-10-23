package fr.sdis83.remocra.mobile.synchronisation

import android.content.Context
import android.util.Log
import androidx.work.WorkerParameters
import fr.sdis83.remocra.mobile.database.RemocraDatabase
import fr.sdis83.remocra.mobile.services.SynchronisationService
import fr.sdis83.remocra.mobile.workers.WorkerRemocra

class SynchroNewHydrantWorker constructor(
    context: Context,
    workerParams: WorkerParameters,
) : WorkerRemocra(context, workerParams) {
    private val TAG = "SynchroNewHydrantWorker"

    override fun doExecute(): Result = try {
        val synchronisationDao = RemocraDatabase.getInstance(applicationContext).synchronisationDao()
        val retrofitBuilder = SynchronisationService.getRetroFitInstance(applicationContext)

        val newHydrants = synchronisationDao.getAllNewHydrants()

        newHydrants.forEach { hydrant ->
            val res = retrofitBuilder.postHydrants(
                idHydrant = hydrant.idHydrant,
                lat = hydrant.lat,
                lon = hydrant.lon,
                code = hydrant.code!!,
                idGestionnaire = hydrant.idGestionnaire,
                idNature = hydrant.idNature!!,
                idNatureDeci = hydrant.idNatureDeci!!,
                observations = hydrant.observation,
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
