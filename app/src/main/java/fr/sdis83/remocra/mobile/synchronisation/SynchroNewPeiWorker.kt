package fr.sdis83.remocra.mobile.synchronisation

import android.content.Context
import android.util.Log
import androidx.work.WorkerParameters
import fr.sdis83.remocra.mobile.database.RemocraDatabase
import fr.sdis83.remocra.mobile.services.SynchronisationService
import fr.sdis83.remocra.mobile.workers.WorkerRemocra
import java.util.UUID

class SynchroNewPeiWorker(
    context: Context,
    workerParams: WorkerParameters,
) : WorkerRemocra(context, workerParams) {

    override fun doExecute(): Result = try {
        val synchronisationDao = RemocraDatabase.getInstance(applicationContext).synchronisationDao()
        val retrofitBuilder = SynchronisationService.getRetroFitInstance(applicationContext)

        // on récupère le paramètre peiId qui est passé à ce worker
        val peiId = inputData.getString("peiId")?.let(UUID::fromString) ?: throw IllegalArgumentException("peiId est requis")

        val newPei = synchronisationDao.getNewPeiById(peiId) ?: throw IllegalArgumentException("PEI avec l'id $peiId non trouvé")
        val typePei = synchronisationDao.getAllTypePei()

        val res = retrofitBuilder.postPei(
            peiId = newPei.peiId,
            lat = newPei.lat,
            lon = newPei.lon,
            peiTypePei = typePei.find { t -> t.typePeiId == newPei.typePeiId }!!.typePeiCode,
            gestionnaireId = newPei.gestionnaireId,
            natureId = newPei.natureId,
            natureDeciId = newPei.natureDeciId,
            peiObservation = newPei.observation,
            domaineId = newPei.domaineId,
        ).execute()

        when (res.code()) {
            200, 201, 409 -> Unit
            else -> throw IllegalArgumentException(res.errorBody()?.string())
        }

        Result.success()
    } catch (e: Throwable) {
        Log.e(workerTag, "Error executing work: " + e.message, e)
        failureWithError(e, "Erreur lors de la synchronisation du PEI")
    }
}
