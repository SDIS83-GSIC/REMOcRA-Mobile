package fr.sdis83.remocra.mobile.workers

import android.annotation.SuppressLint
import android.content.Context
import android.os.Environment
import android.provider.Settings
import android.util.Log
import androidx.work.Data
import androidx.work.Worker
import androidx.work.WorkerParameters
import fr.sdis83.remocra.mobile.services.LogService
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class ExportLogWorker(
    context: Context,
    workerParams: WorkerParameters,
) : Worker(context, workerParams) {

    companion object {
        private const val TAG: String = "ExportLogWorker"
    }

    override fun doWork(): Result = try {
        val filename = File(Environment.getExternalStorageDirectory().toString() + "/logcat_remocra.log")
        filename.createNewFile()

        val cmd = "logcat -d -f ${filename.absolutePath}"
        Runtime.getRuntime().exec(cmd).waitFor()

        val filePart = okhttp3.MultipartBody.Part.createFormData(
            "file",
            filename.name,
            filename.asRequestBody("text/plain".toMediaTypeOrNull()),
        )

        val androidId = getAndroidId()

        val exportResponse = LogService.getRetroFitInstance(applicationContext)
            .exportLog(
                tabletteId = androidId.toRequestBody("text/plain".toMediaTypeOrNull()),
                file = filePart,
            ).execute()

        when (exportResponse.code()) {
            200, 201, 409 -> Unit
            else -> throw IllegalArgumentException(exportResponse.errorBody()?.string() ?: exportResponse.message())
        }

        Result.success(Data.Builder().putString("fileName", filename.absolutePath).build())
    } catch (e: Throwable) {
        Log.e(TAG, "Error executing work: " + e.message, e)
        Result.failure()
    }

    @SuppressLint("HardwareIds")
    private fun getAndroidId(): String {
        val value = Settings.Secure.getString(
            applicationContext.contentResolver,
            Settings.Secure.ANDROID_ID,
        )
        return if (value.isNullOrBlank()) "unknown" else value
    }
}
