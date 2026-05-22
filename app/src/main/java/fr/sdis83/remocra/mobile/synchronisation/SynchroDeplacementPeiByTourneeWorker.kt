package fr.sdis83.remocra.mobile.synchronisation

import android.content.Context
import android.util.Log
import androidx.work.WorkerParameters
import fr.sdis83.remocra.mobile.database.RemocraDatabase
import fr.sdis83.remocra.mobile.services.SynchronisationService
import fr.sdis83.remocra.mobile.workers.WorkerRemocra
import java.util.UUID

class SynchroDeplacementPeiByTourneeWorker(
    context: Context,
    workerParams: WorkerParameters,
) : WorkerRemocra(context, workerParams) {
    private val TAG = "SynchroDeplacementPeiByTourneeWorker"

    private val codeOk = listOf(200, 201, 409)

    override fun doExecute(): Result = try {
        val synchronisationDao = RemocraDatabase.getInstance(applicationContext).synchronisationDao()
        val retrofitBuilder = SynchronisationService.getRetroFitInstance(applicationContext)

        // on récupère le paramètre tourneeId qui est passé à ce worker
        val tourneeId = inputData.getString("tourneeId") ?: throw IllegalArgumentException("tourneeId est requis")

        synchronisationDao.getPeiDeplaces(UUID.fromString(tourneeId)).forEach {
            // On va chercher les PEI qui ont été déplacés pour les synchroniser avec le serveur
            val res = retrofitBuilder.postDeplacementPei(
                peiId = it.peiId,
                tourneeId = it.tourneeId,
                lat = it.lat,
                lon = it.lon,
            ).execute()

            if (!codeOk.contains(res.code())) {
                throw IllegalArgumentException("Erreur API ${res.code()} : ${res.errorBody()?.string()} ")
            }
        }

        Result.success()
    } catch (e: Throwable) {
        Log.e(TAG, "Error executing work: " + e.message, e)
        failureWithError(e, "Erreur de synchronisation de la tournée")
    }
}
