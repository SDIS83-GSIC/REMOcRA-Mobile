package fr.sdis83.remocra.mobile.synchronisation

import android.content.Context
import androidx.work.WorkerParameters
import fr.sdis83.remocra.mobile.database.RemocraDatabase
import fr.sdis83.remocra.mobile.services.SynchronisationService
import fr.sdis83.remocra.mobile.workers.WorkerRemocra

class SynchroTourneeFinWorker constructor(
    context: Context,
    workerParams: WorkerParameters,
) : WorkerRemocra(context, workerParams) {
    private val TAG = "SynchroTourneeFinWorker"

    override fun doExecute(): Result {
        val synchronisationDao = RemocraDatabase.getInstance(applicationContext).synchronisationDao()
        val retrofitBuilder = SynchronisationService.getRetroFitInstance(applicationContext)

        // Récupérer les tournées et les envoyer au fur et à mesure
        val tournees = synchronisationDao.getAllTournee().filter { it.progression == 1f }.map { it.tournee }
        val mapLPeiTournee = synchronisationDao.getAllLPeiTournee().filter { tournees.map { it.tourneeId }.contains(it.tourneeId) }.groupBy { it.tourneeId }

        tournees.forEach { tournee ->
            val res = retrofitBuilder.incomingToRemocra(
                tourneeId = tournee.tourneeId,
            ).execute()

            when (res.code()) {
                200, 201, 409 -> Unit
                else -> throw IllegalArgumentException(res.message())
            }

            // On supprime les données
            synchronisationDao.apply {
                deleteVisiteAnomalie(tournee.tourneeId)

                deleteVisite(tournee.tourneeId)
                deleteTourneeSynchronisee(tournee.tourneeId)
            }
        }

        // Puis on supprime les autres données
        synchronisationDao.apply {
            deleteNewPeiSynchronises()
            deleteContactsRoleSynchronises()
            deleteContactsSynchronises()
            deleteGestionnaireSynchronises()
        }

        return Result.success()
    }
}
