package fr.sdis83.remocra.mobile.synchronisation

import android.content.Context
import android.util.Log
import androidx.work.WorkerParameters
import fr.sdis83.remocra.mobile.database.RemocraDatabase
import fr.sdis83.remocra.mobile.services.SynchronisationService
import fr.sdis83.remocra.mobile.workers.WorkerRemocra

class SynchroTourneeWorker constructor(
    context: Context,
    workerParams: WorkerParameters,
) : WorkerRemocra(context, workerParams) {
    private val TAG = "SynchroTourneeWorker"

    override fun doExecute(): Result = try {
        val synchronisationDao = RemocraDatabase.getInstance(applicationContext).synchronisationDao()
        val retrofitBuilder = SynchronisationService.getRetroFitInstance(applicationContext)

        val tournees = synchronisationDao.getAllTournee().filter { it.progression == 1f }.map { it.tournee }

        tournees.forEach { tournee ->
            val res = retrofitBuilder.postTournee(
                idTourneeRemocra = tournee.idRemocra,
                nom = tournee.nom,
            ).execute()

            when (res.code()) {
                200, 201, 409 -> Unit
                else -> throw IllegalArgumentException(res.message())
            }
        }

        // On supprime les données
        val idsTournee = tournees.map { it.idTournee }
        synchronisationDao.apply {
            deleteNewHydrantsSynchronises()
            deleteContactsRoleSynchronises()
            deleteContactsSynchronises()
            deleteGestionnaireSynchronises()
            deleteHydrantVisiteAnomalie(idsTournee)
            deleteHydrantVisite(idsTournee)
            deleteTourneesSynchronisees(idsTournee)
        }

        retrofitBuilder.incomingToRemocra().execute()
        Result.success()
    } catch (e: Throwable) {
        Log.e(TAG, "Error executing work: " + e.message, e)
        Result.failure()
    }
}
