package fr.sdis83.remocra.mobile.workers

import android.content.Context
import android.util.Log
import androidx.work.WorkerParameters
import fr.sdis83.remocra.mobile.database.RemocraDatabase
import fr.sdis83.remocra.mobile.database.TourneeDispo
import fr.sdis83.remocra.mobile.services.ReferentielService

class TourneesDisposWorker constructor(
    context: Context,
    workerParams: WorkerParameters,
) : WorkerRemocra(context, workerParams) {

    override fun doExecute(): Result = try {
        val retrofitBuilder = ReferentielService.getRetroFitInstance(applicationContext)
        val tourneesDao = RemocraDatabase.getInstance(applicationContext).tourneesDao()

        // On récupère les tournées disponibles de l'organisme
        val tourneesDisponiblesResponse = retrofitBuilder.getTourneesDisponibles().execute()

        if (!tourneesDisponiblesResponse.isSuccessful) {
            Log.e(workerTag, "Error executing work: " + tourneesDisponiblesResponse.errorBody().toString())
            failureWithError("${tourneesDisponiblesResponse.errorBody()?.string()}")
            Result.failure()
        }

        tourneesDao.truncateTourneesDispos()
        tourneesDisponiblesResponse.body()?.forEach {
            tourneesDao.insertTourneeDispo(
                TourneeDispo(
                    it.tourneeId,
                    it.tourneeLibelle,
                ),
            )
        }

        Result.success()
    } catch (e: Throwable) {
        Log.e(workerTag, "Error executing work: " + e.message, e)
        failureWithError(e, "Erreur lors de la synchronisation des tournées disponibles")
    }
}
