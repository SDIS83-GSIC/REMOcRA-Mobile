package fr.sdis83.remocra.mobile.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import fr.sdis83.remocra.mobile.database.HydrantVisite
import fr.sdis83.remocra.mobile.database.HydrantVisiteDao.HydrantVisiteWithAnomalies
import fr.sdis83.remocra.mobile.database.ReferentielDao
import fr.sdis83.remocra.mobile.database.RemocraDatabase
import fr.sdis83.remocra.mobile.database.TypeHydrantSaisie
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class HydrantVisiteViewModel(application: Application, idTournee: UUID, idHydrant: UUID) :
    AndroidViewModel(application) {

    private val hydrantVisiteDao = RemocraDatabase.getInstance(application).hydrantVisiteDao()
    private val referentielDao = RemocraDatabase.getInstance(application).referentielDao()

    val typeSaisieList: LiveData<List<TypeHydrantSaisie>> = referentielDao.getTypeSaisieList()
    val anomalieList: LiveData<List<ReferentielDao.AnomalieItem>> =
        referentielDao.getAnomalieItemList()

    private val _hydrantVisiteState = MutableStateFlow(
        HydrantVisiteWithAnomalies(
            hydrantVisite = HydrantVisite(
                idTournee = idTournee,
                idHydrant = idHydrant
            )
        )
    )
    val hydrantVisiteState: StateFlow<HydrantVisiteWithAnomalies> =
        _hydrantVisiteState.asStateFlow()

    suspend fun loadData(idTournee: UUID, idHydrant: UUID) {
        val hydrantVisite = hydrantVisiteDao.getCurrentVisite(
            idTournee = idTournee,
            idHydrant = idHydrant
        ) ?: HydrantVisite(
            idTournee = idTournee,
            idHydrant = idHydrant
        )

        _hydrantVisiteState.value = HydrantVisiteWithAnomalies(
            hydrantVisite = hydrantVisite,
            anomalies =
            if (hydrantVisite.hasAnomalieChanges)
                hydrantVisiteDao.getCurrentVisiteAnomalie(idHydrant, idTournee).toMutableList()
            else
                hydrantVisiteDao.getExistingVisiteAnomalie(idHydrant).toMutableList()
        )
    }

    init {
        viewModelScope.launch(Dispatchers.IO) {
            loadData(idTournee, idHydrant)
        }
    }

    suspend fun save(close: Boolean = false) {
        if (_hydrantVisiteState.value.hydrantVisite.isValid) {
            if (close) {
                _hydrantVisiteState.value =
                    _hydrantVisiteState.value.copy(
                        hydrantVisite = _hydrantVisiteState.value.hydrantVisite.copy(
                            statut = HydrantVisite.HydrantVisiteStatut.TERMINE
                        )
                    )
            }
            hydrantVisiteDao.upsertHydrantVisite(_hydrantVisiteState.value)
        }
    }

    fun updateForm(hydrantVisite: HydrantVisiteWithAnomalies) {
        _hydrantVisiteState.value = hydrantVisite
    }

    companion object {
        private const val TAG: String = "VisiteViewModel"
    }
}
