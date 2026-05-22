package fr.sdis83.remocra.mobile.synchronisation

import android.content.Context
import android.util.Log
import androidx.work.WorkerParameters
import fr.sdis83.remocra.mobile.database.RemocraDatabase
import fr.sdis83.remocra.mobile.services.SynchronisationService
import fr.sdis83.remocra.mobile.workers.WorkerRemocra
import java.util.UUID

class SynchroVisiteAnomalieByTourneeWorker(
    context: Context,
    workerParams: WorkerParameters,
) : WorkerRemocra(context, workerParams) {

    override fun doExecute(): Result = try {
        val synchronisationDao = RemocraDatabase.getInstance(applicationContext).synchronisationDao()
        val retrofitBuilder = SynchronisationService.getRetroFitInstance(applicationContext)

        // on récupère le paramètre tourneeId qui est passé à ce worker
        val tourneeId = inputData.getString("tourneeId") ?: throw IllegalArgumentException("tourneeId est requis")
        val visitesAnomalie = synchronisationDao.getAllVisiteAnomalie(tourneeId = UUID.fromString(tourneeId))

        visitesAnomalie.forEach { visiteAnomalie ->
            val res = retrofitBuilder.postVisiteAnomalie(
                visiteId = visiteAnomalie.visiteId,
                anomalieId = visiteAnomalie.anomalieId,
            ).execute()

            when (res.code()) {
                200, 201, 409 -> Unit
                else -> throw IllegalArgumentException("Erreur API ${res.code()} : ${res.errorBody()?.string()} ")
            }
        }
        Result.success()
    } catch (e: Throwable) {
        Log.e(workerTag, "Error executing work: " + e.message, e)
        failureWithError(e, "Erreur de synchronisation des visites anomalie de la tournée")
    }
}
