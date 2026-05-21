package fr.sdis83.remocra.mobile.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import fr.sdis83.remocra.mobile.database.RemocraDatabase
import fr.sdis83.remocra.mobile.database.TourneesDao
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
            // TODO
        }
    }
}
