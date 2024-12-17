package fr.sdis83.remocra.mobile.synchronisation

import android.content.Context
import android.util.Log
import androidx.work.WorkerParameters
import fr.sdis83.remocra.mobile.database.RemocraDatabase
import fr.sdis83.remocra.mobile.services.SynchronisationService
import fr.sdis83.remocra.mobile.utils.createImageFormData
import fr.sdis83.remocra.mobile.workers.WorkerRemocra
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class SynchroVisiteWorker constructor(
    context: Context,
    workerParams: WorkerParameters,
) : WorkerRemocra(context, workerParams) {
    private val TAG = "SynchroVisiteWorker"

    override fun doExecute(): Result = try {
        val synchronisationDao = RemocraDatabase.getInstance(applicationContext).synchronisationDao()
        val retrofitBuilder = SynchronisationService.getRetroFitInstance(applicationContext)

        val visites = synchronisationDao.getAllVisite()
        val photosPei = synchronisationDao.getPhotoPei()
        val tournees = synchronisationDao.getAllLPeiTournee()
        val typesVisites = synchronisationDao.getAllTypeVisite()

        visites.forEach { visite ->
            val res = retrofitBuilder.postVisites(
                visiteId = visite.visiteId,
                tourneeId = tournees.first { it.peiId == visite.peiId }.tourneeId,
                peiId = visite.peiId,
                visiteDate = visite.dateVisite.formatDate(),
                visiteTypeVisite = typesVisites.first { it.typeVisiteId == visite.typeVisiteId }.typeVisiteCode,
                ctrDebitPression = visite.ctrlDebitPression,
                visiteAgent1 = visite.agent1,
                visiteAgent2 = visite.agent2,
                visiteCtrlDebitPressionDebit = visite.debit,
                visiteCtrlDebitPressionPression = visite.pression,
                visiteCtrlDebitPressionPressionDyn = visite.pressionDyn,
                visiteObservations = visite.observations,
                hasAnomalieChange = visite.hasAnomalieChanges,
            ).execute()

            when (res.code()) {
                200, 201, 409 -> Unit
                else -> throw IllegalArgumentException(res.message())
            }

            // Puis on s'occupe des photos
            val photos = photosPei.filter { visite.peiId == it.peiId }
            if (photos.isNotEmpty()) {
                photos.forEach {
                    val resPhotoPei = retrofitBuilder.postPhotoPei(
                        photoId = it.photoId,
                        peiId = it.peiId,
                        photoDate = it.datePhoto.formatDate(),
                        photo = createImageFormData(
                            "photo",
                            it.path,
                        )!!,
                    ).execute()

                    when (resPhotoPei.code()) {
                        200, 201, 409 -> Unit
                        else -> throw IllegalArgumentException(resPhotoPei.message())
                    }
                }
            }
        }

        Result.success()
    } catch (e: Throwable) {
        Log.e(TAG, "Error executing work: " + e.message, e)
        Result.failure()
    }

    private fun ZonedDateTime.formatDate(): String {
        return format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")).toString()
    }
}
