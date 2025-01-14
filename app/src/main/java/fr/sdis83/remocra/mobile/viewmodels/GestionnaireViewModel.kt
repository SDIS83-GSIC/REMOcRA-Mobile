package fr.sdis83.remocra.mobile.viewmodels

import android.app.Application
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import fr.sdis83.remocra.mobile.database.Contact
import fr.sdis83.remocra.mobile.database.FonctionContact
import fr.sdis83.remocra.mobile.database.Gestionnaire
import fr.sdis83.remocra.mobile.database.RemocraDatabase
import fr.sdis83.remocra.mobile.workers.JsonNewGestionnaireWorker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class GestionnaireViewModel(application: Application, gestionnaireId: UUID?) : AndroidViewModel(application) {

    companion object {
        private const val TAG: String = "GestionnairesViewModel"
    }

    val gestionnairesDao = RemocraDatabase.getInstance(application).gestionnairesDao()
    val contactDao = RemocraDatabase.getInstance(application).contactsDao()
    val gestionnaire: LiveData<Gestionnaire?> = gestionnairesDao.getCurrentGestionnaireByUUID(gestionnaireId)
    val contactsList: LiveData<List<Contact>> = gestionnairesDao.getContactByGestionnaireUUID(gestionnaireId)
    val fonctionContactList: LiveData<List<FonctionContact>> = contactDao.getFonctionContact()

    suspend fun upsertGestionnaire(gestionnaire: Gestionnaire) {
        gestionnairesDao.upsertGestionnaire(gestionnaire.copy(edited = true))

        val jsonNewGestionnaireWorker = OneTimeWorkRequestBuilder<JsonNewGestionnaireWorker>().build()
        WorkManager.getInstance(getApplication()).beginWith(jsonNewGestionnaireWorker)
            .enqueue()
    }

    private val _gestionnaireState = MutableStateFlow(
        Gestionnaire(
            gestionnaireId = UUID.randomUUID(),
            gestionnaireLibelle = "",
            gestionnaireCode = "",
            gestionnaireActif = true,
        ),
    )

    var gestionnaireState: StateFlow<Gestionnaire> = _gestionnaireState.asStateFlow()
    var gestionnaireValidState: MutableState<GestionnaireValidation> =
        mutableStateOf(GestionnaireValidation(false))

    suspend fun loadData(gestionnaireId: UUID?) {
        if (gestionnaireId != null) {
            _gestionnaireState.value = gestionnairesDao.getGestionnaireByUUID(gestionnaireId)
        } else {
            _gestionnaireState.value = Gestionnaire(
                gestionnaireId = UUID.randomUUID(),
                gestionnaireLibelle = "",
                gestionnaireCode = "",
                gestionnaireActif = true,
            )
        }
    }
    init {
        viewModelScope.launch(Dispatchers.IO) {
            loadData(gestionnaireId)
        }
    }

    data class GestionnaireValidation(val isNomValid: Boolean) {
        val isValid: Boolean = isNomValid
    }

    fun formValidation() {
        gestionnaireValidState.value = GestionnaireValidation(
            isNomValid = !_gestionnaireState.value.gestionnaireLibelle.isNullOrEmpty(),
        )
    }

    fun updateForm(gestionnaire: Gestionnaire) {
        _gestionnaireState.value = gestionnaire
        formValidation()
    }
}
