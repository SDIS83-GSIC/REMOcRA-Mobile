package fr.sdis83.remocra.mobile.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import fr.sdis83.remocra.mobile.database.Gestionnaire
import fr.sdis83.remocra.mobile.database.Hydrant
import fr.sdis83.remocra.mobile.database.RemocraDatabase
import fr.sdis83.remocra.mobile.database.TypeHydrantNature
import fr.sdis83.remocra.mobile.database.TypeHydrantNatureDeci
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

class HydrantCreateViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val TAG: String = "HydrantCreateViewModel"
    }

    private val hydrantDao = RemocraDatabase.getInstance(application).hydrantDao()
    private val referentielDao = RemocraDatabase.getInstance(application).referentielDao()

    val gestionnaireList: LiveData<List<Gestionnaire>> = referentielDao.getGestionnaireList()
    val typeHydrantNatureList: LiveData<List<TypeHydrantNature>> =
        referentielDao.getTypeHydrantNatureList()
    val typeHydrantNatureDeciList: LiveData<List<TypeHydrantNatureDeci>> =
        referentielDao.getTypeHydrantNatureDeciList()

    private val _hydrantCreateState = MutableStateFlow(
        HydrantForm(),
    )
    val hydrantCreateState: StateFlow<HydrantForm> =
        _hydrantCreateState.asStateFlow()

    suspend fun save() {
        _hydrantCreateState.value.toHydrant()?.let { hydrant ->
            hydrantDao.insertHydrant(hydrant)
        }
    }

    fun updateForm(hydrantForm: HydrantForm) {
        _hydrantCreateState.value = hydrantForm
    }

    data class HydrantForm(
        val nature: TypeHydrantNature? = null,
        val natureDeci: TypeHydrantNatureDeci? = null,
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

        fun toHydrant(): Hydrant? =
            if (!this.isValid) {
                null
            } else {
                Hydrant(
                    idHydrant = UUID.randomUUID(),
                    idRemocra = null,
                    idNature = this.nature!!.idRemocra,
                    idNatureDeci = this.natureDeci!!.idRemocra,
                    dispoHbe = null,
                    dispoTerrestre = null,
                    x = this.x!!,
                    y = this.y!!,
                    lon = this.lon!!,
                    lat = this.lat!!,
                    numero = "NEW",
                    code = "NEW",
                    idCommune = null,
                    complement = null,
                    voie = null,
                    voie2 = null,
                    suffixeVoie = null,
                    lieuDit = null,
                    observation = this.observation,
                    idGestionnaire = this.gestionnaire?.idGestionnaire,
                    idRemocraGestionnaire = this.gestionnaire?.idRemocra,
                )
            }
    }
}
