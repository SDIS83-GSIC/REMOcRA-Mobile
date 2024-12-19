package fr.sdis83.remocra.mobile.workers

import android.content.Context
import android.util.Log
import androidx.work.WorkerParameters
import fr.sdis83.remocra.mobile.database.RemocraDatabase
import fr.sdis83.remocra.mobile.services.ReferentielService
import java.util.UUID

class AnnuleReservationTourneeWorker constructor(
    context: Context,
    workerParams: WorkerParameters,
) : WorkerRemocra(context, workerParams) {

    companion object {
        private const val TAG: String = "AnnuleReservationTourneeWorker"
    }

    override fun doExecute(): Result {
        val retrofitBuilder = ReferentielService.getRetroFitInstance(applicationContext)
        val tourneeDao = RemocraDatabase.getInstance(applicationContext).tourneeDao()

        val tourneeId = UUID.fromString(inputData.getString("tourneeId"))

        // On passe toutes les tournées choisies au serveur pour pouvoir les réserver
        val annuleReservationResponse = retrofitBuilder.annuleReservation(
            tourneeId,
        ).execute()

        if (!annuleReservationResponse.isSuccessful) {
            Log.e(TAG, "Error executing work: " + annuleReservationResponse.errorBody().toString())
            return Result.failure()
        }

        // Etape 1 : on supprime le lien entre les PEI et la tournée à supprimer
        tourneeDao.deletePeiTournee(tourneeId)

        // Etape 2 : Suppression des visites anomalies
        val listvisiteId = tourneeDao.getListVisiteIdByTournee(tourneeId)
        tourneeDao.deleteLVisiteAnomalie(listvisiteId)

        // Etape 3 : Les visites
        tourneeDao.deleteVisite(tourneeId)

        // Etape 4 : Les tournées
        tourneeDao.deleteTournee(tourneeId)

        return Result.success()
    }
}
