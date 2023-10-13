package fr.sdis83.remocra.mobile.viewmodels

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import fr.sdis83.remocra.mobile.database.Hydrant
import fr.sdis83.remocra.mobile.database.RemocraDatabase
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

    fun register(mapView: MapView) {
        this.mapView = mapView
    }

    private val hydrantDao = RemocraDatabase.getInstance(applicationContext).hydrantDao()

    val hydrantList = hydrantDao.getHydrantList()
    val newHydrantList = hydrantDao.getNewHydrantList()
    val tourneeList = hydrantDao.getTourneeMap()

    var showCenter = mutableStateOf(false)
        private set

    var mapCenter = mutableStateOf<IGeoPoint?>(null)
        private set

    var mapZoom = mutableStateOf<Double?>(null)
        private set

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

    fun goToHydrant(idhydrant: UUID) {
        CoroutineScope(Dispatchers.IO).launch {
            val hydrant = hydrantDao.getHydrantByIdHydrant(idhydrant)
            hydrant.let {
                setCenter(
                    HydrantGeoPoint(
                        it.lat,
                        it.lon,
                        UUID.randomUUID(),
                        it.code,
                        it.dispoTerrestre,
                        it.adresseComplete,
                        it.observation,
                        it.peiCaracteristiques,
                    ),
                )
                mapView?.setExpectedCenter(
                    HydrantGeoPoint(
                        it.lat,
                        it.lon,
                        it.idHydrant,
                        it.code,
                        it.dispoTerrestre,
                        it.adresseComplete,
                        it.observation,
                        it.peiCaracteristiques,
                    ),
                )
            }
        }
    }

    data class HydrantGeoPoint(
        val lat: Double,
        val lon: Double,
        val idHydrant: UUID,
        val numero: String?,
        val dispoTerrestre: Hydrant.Disponibilite?,
        val adresseComplete: String?,
        val observation: String?,
        val peiCaracteristiques: String?,
    ) :
        GeoPoint(lat, lon)
}
