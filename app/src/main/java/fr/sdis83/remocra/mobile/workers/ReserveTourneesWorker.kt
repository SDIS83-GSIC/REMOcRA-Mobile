package fr.sdis83.remocra.mobile.workers

import android.content.Context
import android.util.Log
import androidx.work.Data
import androidx.work.Worker
import androidx.work.WorkerParameters
import fr.sdis83.remocra.mobile.database.HydrantTournee
import fr.sdis83.remocra.mobile.database.RemocraDatabase
import fr.sdis83.remocra.mobile.database.Tournee
import fr.sdis83.remocra.mobile.services.ReferentielService
import java.util.UUID

class ReserveTourneesWorker constructor(
    context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    companion object {
        private const val TAG: String = "ReserveTourneesWorker"
    }

    override fun doWork(): Result {
        val retrofitBuilder = ReferentielService.getRetroFitInstance(applicationContext)
        val tourneesDao = RemocraDatabase.getInstance(applicationContext).tourneesDao()

        // On passe toutes les tournées choisies au serveur pour pouvoir les réserver
        val tourneesReserveesResponse = retrofitBuilder.reserveTourneesDisponibles(
            tourneesDao.getTourneesAReserver()
                .map { it.idRemocra.toString() }
                .toTypedArray()).execute()

        if (!tourneesReserveesResponse.isSuccessful) {
            Log.e(TAG, "Error executing work: " + tourneesReserveesResponse.errorBody().toString())
            return Result.failure()
        }

        // On stocke les tournées en cours dans l'appli
        tourneesReserveesResponse.body()?.tourneesReservees?.forEach { tournee ->
            val idTournee = UUID.randomUUID()
            tourneesDao.insertTournee(
                Tournee(
                    idTournee = idTournee,
                    idRemocra = tournee.idRemocra,
                    nom = tournee.nom,
                    hydrantCount = tournee.listeHydrant.size
                )
            )


            tournee.listeHydrant.forEach { idHydrant ->
                tourneesDao.insertLienHydrantTournee(
                    HydrantTournee(
                        idHydrantTournee = UUID.randomUUID(),
                        idRemocraHydrant = idHydrant,
                        idRemocraTournee = tournee.idRemocra,
                    )
                )
            }
        }

        val outputData = Data.Builder()
            .putString(
                "NON_RESERVEES",
                tourneesReserveesResponse.body()?.tourneesNonReservees?.map { it.nom }
                    ?.joinToString(", ")
            )
            .build()

        return Result.success(outputData)
    }
}
