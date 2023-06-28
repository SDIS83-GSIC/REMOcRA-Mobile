package fr.sdis83.remocra.mobile.workers

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import fr.sdis83.remocra.mobile.database.RemocraDatabase
import fr.sdis83.remocra.mobile.services.ReferentielService
import java.util.UUID

class ReferentielWorker constructor(
    context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    companion object {
        private const val TAG: String = "ReferentielWorker"
    }

    override fun doWork(): Result {
        val retrofitBuilder = ReferentielService.getRetroFitInstance(applicationContext)
        val referentielDao = RemocraDatabase.getInstance(applicationContext).referentielDao()

        val referentielResponse = retrofitBuilder.getReferentiel().execute()

        if (!referentielResponse.isSuccessful) {
            Log.e(TAG, "Error executing work: " + referentielResponse.errorBody().toString())
            return Result.failure()
        }

        referentielDao.truncateHydrant()
        referentielDao.truncateContact()
        referentielDao.truncateGestionnaire()
        referentielResponse.body()!!.hydrants.forEach {
            referentielDao.insertHydrant(it.copy(idHydrant = UUID.randomUUID()))
        }
        val gestionnaireMap = mutableListOf<Pair<UUID, Int>>()
        referentielResponse.body()!!.gestionnaires.forEach {
            UUID.randomUUID().let {
                idTournee ->
                referentielDao.insertGestionnaire(it.copy(idGestionnaire = idTournee))
                gestionnaireMap.add(Pair(idTournee, it.idRemocra!!))
            }
        }
        referentielResponse.body()!!.contacts.forEach { contact ->
            referentielDao.insertContact(
                contact.copy(
                    idContact = UUID.randomUUID(),
                    idGestionnaire = gestionnaireMap.find { it.second == contact.idRemocraGestionnaire }?.first
                )
            )
        }

        return Result.success()
    }
}
