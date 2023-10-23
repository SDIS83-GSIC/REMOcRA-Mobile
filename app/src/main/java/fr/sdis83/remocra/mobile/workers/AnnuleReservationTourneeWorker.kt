package fr.sdis83.remocra.mobile.workers

import android.content.Context
import android.util.Log
import androidx.work.WorkerParameters
import fr.sdis83.remocra.mobile.database.RemocraDatabase
import fr.sdis83.remocra.mobile.services.ReferentielService

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

        val idTournee = inputData.getLong("idTournee", -1)
        if (idTournee == -1L) {
            throw Exception("Aucun idTournee")
        }

        // On passe toutes les tournées choisies au serveur pour pouvoir les réserver
        val annuleReservationResponse = retrofitBuilder.annuleReservation(
            idTournee,
        ).execute()

        if (!annuleReservationResponse.isSuccessful) {
            Log.e(TAG, "Error executing work: " + annuleReservationResponse.errorBody().toString())
            return Result.failure()
        }

        // Etape 1 : on supprime le lien entre les hydrants et la tournée à supprimer
        tourneeDao.deleteHydrantTournee(idTournee)

        // Etape 2 : Suppression des hydrants visites anomalies
        val uuidTournee = tourneeDao.getTourneeUUID(idTournee)
        val listIdHydrantVisite = tourneeDao.getListIdHydrantVisite(uuidTournee)
        tourneeDao.deleteHydrantVisiteAnomalie(listIdHydrantVisite)

        // Etape 3 : Les hydrants visites
        tourneeDao.deleteHydrantVisite(uuidTournee)

        // Etape 4 : Les tournées
        tourneeDao.deleteTournee(uuidTournee)

        return Result.success()
    }
}
