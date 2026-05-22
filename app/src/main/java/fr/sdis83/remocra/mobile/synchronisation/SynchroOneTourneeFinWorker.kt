package fr.sdis83.remocra.mobile.synchronisation

import android.content.Context
import androidx.work.WorkerParameters
import fr.sdis83.remocra.mobile.database.RemocraDatabase
import fr.sdis83.remocra.mobile.services.SynchronisationService
import fr.sdis83.remocra.mobile.workers.WorkerRemocra
import java.util.UUID

class SynchroOneTourneeFinWorker(
    context: Context,
    workerParams: WorkerParameters,
) : WorkerRemocra(context, workerParams) {

    override fun doExecute(): Result {
        val synchronisationDao = RemocraDatabase.getInstance(applicationContext).synchronisationDao()
        val retrofitBuilder = SynchronisationService.getRetroFitInstance(applicationContext)

        // on récupère le paramètre tourneeId qui est passé à ce worker
        val tourneeIdString = inputData.getString("tourneeId") ?: throw IllegalArgumentException("tourneeId est requis")

        val tourneeId = UUID.fromString(tourneeIdString)

        val res = retrofitBuilder.incomingToRemocra(
            tourneeId = tourneeId,
        ).execute()

        when (res.code()) {
            200, 201, 409 -> Unit
            else -> throw IllegalArgumentException("Erreur API ${res.code()} : ${res.errorBody()?.string()} ")
        }

        // On supprime les données
        synchronisationDao.apply {
            deleteVisiteAnomalie(tourneeId)
            deleteVisite(tourneeId)
            deleteTourneeSynchronisee(tourneeId)
        }

        return Result.success()
    }
}
