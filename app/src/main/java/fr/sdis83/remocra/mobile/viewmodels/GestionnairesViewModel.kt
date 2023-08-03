package fr.sdis83.remocra.mobile.viewmodels

import android.app.Application
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import fr.sdis83.remocra.mobile.database.Contact
import fr.sdis83.remocra.mobile.database.Gestionnaire
import fr.sdis83.remocra.mobile.database.RemocraDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class GestionnairesViewModel(application: Application, idGestionnaire: UUID?) : AndroidViewModel(application) {

    companion object {
        private const val TAG: String = "GestionnairesViewModel"
    }

    val gestionnairesDao= RemocraDatabase.getInstance(application).gestionnairesDao()
    val gestionnairesList: LiveData<List<Gestionnaire>> = gestionnairesDao.getGestionnairesList()
    val gestionnaire: LiveData<Gestionnaire?> = gestionnairesDao.getCurrentGestionnaireByUUID(idGestionnaire)
    val contactsList: LiveData<List<Contact>> = gestionnairesDao.getContactByGestionnaireUUID(idGestionnaire)

    suspend fun upsertGestionnaire(gestionnaire: Gestionnaire){
        gestionnairesDao.upsertGestionnaire(gestionnaire.copy(edited = true))
    }

    private val _gestionnaireState = MutableStateFlow(
        Gestionnaire(
            idRemocra = null,
            nom = null,
            code = null,
            actif = true
        )
    )

    var gestionnaireState: StateFlow<Gestionnaire> = _gestionnaireState.asStateFlow()
    var gestionnaireValidState: MutableState<GestionnaireValidation>
        = mutableStateOf(GestionnaireValidation(false))

    suspend fun loadData(idGestionnaire: UUID?){
        if (idGestionnaire != null) {
            _gestionnaireState.value = gestionnairesDao.getGestionnaireByUUID(idGestionnaire)
        } else {
            _gestionnaireState.value = Gestionnaire(
                idRemocra = null,
                nom = null,
                code = null,
                actif = true
            )
        }
    }
    init {
        viewModelScope.launch(Dispatchers.IO) {
            loadData(idGestionnaire)
        }
    }

    data class GestionnaireValidation(val isNomValid: Boolean){
        val isValid:Boolean = isNomValid
    }

    fun formValidation(){
        gestionnaireValidState.value = GestionnaireValidation(
            isNomValid = !_gestionnaireState.value.nom.isNullOrEmpty()
        )
    }

    fun updateForm(gestionnaire: Gestionnaire) {
        _gestionnaireState.value = gestionnaire
        formValidation()
    }
}