package fr.sdis83.remocra.mobile.synchronisation

import android.content.Context
import android.util.Log
import androidx.work.WorkerParameters
import fr.sdis83.remocra.mobile.database.RemocraDatabase
import fr.sdis83.remocra.mobile.services.SynchronisationService
import fr.sdis83.remocra.mobile.utils.createImageFormData
import fr.sdis83.remocra.mobile.workers.WorkerRemocra
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

class SynchroVisiteByTourneeWorker(
    context: Context,
    workerParams: WorkerParameters,
) : WorkerRemocra(context, workerParams) {

    override fun doExecute(): Result = try {
        val synchronisationDao = RemocraDatabase.getInstance(applicationContext).synchronisationDao()
        val retrofitBuilder = SynchronisationService.getRetroFitInstance(applicationContext)

        // on récupère le paramètre tourneeId qui est passé à ce worker
        val tourneeIdString = inputData.getString("tourneeId") ?: throw IllegalArgumentException("tourneeId est requis")

        val tourneeId = UUID.fromString(tourneeIdString)

        val visites = synchronisationDao.getAllVisite(tourneeId = tourneeId)
        val photosPei = synchronisationDao.getPhotoPei(visites.map { it.peiId })
        val tourneesPei = synchronisationDao.getAllLPeiTournee(tourneeId)
        val typesVisites = synchronisationDao.getAllTypeVisite()

        visites.forEach { visite ->
            val res = retrofitBuilder.postVisites(
                visiteId = visite.visiteId,
                tourneeId = tourneesPei.first { it.peiId == visite.peiId }.tourneeId,
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
                else -> throw IllegalArgumentException(res.errorBody()?.string())
            }

            // Puis on s'occupe des photos
            val photos = photosPei.filter { visite.peiId == it.peiId }
            if (photos.isNotEmpty()) {
                photos.forEach {
                    val resPhotoPei = retrofitBuilder.postPhotoPei(
                        photoId = it.photoId.toString()
                            .toRequestBody("text/plain".toMediaTypeOrNull()),
                        peiId = it.peiId.toString().toRequestBody("text/plain".toMediaTypeOrNull()),
                        photoDate = it.datePhoto.formatDate()
                            .toRequestBody("text/plain".toMediaTypeOrNull()),
                        photo = createImageFormData(
                            "photo",
                            it.path,
                        )!!,
                    ).execute()

                    when (resPhotoPei.code()) {
                        200, 201, 409 -> Unit
                        else -> throw IllegalArgumentException("Erreur API ${resPhotoPei.code()} : ${resPhotoPei.errorBody()?.string()} ")
                    }
                }
            }
        }

        Result.success()
    } catch (e: Throwable) {
        Log.e(workerTag, "Error executing work: " + e.message, e)
        failureWithError(e, "Erreur de synchronisation des visites de la tournée")
    }

    private fun ZonedDateTime.formatDate(): String {
        return format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")).toString()
    }
}
