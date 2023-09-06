package fr.sdis83.remocra.mobile.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import fr.sdis83.remocra.mobile.database.Gestionnaire
import fr.sdis83.remocra.mobile.database.RemocraDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest

class GestionnaireListViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val TAG: String = "GestionnaireListViewModel"
    }

    private val _search: MutableStateFlow<String> = MutableStateFlow("")
    val search: StateFlow<String> = _search.asStateFlow()

    val gestionnairesDao = RemocraDatabase.getInstance(application).gestionnairesDao()
    val gestionnairesList: Flow<List<Gestionnaire>> = search.flatMapLatest {
        gestionnairesDao.getGestionnairesList(it)
    }

    fun doSearch(search: String) {
        _search.value = search
    }
}
