package fr.sdis83.remocra.mobile.workers

import android.content.Context
import android.util.Log
import androidx.work.Data
import androidx.work.WorkerParameters
import fr.sdis83.remocra.mobile.database.LPeiTournee
import fr.sdis83.remocra.mobile.database.RemocraDatabase
import fr.sdis83.remocra.mobile.database.Tournee
import fr.sdis83.remocra.mobile.services.ReferentielService

class ReserveTourneesWorker constructor(
    context: Context,
    workerParams: WorkerParameters,
) : WorkerRemocra(context, workerParams) {

    companion object {
        private const val TAG: String = "ReserveTourneesWorker"
    }

    override fun doExecute(): Result {
        val retrofitBuilder = ReferentielService.getRetroFitInstance(applicationContext)
        val tourneesDao = RemocraDatabase.getInstance(applicationContext).tourneesDao()

        // On passe toutes les tournées choisies au serveur pour pouvoir les réserver
        val tourneesReserveesResponse = retrofitBuilder.reserveTourneesDisponibles(
            tourneesDao.getTourneesAReserver()
                .map { it.tourneeDispoId.toString() }
                .toTypedArray(),
        ).execute()

        if (!tourneesReserveesResponse.isSuccessful) {
            Log.e(TAG, "Error executing work: " + tourneesReserveesResponse.errorBody().toString())
            return Result.failure()
        }

        // On stocke les tournées en cours dans l'appli
        tourneesReserveesResponse.body()?.tourneesReservees?.forEach { tournee ->
            tourneesDao.insertTournee(
                Tournee(
                    tourneeId = tournee.tourneeId,
                    nom = tournee.tourneeLibelle,
                    peiCount = tournee.listPeiIdWithOrdre.size,
                ),
            )

            tournee.listPeiIdWithOrdre.forEach {
                tourneesDao.insertLPeiTournee(
                    LPeiTournee(
                        peiId = it.peiId,
                        tourneeId = it.tourneeId,
                        ordre = it.ordre,
                    ),
                )
            }

            tourneesDao.truncateTourneesDispos()
        }

        val outputData = Data.Builder()
            .putString(
                "NON_RESERVEES",
                tourneesReserveesResponse.body()?.tourneesNonReservees?.map { it.tourneeLibelle }
                    ?.joinToString(", "),
            )
            .build()

        return Result.success(outputData)
    }
}
