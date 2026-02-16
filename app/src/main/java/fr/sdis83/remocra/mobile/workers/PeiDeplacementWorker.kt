package fr.sdis83.remocra.mobile.workers

import android.content.Context
import androidx.work.WorkerParameters
import fr.sdis83.remocra.mobile.services.SynchronisationService
import retrofit2.HttpException

class PeiDeplacementWorker(
    appContext: Context,
    workerParams: WorkerParameters,
) : WorkerRemocra(appContext, workerParams) {
    override fun doExecute(): Result {
        val lat = inputData.getDouble(KEY_LAT, 0.0)
        val lon = inputData.getDouble(KEY_LON, 0.0)
        val context = applicationContext
        val service = SynchronisationService.getRetroFitInstance(context)
        return try {
            val response = service.checkZoneCompetence(lat, lon).execute()
            if (response.isSuccessful) {
                Result.success()
            } else {
                Result.failure()
            }
        } catch (e: HttpException) {
            Result.retry()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    companion object {
        const val KEY_LAT = "lat"
        const val KEY_LON = "lon"
    }
}
