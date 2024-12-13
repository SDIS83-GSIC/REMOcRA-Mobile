package fr.sdis83.remocra.mobile.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import fr.sdis83.remocra.mobile.database.Gestionnaire
import fr.sdis83.remocra.mobile.database.Nature
import fr.sdis83.remocra.mobile.database.NatureDeci
import fr.sdis83.remocra.mobile.database.Pei
import fr.sdis83.remocra.mobile.database.RemocraDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

class PeiCreateViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val TAG: String = "PeiCreateViewModel"
    }

    private val peiDao = RemocraDatabase.getInstance(application).peiDao()
    private val referentielDao = RemocraDatabase.getInstance(application).referentielDao()

    val gestionnaireList: LiveData<List<Gestionnaire>> = referentielDao.getGestionnaireList()
    val natureList: LiveData<List<Nature>> =
        referentielDao.getNatureList()
    val natureDeciList: LiveData<List<NatureDeci>> =
        referentielDao.getNatureDeciList()

    private val _peiCreateState = MutableStateFlow(
        PeiForm(),
    )
    val peiCreateState: StateFlow<PeiForm> =
        _peiCreateState.asStateFlow()

    suspend fun save() {
        val code = _peiCreateState.value.nature!!.typePeiId
        _peiCreateState.value.toPei(peiDao.getLatestCreated().plus(1).toString(), code)?.let { hydrant ->
            peiDao.insertPei(hydrant)
        }
    }

    fun updateForm(peiForm: PeiForm) {
        _peiCreateState.value = peiForm
    }

    data class PeiForm(
        val nature: Nature? = null,
        val natureDeci: NatureDeci? = null,
        val x: Double? = null,
        val y: Double? = null,
        val lon: Double? = null,
        val lat: Double? = null,
        val observation: String? = null,
        val gestionnaire: Gestionnaire? = null,

    ) {
        val isValid: Boolean
            get() =
                this.nature != null &&
                    this.natureDeci != null &&
                    this.x != null &&
                    this.y != null &&
                    this.lon != null &&
                    this.lat != null

        fun toPei(numero: String, typePeiId: UUID): Pei? =
            if (!this.isValid) {
                null
            } else {
                Pei(
                    peiId = UUID.randomUUID(),
                    natureId = this.nature!!.natureId,
                    natureDeciId = this.natureDeci!!.natureDeciId,
                    dispoHbe = null,
                    dispoTerrestre = null,
                    x = this.x!!,
                    y = this.y!!,
                    lon = this.lon!!,
                    lat = this.lat!!,
                    peiNumeroComplet = "$numero",
                    typePeiId = typePeiId,
                    adresseComplete = null,
                    observation = this.observation,
                    gestionnaireId = this.gestionnaire?.gestionnaireId,
                    peiCaracteristiques = null,
                    isNew = true,
                )
            }
    }
}
