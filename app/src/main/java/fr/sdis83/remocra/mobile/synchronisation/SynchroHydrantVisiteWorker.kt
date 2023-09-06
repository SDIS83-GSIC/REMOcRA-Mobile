package fr.sdis83.remocra.mobile.synchronisation

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import fr.sdis83.remocra.mobile.database.RemocraDatabase
import fr.sdis83.remocra.mobile.services.SynchronisationService
import java.time.format.DateTimeFormatter

class SynchroHydrantVisiteWorker constructor(
    context: Context,
    workerParams: WorkerParameters,
) : Worker(context, workerParams) {
    private val TAG = "SynchroHydrantVisiteWorker"

    override fun doWork(): Result = try {
        val synchronisationDao = RemocraDatabase.getInstance(applicationContext).synchronisationDao()
        val retrofitBuilder = SynchronisationService.getRetroFitInstance(applicationContext)

        val hydrantsVisites = synchronisationDao.getAllHydrantVisite()
        val hydrants = synchronisationDao.getAllHydrant()

        hydrantsVisites.forEach { hydrantVisite ->
            val res = retrofitBuilder.postHydrantsVisites(
                idHydrantVisite = hydrantVisite.idHydrantVisite,
                idHydrant = hydrants.first { it.idHydrant == hydrantVisite.idHydrant }.idRemocra!!,
                date = hydrantVisite.dateVisite.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")).toString(),
                idTypeVisite = hydrantVisite.idTypeHydrantSaisie!!,
                ctrDebitPression = hydrantVisite.ctrlDebitPression,
                agent1 = hydrantVisite.agent1,
                agent2 = hydrantVisite.agent2,
                debit = hydrantVisite.debit,
                pression = hydrantVisite.pression,
                pressionDyn = hydrantVisite.pressionDyn,
                observations = hydrantVisite.observations,
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
