package fr.sdis83.remocra.mobile.viewmodels

import android.content.Context
import androidx.annotation.WorkerThread
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.sdis83.remocra.mobile.database.RemocraDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.osmdroid.api.IGeoPoint
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.simplefastpoint.SimpleFastPointOverlay
import org.osmdroid.views.overlay.simplefastpoint.SimplePointTheme
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

    var mapCenter = mutableStateOf<IGeoPoint?>(null)
        private set

    var mapZoom = mutableStateOf<Double?>(null)
        private set

    fun setCenter(center: IGeoPoint) {
        mapCenter.value = center
    }

    fun setZoom(zoom: Double) {
        mapZoom.value = zoom
    }

    fun goToHydrant(idhydrant: UUID) {
        CoroutineScope(Dispatchers.IO).launch {
            val hydrant = hydrantDao.getHydrantByIdHydrant(idhydrant)
            hydrant.let {
                setCenter(HydrantGeoPoint(it.lat, it.lon, UUID.randomUUID()))
                mapView?.setExpectedCenter(HydrantGeoPoint(it.lat, it.lon, it.idHydrant))
            }
        }
    }

    private var searchJob: Job? = null

    @WorkerThread
    fun getHydrantDebounced(
        hydrantLayer: MutableState<SimpleFastPointOverlay.PointAdapter>,
        box: BoundingBox,
    ) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(250)
            CoroutineScope(Dispatchers.IO).launch {
                hydrantLayer.value =
                    SimplePointTheme(
                        hydrantDao.getHydrantInBoundingBox(
                            box.latNorth,
                            box.latSouth,
                            box.lonWest,
                            box.lonEast,
                        ).map {
                            HydrantGeoPoint(it.lat, it.lon, it.idHydrant)
                        },
                    )
            }
        }
    }

    data class HydrantGeoPoint(val lat: Double, val lon: Double, val idHydrant: UUID) :
        GeoPoint(lat, lon)
}
