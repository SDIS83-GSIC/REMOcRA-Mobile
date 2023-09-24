package fr.sdis83.remocra.mobile.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import fr.sdis83.remocra.mobile.database.Hydrant
import fr.sdis83.remocra.mobile.database.HydrantVisite
import fr.sdis83.remocra.mobile.database.HydrantVisiteDao.HydrantVisiteWithAnomalies
import fr.sdis83.remocra.mobile.database.ReferentielDao
import fr.sdis83.remocra.mobile.database.RemocraDatabase
import fr.sdis83.remocra.mobile.database.TypeHydrantAnomalie
import fr.sdis83.remocra.mobile.database.TypeHydrantSaisie
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.launch
import java.util.UUID

class HydrantVisiteViewModel(application: Application, idTournee: UUID, idHydrant: UUID) :
    AndroidViewModel(application) {

    private val hydrantVisiteDao = RemocraDatabase.getInstance(application).hydrantVisiteDao()
    private val hydrantDao = RemocraDatabase.getInstance(application).hydrantDao()
    private val referentielDao = RemocraDatabase.getInstance(application).referentielDao()

    val typeSaisieList: LiveData<List<TypeHydrantSaisie>> = referentielDao.getTypeSaisieList()

    private val _hydrant: MutableStateFlow<Hydrant?> = MutableStateFlow(null)
    val hydrantState: StateFlow<Hydrant?> =
        _hydrant.asStateFlow()

    private lateinit var existingAnomalies: List<TypeHydrantAnomalie>

    private val _hydrantVisiteState = MutableStateFlow(
        HydrantVisiteWithAnomalies(
            hydrantVisite = HydrantVisite(
                idTournee = idTournee,
                idHydrant = idHydrant,
                hasAnomalieChanges = true,
            ),
        ),
    )
    val hydrantVisiteState: StateFlow<HydrantVisiteWithAnomalies> =
        _hydrantVisiteState.asStateFlow()

    val anomalieList: Flow<List<ReferentielDao.AnomalieItem>> =
        merge(hydrantVisiteState, hydrantState).flatMapLatest {
            if (hydrantVisiteState.value == null || hydrantState.value == null) {
                flowOf(listOf())
            } else {
                referentielDao.getAnomalieItemList(
                    hydrantVisiteState.value.hydrantVisite.idTypeHydrantSaisie,
                    hydrantState.value!!.idNature,
                )
            }
        }
    suspend fun loadData(idTournee: UUID, idHydrant: UUID) {
        _hydrant.value = hydrantDao.getHydrantByIdHydrant(idHydrant)
        existingAnomalies = hydrantVisiteDao.getExistingVisiteAnomalie(idHydrant)

        var loadAnomalies = false

        var hydrantVisite = hydrantVisiteDao.getCurrentVisite(
            idTournee = idTournee,
            idHydrant = idHydrant,
        )
        if (hydrantVisite == null) {
            hydrantVisite = HydrantVisite(
                idTournee = idTournee,
                idHydrant = idHydrant,
                hasAnomalieChanges = true,
            )
            loadAnomalies = true
        }

        _hydrantVisiteState.value = HydrantVisiteWithAnomalies(
            hydrantVisite = hydrantVisite,
            anomalies =
            if (loadAnomalies) {
                hydrantVisiteDao.getExistingVisiteAnomalie(idHydrant).toMutableList()
            } else if (hydrantVisite.hasAnomalieChanges) {
                hydrantVisiteDao.getCurrentVisiteAnomalie(idHydrant, idTournee).toMutableList()
            } else {
                hydrantVisiteDao.getExistingVisiteAnomalie(idHydrant).toMutableList()
            },
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
                            statut = HydrantVisite.HydrantVisiteStatut.TERMINE,
                        ),
                    )
            }
            hydrantVisiteDao.upsertHydrantVisite(_hydrantVisiteState.value)
        }
    }

    fun updateForm(hydrantVisite: HydrantVisiteWithAnomalies) {
        if (!hydrantVisite.hydrantVisite.hasAnomalieChanges) {
            _hydrantVisiteState.value =
                hydrantVisite.copy(anomalies = existingAnomalies.toMutableList())
        } else {
            _hydrantVisiteState.value = hydrantVisite
        }
    }

    companion object {
        private const val TAG: String = "VisiteViewModel"
    }
}
