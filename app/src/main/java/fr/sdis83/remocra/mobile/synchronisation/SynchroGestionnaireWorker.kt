package fr.sdis83.remocra.mobile.synchronisation

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import fr.sdis83.remocra.mobile.database.RemocraDatabase
import fr.sdis83.remocra.mobile.services.SynchronisationService

class SynchroGestionnaireWorker constructor(
    context: Context,
    workerParams: WorkerParameters,
) : Worker(context, workerParams) {
    private val TAG = "SynchroGestionnaireWorker"

    override fun doWork(): Result = try {
        val synchronisationDao = RemocraDatabase.getInstance(applicationContext).synchronisationDao()
        val retrofitBuilder = SynchronisationService.getRetroFitInstance(applicationContext)

        val gestionnaires = synchronisationDao.getAllGestionnaire()

        gestionnaires.forEach { gestionnaire ->
            val res = retrofitBuilder.postGestionnaire(
                idGestionnaire = gestionnaire.idGestionnaire,
                idRemocra = gestionnaire.idRemocra,
                codeGestionnaire = gestionnaire.code,
                nomGestionnaire = gestionnaire.nom!!,
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
