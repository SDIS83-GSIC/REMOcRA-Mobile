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
import fr.sdis83.remocra.mobile.synchronisation.SynchroContactRoleWorker
import fr.sdis83.remocra.mobile.synchronisation.SynchroContactWorker
import fr.sdis83.remocra.mobile.synchronisation.SynchroGestionnaireFinWorker
import fr.sdis83.remocra.mobile.synchronisation.SynchroGestionnaireWorker
import fr.sdis83.remocra.mobile.workers.ReferentielWorker
import fr.sdis83.remocra.mobile.workers.WorkerRemocra
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

class SyncGestionnaireViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val INPUT_GESTIONNAIRE_ID = "gestionnaireId"
        private const val REFERENTIEL_BACKGROUND_WORK_NAME = "ReferentielBackgroundWork"
    }

    private val synchronisationDao = RemocraDatabase.getInstance(getApplication()).synchronisationDao()

    private val gestionnaireASynchro = MutableStateFlow<List<SynchronisationDao.GestionnaireWithContactRow>>(emptyList())
    val _gestionnaireASynchro: StateFlow<List<SynchronisationDao.GestionnaireWithContactRow>> = gestionnaireASynchro.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _errorMessageSynchro = MutableStateFlow<String?>(null)
    val errorMessageSynchro: StateFlow<String?> = _errorMessageSynchro.asStateFlow()

    /**
     * Charge les gestionnaires et leurs contacts qui ont été modifiés ou créés.
     */
    fun chargerGestionnaireASynchro() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                val gestionnairesWithContact = withContext(Dispatchers.IO) {
                    synchronisationDao.getAllGestionnaireWithContact()
                }
                gestionnaireASynchro.value = gestionnairesWithContact
            } catch (exception: Exception) {
                _errorMessage.value = exception.message ?: "Erreur inconnue"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun synchroniserGestionnaire(
        gestionnaireId: UUID,
    ) {
        viewModelScope.launch {
            val data = Data.Builder()
                .putString(INPUT_GESTIONNAIRE_ID, gestionnaireId.toString())
                .build()

            val gestionnaireWorker = OneTimeWorkRequestBuilder<SynchroGestionnaireWorker>()
                .setInputData(data)
                .build()
            val contactWorker = OneTimeWorkRequestBuilder<SynchroContactWorker>()
                .setInputData(data)
                .build()
            val contactRoleWorker = OneTimeWorkRequestBuilder<SynchroContactRoleWorker>()
                .setInputData(data)
                .build()
            val synchroGestionnaireFinWorker = OneTimeWorkRequestBuilder<SynchroGestionnaireFinWorker>()
                .setInputData(data)
                .build()

            val referentielWorker = OneTimeWorkRequestBuilder<ReferentielWorker>()
                .build()

            WorkManager.getInstance(getApplication()).let { workManager ->
                _isLoading.value = true

                workManager
                    .beginWith(gestionnaireWorker)
                    .then(contactWorker)
                    .then(contactRoleWorker)
                    .then(synchroGestionnaireFinWorker)
                    .enqueue()

                val workersToObserve = listOf(
                    gestionnaireWorker,
                    contactWorker,
                    contactRoleWorker,
                    synchroGestionnaireFinWorker,
                )

                workersToObserve.forEach { worker ->
                    workManager.getWorkInfoByIdLiveData(worker.id).observeForever { workInfo ->
                        when (workInfo.state) {
                            WorkInfo.State.RUNNING -> {
                                if (worker == gestionnaireWorker) {
                                    Toast.makeText(
                                        getApplication(),
                                        "Synchronisation du gestionnaire en cours...",
                                        Toast.LENGTH_SHORT,
                                    ).show()
                                }
                            }

                            WorkInfo.State.SUCCEEDED -> {
                                if (worker == synchroGestionnaireFinWorker) {
                                    _isLoading.value = false
                                    _errorMessageSynchro.value = null
                                    workManager.enqueueUniqueWork(
                                        REFERENTIEL_BACKGROUND_WORK_NAME,
                                        ExistingWorkPolicy.KEEP,
                                        referentielWorker,
                                    )
                                    Toast.makeText(
                                        getApplication(),
                                        "Gestionnaire synchronisé avec succès.",
                                        Toast.LENGTH_SHORT,
                                    ).show()
                                    chargerGestionnaireASynchro()
                                }
                            }

                            WorkInfo.State.FAILED -> {
                                _isLoading.value = false
                                val errorMessage = workInfo.outputData.getString(WorkerRemocra.OUTPUT_ERROR_MESSAGE)
                                    ?: "Erreur inconnue"
                                val errorAvecContexte = "Gestionnaire $gestionnaireId en échec : $errorMessage"

                                if (_errorMessageSynchro.value == null) {
                                    _errorMessageSynchro.value = errorAvecContexte
                                    Log.e("SyncGestionnaireViewModel", "Erreur lors de la synchronisation: $errorAvecContexte")
                                }
                            }

                            WorkInfo.State.CANCELLED -> {
                                _isLoading.value = false
                                if (_errorMessageSynchro.value == null) {
                                    _errorMessageSynchro.value = "Synchronisation annulée pour le gestionnaire $gestionnaireId"
                                }
                            }

                            else -> Unit
                        }
                    }
                }
            }
        }
    }

    fun synchroniserTousGestionnaires(gestionnaireIds: List<UUID>) {
        if (gestionnaireIds.isEmpty()) {
            _errorMessageSynchro.value = "Aucun gestionnaire à synchroniser"
            return
        }
        _errorMessageSynchro.value = null
        gestionnaireIds.forEach { synchroniserGestionnaire(it) }
    }
}
