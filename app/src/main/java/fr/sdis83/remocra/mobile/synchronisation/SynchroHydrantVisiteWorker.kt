package fr.sdis83.remocra.mobile.synchronisation

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import fr.sdis83.remocra.mobile.database.RemocraDatabase
import fr.sdis83.remocra.mobile.services.SynchronisationService
import fr.sdis83.remocra.mobile.utils.createImageFormData
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class SynchroHydrantVisiteWorker constructor(
    context: Context,
    workerParams: WorkerParameters,
) : Worker(context, workerParams) {
    private val TAG = "SynchroHydrantVisiteWorker"

    override fun doWork(): Result = try {
        val synchronisationDao = RemocraDatabase.getInstance(applicationContext).synchronisationDao()
        val retrofitBuilder = SynchronisationService.getRetroFitInstance(applicationContext)

        val hydrantsVisites = synchronisationDao.getAllHydrantVisite()
        val hydrants = synchronisationDao.getAllHydrant()
        val hydrantPhotos = synchronisationDao.getHydrantPhoto()

        hydrantsVisites.forEach { hydrantVisite ->
            val idHydrant = hydrants.first { it.idHydrant == hydrantVisite.idHydrant }.idRemocra!!
            val res = retrofitBuilder.postHydrantsVisites(
                idHydrantVisite = hydrantVisite.idHydrantVisite,
                idHydrant = idHydrant,
                date = hydrantVisite.dateVisite.formatDate(),
                idTypeVisite = hydrantVisite.idTypeHydrantSaisie!!,
                ctrDebitPression = hydrantVisite.ctrlDebitPression,
                agent1 = hydrantVisite.agent1,
                agent2 = hydrantVisite.agent2,
                debit = hydrantVisite.debit,
                pression = hydrantVisite.pression,
                pressionDyn = hydrantVisite.pressionDyn,
                observations = hydrantVisite.observations,
            ).execute()

            when (res.code()) {
                200, 201, 409 -> Unit
                else -> throw IllegalArgumentException(res.message())
            }

            // Puis on s'occupe des photos
            val photos = hydrantPhotos.filter { hydrantVisite.idHydrant == it.idHydrant }
            if (photos.isNotEmpty()) {
                photos.forEach {
                    val resHydranPhoto = retrofitBuilder.postHydrantPhoto(
                        idHydrant = idHydrant,
                        datePhoto = it.datePhoto.formatDate(),
                        photo = createImageFormData(
                            "photo",
                            it.path,
                        )!!,
                    ).execute()

                    when (resHydranPhoto.code()) {
                        200, 201, 409 -> Unit
                        else -> throw IllegalArgumentException(res.message())
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
        return format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")).toString()
    }
}
