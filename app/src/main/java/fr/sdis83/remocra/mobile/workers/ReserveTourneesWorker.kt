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
        val tourneeDao = RemocraDatabase.getInstance(applicationContext).tourneeDao()

        // On passe toutes les tournées choisies au serveur pour pouvoir les réserver
        val tourneesReserveesResponse = retrofitBuilder.reserveTourneesDisponibles(
            tourneeDao.getTourneesAReserver()
                .map { it.idRemocra.toString() }
                .toTypedArray()).execute()

        if (!tourneesReserveesResponse.isSuccessful) {
            Log.e(TAG, "Error executing work: " + tourneesReserveesResponse.errorBody().toString())
            return Result.failure()
        }


        val listeHydrant = tourneeDao.getHydrants()
        // On stocke les tournées en cours dans l'appli
        tourneesReserveesResponse.body()?.tourneesReservees?.forEach { tournee ->
            val idTournee = UUID.randomUUID()
            tourneeDao.insertTournee(Tournee(
                idTournee = idTournee,
                idRemocra = tournee.idRemocra,
                nom = tournee.nom,
                hydrantCount = tournee.listeHydrant.size)
            )


            tournee.listeHydrant.forEach {idHydrant ->
                val uuidHydrant = listeHydrant.firstOrNull { it.idRemocra == idHydrant }?.idHydrant
                if(uuidHydrant != null) {
                    tourneeDao.insertLienHydrantTournee(HydrantTournee(
                        idHydrantTournee = UUID.randomUUID(),
                        idRemocra = tournee.idRemocra,
                        idHydrant = uuidHydrant,
                        idTournee = idTournee,
                    ))
                } else {
                    Log.e(TAG, "Impossible d'ajouter l'hydrant $idHydrant à la tournée ${tournee.idRemocra} ")
                }

            }
        }

        val outputData = Data.Builder()
            .putString("NON_RESERVEES", tourneesReserveesResponse.body()?.tourneesNonReservees?.map { it.nom }?.joinToString(", "))
            .build()

        return Result.success(outputData)
    }
}