package fr.sdis83.remocra.mobile.synchronisation

import android.content.Context
import androidx.work.WorkerParameters
import fr.sdis83.remocra.mobile.database.RemocraDatabase
import fr.sdis83.remocra.mobile.services.SynchronisationService
import fr.sdis83.remocra.mobile.workers.WorkerRemocra
import java.util.UUID

class SynchroOneTourneeWorker(
    context: Context,
    workerParams: WorkerParameters,
) : WorkerRemocra(context, workerParams) {

    override fun doExecute(): Result {
        val synchronisationDao =
            RemocraDatabase.getInstance(applicationContext).synchronisationDao()
        val retrofitBuilder = SynchronisationService.getRetroFitInstance(applicationContext)

        // on récupère le paramètre tourneeId qui est passé à ce worker
        val tourneeId = inputData.getString("tourneeId")
            ?: throw IllegalArgumentException("tourneeId est requis")

        val tournee = synchronisationDao.getTourneeById(UUID.fromString(tourneeId))
            ?: throw IllegalArgumentException("Tournee with id $tourneeId not found in database")
        val res = retrofitBuilder.postTournee(
            tourneeId = tournee.tourneeId,
            tourneeLibelle = tournee.nom,
        ).execute()

        when (res.code()) {
            200, 201, 409 -> Unit
            else -> throw IllegalStateException("Erreur API ${res.code()} : ${res.errorBody()?.string()} ")
        }
        return Result.success()
    }
}
