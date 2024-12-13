package fr.sdis83.remocra.mobile.workers

import android.content.Context
import android.util.Log
import androidx.work.WorkerParameters
import fr.sdis83.remocra.mobile.database.RemocraDatabase
import fr.sdis83.remocra.mobile.services.ReferentielService

class TourneesDisposWorker constructor(
    context: Context,
    workerParams: WorkerParameters,
) : WorkerRemocra(context, workerParams) {

    companion object {
        private const val TAG: String = "TourneesDisposWorker"
    }

    override fun doExecute(): Result {
        val retrofitBuilder = ReferentielService.getRetroFitInstance(applicationContext)
        val tourneesDao = RemocraDatabase.getInstance(applicationContext).tourneesDao()

        // On récupère les tournées disponibles de l'organisme
        val tourneesDisponiblesResponse = retrofitBuilder.getTourneesDisponibles().execute()

        if (!tourneesDisponiblesResponse.isSuccessful) {
            Log.e(TAG, "Error executing work: " + tourneesDisponiblesResponse.errorBody().toString())
            return Result.failure()
        }

        tourneesDao.truncateTourneesDispos()
        tourneesDisponiblesResponse.body()?.forEach {
            tourneesDao.insertTourneeDispo(it)
        }

        return Result.success()
    }
}
