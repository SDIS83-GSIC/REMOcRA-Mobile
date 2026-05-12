package fr.sdis83.remocra.mobile.synchronisation

import android.content.Context
import android.util.Log
import androidx.work.WorkerParameters
import fr.sdis83.remocra.mobile.database.RemocraDatabase
import fr.sdis83.remocra.mobile.services.SynchronisationService
import fr.sdis83.remocra.mobile.workers.WorkerRemocra

class SynchroNewPeiWorker constructor(
    context: Context,
    workerParams: WorkerParameters,
) : WorkerRemocra(context, workerParams) {
    private val TAG = "SynchroNewPeiWorker"

    override fun doExecute(): Result = try {
        val synchronisationDao = RemocraDatabase.getInstance(applicationContext).synchronisationDao()
        val retrofitBuilder = SynchronisationService.getRetroFitInstance(applicationContext)

        val newPei = synchronisationDao.getAllNewPei()
        val typePei = synchronisationDao.getAllTypePei()

        newPei.forEach { pei ->
            val res = retrofitBuilder.postPei(
                peiId = pei.peiId,
                lat = pei.lat,
                lon = pei.lon,
                peiTypePei = typePei.find { t -> t.typePeiId == pei.typePeiId }!!.typePeiCode,
                gestionnaireId = pei.gestionnaireId,
                domaineId = pei.domaineId,
                natureId = pei.natureId,
                natureDeciId = pei.natureDeciId,
                peiObservation = pei.observation,
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
