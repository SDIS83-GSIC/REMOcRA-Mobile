package fr.sdis83.remocra.mobile.viewmodels

import android.app.Application
import android.content.Context
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import fr.sdis83.remocra.mobile.workers.ExportCaracteristiquesTabletteWorker
import fr.sdis83.remocra.mobile.workers.ExportLogWorker

class ExportViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val TAG: String = "ExportViewModel"
    }

    fun exportDiagnostics(context: Context) {
        val exportLogWorker = OneTimeWorkRequestBuilder<ExportLogWorker>().build()
        val exportCaracteristiquesTabletteWorker =
            OneTimeWorkRequestBuilder<ExportCaracteristiquesTabletteWorker>().build()

        WorkManager.getInstance(getApplication()).let { workManager ->
            workManager.beginWith(exportLogWorker)
                .then(exportCaracteristiquesTabletteWorker)
                .enqueue()

            workManager.getWorkInfoByIdLiveData(exportLogWorker.id).observeForever {
                when (it.state) {
                    WorkInfo.State.RUNNING -> {
                        Toast.makeText(context, "Export en cours", Toast.LENGTH_SHORT).show()
                    }

                    WorkInfo.State.SUCCEEDED -> {
                        Toast.makeText(context, "Export réussi", Toast.LENGTH_SHORT).show()
                    }

                    WorkInfo.State.FAILED -> {
                        Toast.makeText(
                            context,
                            "Impossible d'exporter les logs",
                            Toast.LENGTH_SHORT,
                        ).show()
                    }

                    else -> {}
                }
            }
        }
    }
}
