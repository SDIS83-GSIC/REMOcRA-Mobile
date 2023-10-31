package fr.sdis83.remocra.mobile.workers

import android.content.Context
import android.os.Build
import android.os.Environment
import android.util.Log
import androidx.work.Data
import androidx.work.Worker
import androidx.work.WorkerParameters
import fr.sdis83.remocra.mobile.database.RemocraDatabase
import fr.sdis83.remocra.mobile.utils.getVersionCode
import fr.sdis83.remocra.mobile.utils.getVersionName
import java.io.File

class ExportCaracteristiquesTabletteWorker constructor(
    context: Context,
    workerParams: WorkerParameters,
) : Worker(context, workerParams) {

    companion object {
        private const val TAG: String = "ExportCaracteristiquesTabletteWorker "
    }

    override fun doWork(): Result = try {
        val caracteristiques = "INFORMATIONS PERIPHERIQUE : \n" +
            "Brand: ${Build.BRAND} \n" +
            "Model: ${Build.MODEL} \n" +
            "SDK: ${Build.VERSION.SDK_INT} \n" +
            "Android version : ${Build.VERSION.RELEASE} \n" +
            "Manufacture: ${Build.MANUFACTURER} \n\n" +
            "INFORMATIONS APPLICATION REMOcRA : \n" +
            "Version REMOcRA : ${getVersionName(applicationContext)} \n" +
            "Version code REMOcRA : ${getVersionCode(applicationContext)} \n" +
            "Version room REMOcRA : ${RemocraDatabase.getInstance(applicationContext).getRoomVersion()}"

        val filename = File(Environment.getExternalStorageDirectory().toString() + "/caracteristiques_remocra.log")
        filename.createNewFile()
        filename.writeText(caracteristiques)

        Result.success(Data.Builder().putString("fileName", filename.absolutePath).build())
    } catch (e: Throwable) {
        Log.e(TAG, "Error executing work: " + e.message, e)
        Result.failure()
    }
}
