package fr.sdis83.remocra.mobile.viewmodels

import android.app.Application
import android.widget.Toast
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import fr.sdis83.remocra.mobile.database.RemocraDatabase
import fr.sdis83.remocra.mobile.synchronisation.SynchroContactRoleWorker
import fr.sdis83.remocra.mobile.synchronisation.SynchroContactWorker
import fr.sdis83.remocra.mobile.synchronisation.SynchroGestionnaireWorker
import fr.sdis83.remocra.mobile.synchronisation.SynchroNewPeiWorker
import fr.sdis83.remocra.mobile.synchronisation.SynchroTourneeFinWorker
import fr.sdis83.remocra.mobile.synchronisation.SynchroTourneeWorker
import fr.sdis83.remocra.mobile.synchronisation.SynchroVisiteAnomalieWorker
import fr.sdis83.remocra.mobile.synchronisation.SynchroVisiteWorker
import fr.sdis83.remocra.mobile.workers.ReferentielWorker

class SyncViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "SyncViewModel"
        enum class JobStatus {
            WAITING,
            SUCCESS,
            LOADING,
            ERROR_VERSION,
            ERROR,
        }
    }

    private val hydrantVisiteDao = RemocraDatabase.getInstance(getApplication()).visiteDao()
    private val tourneesDao = RemocraDatabase.getInstance(getApplication()).tourneesDao()

    val visiteCount = hydrantVisiteDao.getPeiVisiteCount()
    val lPeiTourneeCount = hydrantVisiteDao.getLPeiTourneeCount()
    val tourneeNotDoneCount = tourneesDao.getTourneeNotDoneCount()
    val tourneeCount = tourneesDao.getTourneeCount()
    val PeiCreesCount = tourneesDao.getHydrantsCreesCount()
    val isBusy = MutableLiveData(false)

    private var referentielStatus = mutableStateOf(JobStatus.WAITING)

    fun synchro(application: Application) {
        val synchroGestionnaire = OneTimeWorkRequestBuilder<SynchroGestionnaireWorker>()
            .build()

        val synchroContact = OneTimeWorkRequestBuilder<SynchroContactWorker>()
            .build()

        val synchroContactRole = OneTimeWorkRequestBuilder<SynchroContactRoleWorker>()
            .build()

        val synchroNewPei = OneTimeWorkRequestBuilder<SynchroNewPeiWorker>()
            .build()

        val synchroTourneeWorker = OneTimeWorkRequestBuilder<SynchroTourneeWorker>()
            .build()

        val synchroVisiteWorker = OneTimeWorkRequestBuilder<SynchroVisiteWorker>()
            .build()

        val synchroVisiteAnomalieWorker =
            OneTimeWorkRequestBuilder<SynchroVisiteAnomalieWorker>()
                .build()

        val synchroTourneeFinWorker =
            OneTimeWorkRequestBuilder<SynchroTourneeFinWorker>()
                .build()

        val referentielWorker = OneTimeWorkRequestBuilder<ReferentielWorker>()
            .build()

        WorkManager.getInstance(application).let { workManager ->
            workManager
                .beginWith(synchroGestionnaire)
                .then(synchroContact)
                .then(synchroContactRole)
                .then(synchroNewPei)
                .then(synchroTourneeWorker)
                .then(synchroVisiteWorker)
                .then(synchroVisiteAnomalieWorker)
                .then(synchroTourneeFinWorker)
                // On recharge le référentiel à la toute fin pour avoir les données à jour
                .then(referentielWorker)
                .enqueue()

            workManager.getWorkInfoByIdLiveData(referentielWorker.id).observeForever {
                when (it.state) {
                    WorkInfo.State.RUNNING -> {
                        referentielStatus.value = JobStatus.LOADING
                        Toast.makeText(
                            application,
                            "Synchronisation en cours...",
                            Toast.LENGTH_LONG,
                        )
                            .show()
                        isBusy.value = true
                    }

                    WorkInfo.State.SUCCEEDED -> {
                        referentielStatus.value = JobStatus.SUCCESS
                        Toast.makeText(
                            application,
                            "Synchronisation terminée.",
                            Toast.LENGTH_LONG,
                        )
                            .show()
                        isBusy.value = false
                    }

                    WorkInfo.State.FAILED -> {
                        referentielStatus.value = JobStatus.ERROR
                        Toast.makeText(
                            application,
                            "Echec lors de la synchronisation.",
                            Toast.LENGTH_LONG,
                        )
                            .show()
                        isBusy.value = false
                    }

                    else -> {
                        referentielStatus.value = JobStatus.LOADING
                        Toast.makeText(application, "En attente...", Toast.LENGTH_LONG)
                            .show()
                        isBusy.value = true
                    }
                }
            }
        }
    }
}
