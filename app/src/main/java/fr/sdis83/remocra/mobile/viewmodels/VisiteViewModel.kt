package fr.sdis83.remocra.mobile.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import fr.sdis83.remocra.mobile.database.Agent
import fr.sdis83.remocra.mobile.database.Anomalie
import fr.sdis83.remocra.mobile.database.Pei
import fr.sdis83.remocra.mobile.database.ReferentielDao
import fr.sdis83.remocra.mobile.database.RemocraDatabase
import fr.sdis83.remocra.mobile.database.TypeVisite
import fr.sdis83.remocra.mobile.database.Visite
import fr.sdis83.remocra.mobile.database.VisiteDao.VisiteWithAnomalies
import fr.sdis83.remocra.mobile.utils.GlobalConstants
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

class VisiteViewModel(application: Application, tourneeId: UUID, peiId: UUID, gestionAgents: String?) :
    AndroidViewModel(application) {

    private val visiteDao = RemocraDatabase.getInstance(application).visiteDao()
    private val peiDao = RemocraDatabase.getInstance(application).peiDao()
    private val referentielDao = RemocraDatabase.getInstance(application).referentielDao()
    private val agentDao = RemocraDatabase.getInstance(application).agentDao()

    val typeVisiteList: LiveData<List<TypeVisite>> = referentielDao.getTypeVisiteList()

    private val _pei: MutableStateFlow<Pei?> = MutableStateFlow(null)
    val peiState: StateFlow<Pei?> =
        _pei.asStateFlow()

    private lateinit var existingAnomalies: List<Anomalie>

    private val _visiteState = MutableStateFlow(
        VisiteWithAnomalies(
            visite = Visite(
                visiteId = UUID.randomUUID(),
                tourneeId = tourneeId,
                peiId = peiId,
                hasAnomalieChanges = true,
            ),
            numeroPei = peiState.value?.peiNumeroComplet,
        ),
    )
    val visiteState: StateFlow<VisiteWithAnomalies> =
        _visiteState.asStateFlow()

    val anomalieList: Flow<List<ReferentielDao.AnomalieItem>> =
        merge(visiteState, peiState).flatMapLatest {
            if (visiteState.value == null || peiState.value == null) {
                flowOf(listOf())
            } else {
                referentielDao.getAnomalieItemList(
                    visiteState.value.visite.typeVisiteId,
                    peiState.value!!.natureId,
                )
            }
        }

    suspend fun loadData(tourneeId: UUID, peiId: UUID, gestionAgents: String?) {
        _pei.value = peiDao.getPeiByPeiId(peiId)
        existingAnomalies = visiteDao.getExistingVisiteAnomalie(peiId)

        var loadAnomalies = false

        var visite = visiteDao.getCurrentVisite(
            tourneeId = tourneeId,
            peiId = peiId,
        )

        // Gestion des agents
        var agent1: String? = null
        var agent2: String? = null

        if (gestionAgents == GlobalConstants.UTILISATEUR_CONNECTE_OBLIGATOIRE ||
            gestionAgents == GlobalConstants.UTILISATEUR_CONNECTE
        ) {
            agent1 = agentDao.getUtilisateurConnecte()
        } else if (gestionAgents == GlobalConstants.VALEUR_PRECEDENTE) {
            agent1 = agentDao.getAgent1ValeurPrecedente()
            agent2 = agentDao.getAgent2ValeurPrecedente()
        }

        if (visite == null) {
            visite = Visite(
                visiteId = UUID.randomUUID(),
                tourneeId = tourneeId,
                peiId = peiId,
                hasAnomalieChanges = true,
                agent1 = agent1,
                agent2 = agent2,
            )
            loadAnomalies = true
        }

        _visiteState.value = VisiteWithAnomalies(
            visite = visite,
            anomalies =
            if (loadAnomalies) {
                visiteDao.getExistingVisiteAnomalie(peiId).toMutableList()
            } else if (visite.hasAnomalieChanges) {
                visiteDao.getCurrentVisiteAnomalie(peiId, tourneeId).toMutableList()
            } else {
                visiteDao.getExistingVisiteAnomalie(peiId).toMutableList()
            },
            numeroPei = peiState.value?.peiNumeroComplet,
        )
    }

    init {
        viewModelScope.launch(Dispatchers.IO) {
            loadData(tourneeId, peiId, gestionAgents)
        }
    }

    suspend fun save(close: Boolean = false) {
        if (_visiteState.value.visite.isValid) {
            if (close) {
                _visiteState.value =
                    _visiteState.value.copy(
                        visite = _visiteState.value.visite.copy(
                            statut = Visite.VisiteStatut.TERMINE,
                        ),
                    )
            }
            visiteDao.upsertVisite(_visiteState.value)

            // On save les agents aussi
            val agent1 = _visiteState.value.visite.agent1
            val agent2 = _visiteState.value.visite.agent2

            upsertAgent(agent1, 1)
            upsertAgent(agent2, 2)
        }
    }

    private suspend fun upsertAgent(agentRenseigne: String?, numeroAgent: Int) {
        if (agentRenseigne != null) {
            // on regarde si l'agent 1 existe en base
            val agent = agentDao.checkIfExist(agentRenseigne, numeroAgent)
            agentDao.updateLastValue(numeroAgent)
            if (agent == null) {
                agentDao.insertAgent(
                    Agent(
                        UUID.randomUUID(),
                        nomAgent = agentRenseigne,
                        numeroAgent = numeroAgent,
                        isUserConnecte = false,
                        isLastValue = true,
                    ),
                )
            } else {
                // on met à jour la dernère valeur
                agentDao.updateAgent(agent.copy(isLastValue = true))
            }
        }
    }

    fun updateForm(visite: VisiteWithAnomalies) {
        if (!visite.visite.hasAnomalieChanges) {
            _visiteState.value =
                visite.copy(anomalies = existingAnomalies.toMutableList())
        } else {
            _visiteState.value = visite
        }
    }

    companion object {
        private const val TAG: String = "VisiteViewModel"
    }
}
