package fr.sdis83.remocra.mobile.workers

import android.content.Context
import android.os.Environment
import android.util.Log
import androidx.work.Data
import androidx.work.Worker
import androidx.work.WorkerParameters
import java.io.File

class ExportLogWorker constructor(
    context: Context,
    workerParams: WorkerParameters,
) : Worker(context, workerParams) {

    companion object {
        private const val TAG: String = "ExportLogWorker"
    }

    override fun doWork(): Result = try {
        val filename = File(Environment.getExternalStorageDirectory().toString() + "/logcat_remocra.log")
        filename.createNewFile()
        val cmd = "logcat -d -f" + filename.absolutePath
        Runtime.getRuntime().exec(cmd)

        Result.success(Data.Builder().putString("fileName", filename.absolutePath).build())
    } catch (e: Throwable) {
        Log.e(TAG, "Error executing work: " + e.message, e)
        Result.failure()
    }
}
