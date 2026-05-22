package fr.sdis83.remocra.mobile.viewmodels

import android.app.Application
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import fr.sdis83.remocra.mobile.database.RemocraDatabase
import fr.sdis83.remocra.mobile.database.SynchronisationDao
import fr.sdis83.remocra.mobile.synchronisation.SynchroNewPeiFinWorker
import fr.sdis83.remocra.mobile.synchronisation.SynchroNewPeiWorker
import fr.sdis83.remocra.mobile.workers.ReferentielWorker
import fr.sdis83.remocra.mobile.workers.WorkerRemocra
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

class SyncNewPeiViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val REFERENTIEL_BACKGROUND_WORK_NAME = "ReferentielBackgroundWork"
    }

    private val synchronisationDao = RemocraDatabase.getInstance(getApplication()).synchronisationDao()

    private val newPeiASynchro = MutableStateFlow<List<SynchronisationDao.NewPeiWithDetails>>(emptyList())
    val _newPeiASynchro: StateFlow<List<SynchronisationDao.NewPeiWithDetails>> = newPeiASynchro.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _errorMessageSynchro = MutableStateFlow<String?>(null)
    val errorMessageSynchro: StateFlow<String?> = _errorMessageSynchro.asStateFlow()

    /**
     * Charge les nouveaux PEI.
     */
    fun chargerNewPeiASynchro() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                val nouveauxPei = withContext(Dispatchers.IO) {
                    synchronisationDao.getAllNewPeiWithDetails()
                }
                newPeiASynchro.value = nouveauxPei
            } catch (exception: Exception) {
                _errorMessage.value = exception.message ?: "Erreur inconnue"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun synchroniserNewPei(peiId: UUID) {
        viewModelScope.launch {
            val dataBuilder = Data.Builder()
                .putString("peiId", peiId.toString()).build()

            val synchroNewPeiWorker = OneTimeWorkRequestBuilder<SynchroNewPeiWorker>()
                .setInputData(dataBuilder)
                .build()
            val synchroNewPeiFinWorker = OneTimeWorkRequestBuilder<SynchroNewPeiFinWorker>()
                .setInputData(dataBuilder)
                .build()
            val referentielWorker = OneTimeWorkRequestBuilder<ReferentielWorker>()
                .build()

            WorkManager.getInstance(getApplication()).let { workManager ->
                workManager
                    .beginWith(synchroNewPeiWorker)
                    .then(synchroNewPeiFinWorker)
                    .enqueue()

                // Observer tous les workers pour capter l'erreur exacte, quel que soit l'étape en échec.
                val workersToObserve = listOf(
                    synchroNewPeiWorker,
                    synchroNewPeiFinWorker,
                )

                workersToObserve.forEach { worker ->
                    workManager.getWorkInfoByIdLiveData(worker.id).observeForever { workInfo ->
                        when (workInfo.state) {
                            WorkInfo.State.RUNNING -> {
                                if (worker == synchroNewPeiWorker) {
                                    _isLoading.value = true
                                    _errorMessageSynchro.value = null
                                    Toast.makeText(
                                        getApplication(),
                                        "Synchronisation du nouveau PEI en cours...",
                                        Toast.LENGTH_SHORT,
                                    ).show()
                                }
                            }

                            WorkInfo.State.SUCCEEDED -> {
                                // Succès affiché uniquement à la fin de la chaîne.
                                if (worker == synchroNewPeiFinWorker) {
                                    _isLoading.value = false
                                    _errorMessageSynchro.value = null
                                    workManager.enqueueUniqueWork(
                                        REFERENTIEL_BACKGROUND_WORK_NAME,
                                        ExistingWorkPolicy.KEEP,
                                        referentielWorker,
                                    )
                                    Toast.makeText(
                                        getApplication(),
                                        "Nouveau PEI synchronisé avec succès.",
                                        Toast.LENGTH_LONG,
                                    ).show()
                                    chargerNewPeiASynchro()
                                }
                            }

                            WorkInfo.State.FAILED -> {
                                _isLoading.value = false
                                val errorMessage = workInfo.outputData.getString(WorkerRemocra.OUTPUT_ERROR_MESSAGE)
                                    ?: "Erreur inconnue"
                                val errorMessageAvecPei = "PEI $peiId en échec : $errorMessage"

                                // Ne pas écraser une erreur déjà remontée par un worker précédent.
                                if (_errorMessageSynchro.value == null) {
                                    _errorMessageSynchro.value = errorMessageAvecPei
                                    Log.e("SyncNewPeiViewModel", "Erreur lors de la synchronisation : $errorMessageAvecPei")
                                    Toast.makeText(
                                        getApplication(),
                                        "Erreur lors de la synchronisation du nouveau PEI.",
                                        Toast.LENGTH_LONG,
                                    ).show()
                                }
                            }

                            else -> {}
                        }
                    }
                }
            }
        }
    }

    fun synchroniserTousNewPei(peiIds: List<UUID>) {
        if (peiIds.isEmpty()) {
            _errorMessageSynchro.value = "Aucun PEI à synchroniser"
            return
        }

        _errorMessageSynchro.value = null
        peiIds.forEach { peiId ->
            synchroniserNewPei(peiId)
        }
    }
}
