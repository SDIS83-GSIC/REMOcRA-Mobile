package fr.sdis83.remocra.mobile.viewmodels

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import fr.sdis83.remocra.mobile.database.Pei
import fr.sdis83.remocra.mobile.database.RemocraDatabase
import fr.sdis83.remocra.mobile.database.Visite
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.osmdroid.api.IGeoPoint
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import java.util.UUID

class MapViewModel(applicationContext: Context) : ViewModel() {
    companion object {
        private const val TAG = "MapViewModel"
    }

    private var mapView: MapView? = null

    var peiTourneeSelected: MutableLiveData<PeiGeoPoint?> = MutableLiveData()
    var peiNewSelected: MutableLiveData<PeiGeoPoint?> = MutableLiveData()

    fun register(mapView: MapView) {
        this.mapView = mapView
    }

    private val peiDao = RemocraDatabase.getInstance(applicationContext).peiDao()

    val peiList = peiDao.getPeiList()
    val newPeiList = peiDao.getNewPeiList()
    val tourneeList = peiDao.getTourneeMap()

    var showCenter = mutableStateOf(false)
        private set

    var mapCenter = mutableStateOf<IGeoPoint?>(null)
        private set

    var mapZoom = mutableStateOf<Double?>(null)
        private set

    var affichageIndispo = mutableStateOf(false)
        private set

    var affichageSymbolesNormalises = mutableStateOf(false)
        private set

    var peiEnDeplacement = mutableStateOf<PeiGeoPoint?>(null)
        private set

    fun setAffichageIndispo(affichageIndispo_new: Boolean) {
        affichageIndispo.value = affichageIndispo_new
    }

    fun setAffichageSymbolesNormalises(value: Boolean) {
        affichageSymbolesNormalises.value = value
    }

    fun showCenter(show: Boolean) {
        showCenter.value = show
    }

    fun setCenter(center: IGeoPoint) {
        mapCenter.value = center
    }

    fun setZoom(zoom: Double) {
        mapZoom.value = zoom
    }

    fun scaleToBox(boundingBox: BoundingBox) {
        mapView?.zoomToBoundingBox(boundingBox, true)
    }

    fun goToPei(idPei: UUID, isNew: Boolean) {
        CoroutineScope(Dispatchers.IO).launch {
            val pei = peiDao.getPeiGeoPointByIdPei(idPei)
            pei.let {
                setCenter(
                    PeiGeoPoint(
                        it.lat,
                        it.lon,
                        UUID.randomUUID(),
                        it.peiNumeroComplet,
                        it.dispoTerrestre,
                        it.adresseComplete,
                        it.observation,
                        it.peiCaracteristiques,
                        it.statutVisite,
                        it.tourneeId,
                        it.natureCode,
                    ),
                )
                mapView?.setExpectedCenter(
                    PeiGeoPoint(
                        it.lat,
                        it.lon,
                        it.peiId,
                        it.peiNumeroComplet,
                        it.dispoTerrestre,
                        it.adresseComplete,
                        it.observation,
                        it.peiCaracteristiques,
                        it.statutVisite,
                        it.tourneeId,
                        it.natureCode,
                    ),
                )
                if (isNew) {
                    peiTourneeSelected.postValue(null)
                    peiNewSelected.postValue(pei)
                } else {
                    peiNewSelected.postValue(null)
                    peiTourneeSelected.postValue(pei)
                }
            }
        }
    }

    fun commencerDeplacementPei(pei: PeiGeoPoint) {
        peiEnDeplacement.value = pei
    }

    fun annulerDeplacementPei() {
        peiEnDeplacement.value = null
    }

    fun updatePeiPosition(peiId: UUID, lat: Double, lon: Double, x: Double, y: Double) {
        CoroutineScope(Dispatchers.IO).launch {
            peiDao.updateDeplacementPei(
                lat = lat,
                lon = lon,
                x = x,
                y = y,
                peiId = peiId,
            )
        }
    }

    data class PeiGeoPoint(
        val lat: Double,
        val lon: Double,
        val peiId: UUID,
        val peiNumeroComplet: String?,
        val dispoTerrestre: Pei.Disponibilite?,
        val adresseComplete: String?,
        val observation: String?,
        val peiCaracteristiques: String?,
        val statutVisite: Visite.VisiteStatut?,
        val tourneeId: UUID? = null,
        val natureCode: String,
    ) :
        GeoPoint(lat, lon)
}
