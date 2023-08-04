package fr.sdis83.remocra.mobile.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.GpsNotFixed
import androidx.compose.material.icons.filled.ZoomInMap
import androidx.compose.material.icons.filled.ZoomOutMap
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import fr.sdis83.remocra.mobile.MapViewState
import fr.sdis83.remocra.mobile.R
import fr.sdis83.remocra.mobile.viewmodels.MapViewModel
import org.osmdroid.api.IGeoPoint
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import org.osmdroid.views.overlay.simplefastpoint.SimpleFastPointOverlay
import org.osmdroid.views.overlay.simplefastpoint.SimpleFastPointOverlay.PointAdapter
import org.osmdroid.views.overlay.simplefastpoint.SimplePointTheme

@Composable
fun MapView(
    mapViewModel: MapViewModel,
    mapViewState: MutableState<MapViewState>,
    modifier: Modifier = Modifier,
    onLoad: ((map: MapView) -> Unit)? = null
) {
    val context = LocalContext.current

    val mapState =
        rememberMapViewWithLifecycle(mapViewModel.mapCenter.value, mapViewModel.mapZoom.value)

    mapViewModel.register(mapState)

    val hydrantPoints = remember {
        mutableStateOf<PointAdapter>(
            SimplePointTheme(
                emptyList()
            )
        )
    }
    val iwOverlay = Marker(mapState)

    val hydrantLayer = SimpleFastPointOverlay(
        hydrantPoints.value
    ).apply {
        setOnClickListener { points, point ->
            val selected = points.get(point) as MapViewModel.HydrantGeoPoint
            iwOverlay.position = selected
            iwOverlay.title = selected.idHydrant.toString()
            iwOverlay.showInfoWindow()
        }
    }

    val locationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(context), mapState).apply {
        enableMyLocation()
        isOptionsMenuEnabled = true
    }

    mapState.overlays.addAll(
        listOf(
            locationOverlay,
            hydrantLayer,
            iwOverlay
        )
    )

    mapState.addMapListener(
        object : MapListener {
            override fun onScroll(event: ScrollEvent?): Boolean {
                mapViewModel.setCenter(mapState.mapCenter)
                mapViewModel.getHydrantDebounced(
                    hydrantPoints,
                    mapState.boundingBox.increaseByScale(2f)
                )
                return true
            }

            override fun onZoom(event: ZoomEvent?): Boolean {
                mapViewModel.setZoom(mapState.zoomLevelDouble)
                return true
            }
        }
    )

    val icon =
        if (mapViewState.value.isFullscreen)
            Icons.Filled.ZoomInMap
        else
            Icons.Filled.ZoomOutMap

    Box {
        AndroidView(
            { mapState },
            modifier
        ) { mapView -> onLoad?.invoke(mapView) }
        Column(
            Modifier
                .padding(16.dp)
                .align(Alignment.BottomStart)
        ) {
            FloatingActionButton(
                onClick = {
                    if (!locationOverlay.isFollowLocationEnabled) {
                        locationOverlay.enableFollowLocation();
                    } else {
                        locationOverlay.disableFollowLocation();
                    }
                },
                shape = CircleShape
            ) {
                Icon(
                    imageVector = if (!locationOverlay.isFollowLocationEnabled) {
                        Icons.Filled.GpsNotFixed
                    } else {
                        Icons.Filled.GpsFixed
                    }, contentDescription = "GPS"
                )
            }
            Spacer(Modifier.height(16.dp))
            FloatingActionButton(
                onClick = {
                    mapViewState.value = mapViewState.value.copy(
                        showMapView = mapViewState.value.showMapView,
                        isFullscreen = !mapViewState.value.isFullscreen
                    )
                },
                shape = CircleShape
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = "Fullscreen"
                )
            }
        }
    }
}

@Composable
fun rememberMapViewWithLifecycle(center: IGeoPoint?, zoom: Double?): MapView {
    val context = LocalContext.current
    val mapView = remember {
        MapView(context).apply {
            id = R.id.map_layout
            clipToOutline = true
            setTileSource(TileSourceFactory.MAPNIK)
            controller.setCenter(center ?: GeoPoint(48.8638061, 2.32293013))
            controller.setZoom(zoom ?: 19.0)
        }
    }

    Configuration.getInstance().userAgentValue = "fr.sdis83.remocra.mobile"

    val lifecycleObserver = rememberMapLifecycleObserver(mapView)
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle) {
        lifecycle.addObserver(lifecycleObserver)
        onDispose {
            lifecycle.removeObserver(lifecycleObserver)
        }
    }

    return mapView
}

@Composable
fun rememberMapLifecycleObserver(mapView: MapView): LifecycleEventObserver =
    remember(mapView) {
        LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                else -> {}
            }
        }
    }
