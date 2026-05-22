package fr.sdis83.remocra.mobile.viewmodels

import android.app.Application
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import fr.sdis83.remocra.mobile.database.RemocraDatabase
import fr.sdis83.remocra.mobile.database.TourneesDao
import fr.sdis83.remocra.mobile.synchronisation.SynchroDeplacementPeiByTourneeWorker
import fr.sdis83.remocra.mobile.synchronisation.SynchroOneTourneeFinWorker
import fr.sdis83.remocra.mobile.synchronisation.SynchroOneTourneeWorker
import fr.sdis83.remocra.mobile.synchronisation.SynchroVisiteAnomalieByTourneeWorker
import fr.sdis83.remocra.mobile.synchronisation.SynchroVisiteByTourneeWorker
import fr.sdis83.remocra.mobile.workers.WorkerRemocra
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

class SyncTourneeViewModel(application: Application) : AndroidViewModel(application) {

    private val synchronisationDao = RemocraDatabase.getInstance(getApplication()).synchronisationDao()

    private val tourneesSynchro = MutableStateFlow<List<TourneesDao.TourneeAvancement>>(emptyList())
    val _tourneesSynchro: StateFlow<List<TourneesDao.TourneeAvancement>> = tourneesSynchro.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _errorMessageSynchro = MutableStateFlow<String?>(null)
    val errorMessageSynchro: StateFlow<String?> = _errorMessageSynchro.asStateFlow()

    /**
     * Charge les tournées actuellement marquées comme réservées en local.
     */
    fun chargerTourneesASynchroniser() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                val tournees = withContext(Dispatchers.IO) {
                    synchronisationDao.getAllTourneeReservees()
                }
                tourneesSynchro.value = tournees
            } catch (exception: Exception) {
                _errorMessage.value = exception.message ?: "Erreur inconnue"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Point d'entrée pour déclencher la synchro de la tournée sélectionnée.
     */
    fun synchroniserTourneeReservee(tourneeId: UUID) {
        viewModelScope.launch {
            val dataBuilder = Data.Builder()
                .putString("tourneeId", tourneeId.toString()).build()
            val synchroTourneeWorker = OneTimeWorkRequestBuilder<SynchroOneTourneeWorker>()
                .setInputData(dataBuilder)
                .build()

            val synchroDeplacement = OneTimeWorkRequestBuilder<SynchroDeplacementPeiByTourneeWorker>()
                .setInputData(dataBuilder)
                .build()

            val synchroVisiteWorker = OneTimeWorkRequestBuilder<SynchroVisiteByTourneeWorker>()
                .setInputData(dataBuilder)
                .build()

            val synchroVisiteAnomalieWorker =
                OneTimeWorkRequestBuilder<SynchroVisiteAnomalieByTourneeWorker>()
                    .setInputData(dataBuilder)
                    .build()

            val synchroTourneeFinWorker =
                OneTimeWorkRequestBuilder<SynchroOneTourneeFinWorker>()
                    .setInputData(dataBuilder)
                    .build()

            WorkManager.getInstance(getApplication()).let { workManager ->
                workManager
                    .beginWith(synchroTourneeWorker)
                    .then(synchroDeplacement)
                    .then(synchroVisiteWorker)
                    .then(synchroVisiteAnomalieWorker)
                    .then(synchroTourneeFinWorker)
                    .enqueue()

                // Observer tous les workers pour capturer les erreurs de chacun
                val workersToObserve = listOf(
                    synchroTourneeWorker,
                    synchroDeplacement,
                    synchroVisiteWorker,
                    synchroVisiteAnomalieWorker,
                    synchroTourneeFinWorker,
                )

                workersToObserve.forEach { worker ->
                    workManager.getWorkInfoByIdLiveData(worker.id).observeForever { workInfo ->
                        when (workInfo.state) {
                            WorkInfo.State.RUNNING -> {
                                if (worker == synchroTourneeWorker) {
                                    _isLoading.value = true
                                    Toast.makeText(
                                        getApplication(),
                                        "Synchronisation de la tournée en cours...",
                                        Toast.LENGTH_LONG,
                                    ).show()
                                }
                            }

                            WorkInfo.State.SUCCEEDED -> {
                                // Afficher le succès seulement si c'est le dernier worker
                                if (worker == synchroTourneeFinWorker) {
                                    _errorMessageSynchro.value = null
                                    _isLoading.value = false
                                    Toast.makeText(
                                        getApplication(),
                                        "Tournée synchronisée avec succès.",
                                        Toast.LENGTH_LONG,
                                    ).show()
                                    // Recharger les tournées après succès pour refléter l'état final
                                    chargerTourneesASynchroniser()
                                }
                            }

                            WorkInfo.State.FAILED -> {
                                _isLoading.value = false
                                val errorMessage = workInfo.outputData.getString(WorkerRemocra.OUTPUT_ERROR_MESSAGE)
                                    ?: "Erreur inconnue"

                                // Ne pas écraser un message d'erreur déjà positionné par un worker précédent
                                if (_errorMessageSynchro.value == null) {
                                    _errorMessageSynchro.value = errorMessage
                                    Log.e("SyncTourneeViewModel", "Erreur lors de la synchronisation: $errorMessage")
                                    Toast.makeText(
                                        getApplication(),
                                        "Erreur inconnue pendant la synchronisation de la tournée",
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
}
