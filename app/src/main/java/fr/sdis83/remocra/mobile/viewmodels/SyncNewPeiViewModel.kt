package fr.sdis83.remocra.mobile.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import fr.sdis83.remocra.mobile.database.RemocraDatabase
import fr.sdis83.remocra.mobile.database.SynchronisationDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SyncNewPeiViewModel(application: Application) : AndroidViewModel(application) {

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
}
