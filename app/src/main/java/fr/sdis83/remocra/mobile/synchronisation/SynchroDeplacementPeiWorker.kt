package fr.sdis83.remocra.mobile.synchronisation

import android.content.Context
import android.util.Log
import androidx.work.WorkerParameters
import fr.sdis83.remocra.mobile.database.RemocraDatabase
import fr.sdis83.remocra.mobile.services.SynchronisationService
import fr.sdis83.remocra.mobile.workers.WorkerRemocra
import java.util.UUID

class SynchroDeplacementPeiWorker constructor(
    context: Context,
    workerParams: WorkerParameters,
) : WorkerRemocra(context, workerParams) {
    private val TAG = "SynchroTourneeWorker"

    private val codeOk = listOf(200, 201, 409)

    override fun doExecute(): Result = try {
        val synchronisationDao = RemocraDatabase.getInstance(applicationContext).synchronisationDao()
        val retrofitBuilder = SynchronisationService.getRetroFitInstance(applicationContext)

        val tournees = synchronisationDao.getAllTournee().filter { it.progression == 1f }.map { it.tournee }

        val peiDeplaces = synchronisationDao.getPeiDeplacesByTournee()
        tournees.forEach { tournee ->
            peiDeplaces.filter { it.tourneeId == tournee.tourneeId }.forEach {
                // On va chercher les PEI qui ont été déplacés pour les synchroniser avec le serveur
                val res = retrofitBuilder.postDeplacementPei(
                    peiId = it.peiId,
                    tourneeId = it.tourneeId,
                    lat = it.lat,
                    lon = it.lon,
                ).execute()

                if (!codeOk.contains(res.code())) {
                    throw IllegalArgumentException(res.message())
                }
            }
        }

        Result.success()
    } catch (e: Throwable) {
        Log.e(TAG, "Error executing work: " + e.message, e)
        Result.failure()
    }

    data class PeiDeplace(val lat: Double, val lon: Double, val peiId: UUID, val tourneeId: UUID)
}
