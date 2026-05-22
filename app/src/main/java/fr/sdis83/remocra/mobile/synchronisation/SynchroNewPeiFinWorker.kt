package fr.sdis83.remocra.mobile.synchronisation

import android.content.Context
import androidx.work.WorkerParameters
import fr.sdis83.remocra.mobile.database.RemocraDatabase
import fr.sdis83.remocra.mobile.services.SynchronisationService
import fr.sdis83.remocra.mobile.workers.WorkerRemocra
import java.util.UUID

class SynchroNewPeiFinWorker(
    context: Context,
    workerParams: WorkerParameters,
) : WorkerRemocra(context, workerParams) {

    override fun doExecute(): Result {
        val synchronisationDao = RemocraDatabase.getInstance(applicationContext).synchronisationDao()
        val retrofitBuilder = SynchronisationService.getRetroFitInstance(applicationContext)

        val peiId = inputData.getString("peiId")?.let(UUID::fromString)
            ?: throw IllegalArgumentException("peiId est requis")

        val res = retrofitBuilder.incomingNewPeiToRemocra(
            peiId = peiId,
        ).execute()

        when (res.code()) {
            200, 201, 409 -> Unit
            else -> throw IllegalArgumentException("Erreur API ${res.code()} : ${res.errorBody()?.string()} ")
        }

        // On supprime les données
        synchronisationDao.apply {
            // On supprime car il sera recharger avec la récupération du référentiel
            deleteNewPeiSynchronises(peiId)
        }

        return Result.success()
    }
}
