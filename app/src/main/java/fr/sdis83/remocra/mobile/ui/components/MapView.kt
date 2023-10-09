package fr.sdis83.remocra.mobile.ui.components

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.IntentSender
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.LocationManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.GpsNotFixed
import androidx.compose.material.icons.filled.GpsOff
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomInMap
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material.icons.filled.ZoomOutMap
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsResponse
import com.google.android.gms.location.SettingsClient
import com.google.android.gms.tasks.Task
import fr.sdis83.remocra.mobile.MapViewState
import fr.sdis83.remocra.mobile.R
import fr.sdis83.remocra.mobile.database.Tournee
import fr.sdis83.remocra.mobile.utils.GlobalConstants
import fr.sdis83.remocra.mobile.viewmodels.MapViewModel
import org.osmdroid.api.IGeoPoint
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.infowindow.InfoWindow
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import org.osmdroid.views.overlay.simplefastpoint.SimpleFastPointOverlay
import org.osmdroid.views.overlay.simplefastpoint.SimpleFastPointOverlayOptions
import org.osmdroid.views.overlay.simplefastpoint.SimplePointTheme

@Composable
fun MapView(
    mapViewModel: MapViewModel,
    mapViewState: MutableState<MapViewState>,
    modifier: Modifier = Modifier,
    onLoad: ((map: MapView) -> Unit)? = null,
) {
    val context = LocalContext.current

    val settingResultRequest = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult(),
    ) { activityResult ->
        if (activityResult.resultCode == Activity.RESULT_OK) {
            Log.d("appDebug", "Accepted")
        } else {
            Log.d("appDebug", "Denied")
        }
    }

    val requestPermissionLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestMultiplePermissions(),
            onResult = { isGranted: Map<String, Boolean> ->
                if (isGranted.values.all { it }) {
                    Log.d("appDebug", "Accepted")
                } else {
                    Log.d("appDebug", "Denied")
                }
            },
        )

    val mapState =
        rememberMapViewWithLifecycle(
            mapViewModel.mapCenter.value,
            mapViewModel.mapZoom.value,
        ).apply {
            maxZoomLevel = GlobalConstants.MAX_ZOOM_MAP
            zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
            setMultiTouchControls(true)
        }

    val iwOverlay = remember {
        Marker(mapState).apply {
            icon = context.getDrawable(R.drawable.baseline_arrow_drop_down_24)
            infoWindow = HydrantInfoWindow(
                mapState,
            )
        }
    }

    val hydrantOverlay = remember {
        mutableStateOf(SimpleFastPointOverlay(SimplePointTheme(listOf<MapViewModel.HydrantGeoPoint>())))
    }

    val newHydrantOverlay = remember {
        mutableStateOf(SimpleFastPointOverlay(SimplePointTheme(listOf<MapViewModel.HydrantGeoPoint>())))
    }

    val tourneeOverlay = remember {
        mutableStateOf(
            mapOf<Tournee, SimpleFastPointOverlay>(),
        )
    }

    val locationOverlay = remember {
        MyLocationNewOverlay(GpsMyLocationProvider(context), mapState).apply {
            enableMyLocation()
            isOptionsMenuEnabled = true
        }
    }

    LaunchedEffect(Unit) {
        mapViewModel.register(mapState)
        mapState.apply {
            overlays.addAll(
                listOf(
                    locationOverlay,
                    iwOverlay,
                ),
            )
            mapState.addMapListener(
                object : MapListener {
                    override fun onScroll(event: ScrollEvent?): Boolean {
                        mapViewModel.setCenter(mapCenter)
                        return true
                    }

                    override fun onZoom(event: ZoomEvent?): Boolean {
                        mapViewModel.setZoom(zoomLevelDouble)
                        return true
                    }
                },
            )
        }
        mapViewModel.hydrantList.observeForever {
            hydrantOverlay.value =
                SimpleFastPointOverlay(
                    SimplePointTheme(
                        it.map {
                            MapViewModel.HydrantGeoPoint(
                                it.lat,
                                it.lon,
                                it.idHydrant,
                                it.numero,
                                it.dispoTerrestre,
                                it.voie,
                                it.suffixeVoie,
                                it.voie2,
                                it.observation,
                                peiCaracteristiques = it.peiCaracteristiques,
                            )
                        },
                    ),
                    SimpleFastPointOverlayOptions.getDefaultStyle().apply {
                        pointStyle.color = Color.rgb(255, 127, 31)
                        selectedPointStyle.strokeWidth = 8f
                        selectedPointStyle.color = Color.argb(
                            0.75f, 0.1f, 0.33f, 1f,
                        )
                        symbol = SimpleFastPointOverlayOptions.Shape.SQUARE
                        setRadius(8f)
                        setSelectedRadius(16f)
                    },
                ).apply {
                    setOnClickListener { points, point ->
                        InfoWindow.closeAllInfoWindowsOn(mapState)
                        val selected = points.get(point) as MapViewModel.HydrantGeoPoint
                        mapState.controller.animateTo(selected)
                        iwOverlay.position = selected
                        iwOverlay.relatedObject = selected
                        iwOverlay.showInfoWindow()
                    }
                }
            mapState.overlays.retainAll(
                listOf(
                    locationOverlay,
                    iwOverlay,
                ),
            )
            mapState.overlays.add(hydrantOverlay.value)
            mapState.overlays.add(newHydrantOverlay.value)
            mapState.overlays.addAll(tourneeOverlay.value.values)
            Log.e("hydrantOverlay", mapState.overlays.size.toString())
        }
        mapViewModel.newHydrantList.observeForever {
            newHydrantOverlay.value =
                SimpleFastPointOverlay(
                    SimplePointTheme(
                        it.map {
                            MapViewModel.HydrantGeoPoint(
                                it.lat,
                                it.lon,
                                it.idHydrant,
                                it.numero,
                                it.dispoTerrestre,
                                it.voie,
                                it.suffixeVoie,
                                it.voie2,
                                it.observation,
                                it.peiCaracteristiques,
                            )
                        },
                    ),
                    SimpleFastPointOverlayOptions.getDefaultStyle().apply {
                        pointStyle.color = Color.rgb(255, 0, 0)
                        selectedPointStyle.strokeWidth = 8f
                        selectedPointStyle.color = Color.argb(0.75f, 0.1f, 0.33f, 1f)
                        symbol = SimpleFastPointOverlayOptions.Shape.SQUARE
                        setRadius(8f)
                        setSelectedRadius(16f)
                    },
                ).apply {
                    setOnClickListener { points, point ->
                        InfoWindow.closeAllInfoWindowsOn(mapState)
                        val selected = points.get(point) as MapViewModel.HydrantGeoPoint
                        mapState.controller.animateTo(selected)
                        iwOverlay.position = selected
                        iwOverlay.relatedObject = selected
                        iwOverlay.showInfoWindow()
                    }
                }
            mapState.overlays.retainAll(
                listOf(
                    locationOverlay,
                    iwOverlay,
                ),
            )
            mapState.overlays.add(hydrantOverlay.value)
            mapState.overlays.add(newHydrantOverlay.value)
            mapState.overlays.addAll(tourneeOverlay.value.values)
            Log.e("newHydrantOverlay", mapState.overlays.size.toString())
        }
        mapViewModel.tourneeList.observeForever { list ->
            tourneeOverlay.value =
                list.mapValues {
                    SimpleFastPointOverlay(
                        SimplePointTheme(
                            it.value.map { hydrant ->
                                MapViewModel.HydrantGeoPoint(
                                    hydrant.lat,
                                    hydrant.lon,
                                    hydrant.idHydrant,
                                    hydrant.numero,
                                    hydrant.dispoTerrestre,
                                    hydrant.voie,
                                    hydrant.suffixeVoie,
                                    hydrant.voie2,
                                    hydrant.observation,
                                    hydrant.peiCaracteristiques,
                                )
                            },
                        ),
                        SimpleFastPointOverlayOptions.getDefaultStyle().apply {
                            pointStyle.color = Color.argb(
                                it.key.getColor().alpha,
                                it.key.getColor().red,
                                it.key.getColor().green,
                                it.key.getColor().blue,
                            )
                            selectedPointStyle.strokeWidth = 8f
                            selectedPointStyle.color = Color.argb(0.75f, 0.1f, 0.33f, 1f)
                            symbol = SimpleFastPointOverlayOptions.Shape.CIRCLE
                            setRadius(12f)
                            setSelectedRadius(20f)
                        },
                    ).apply {
                        setOnClickListener { points, point ->
                            InfoWindow.closeAllInfoWindowsOn(mapState)
                            val selected = points.get(point) as MapViewModel.HydrantGeoPoint
                            mapState.controller.animateTo(selected)
                            iwOverlay.position = selected
                            iwOverlay.relatedObject = selected
                            iwOverlay.showInfoWindow()
                        }
                    }
                }
            mapState.overlays.retainAll(
                listOf(
                    locationOverlay,
                    iwOverlay,
                ),
            )
            mapState.overlays.add(hydrantOverlay.value)
            mapState.overlays.add(newHydrantOverlay.value)
            mapState.overlays.addAll(tourneeOverlay.value.values)
            Log.e("tourneeOverlay", mapState.overlays.size.toString())
        }
    }

    Box {
        AndroidView(
            { mapState },
            modifier,
        ) { mapView -> onLoad?.invoke(mapView) }
        if (mapViewModel.showCenter.value!!) {
            Icon(
                imageVector = Icons.Filled.GpsFixed,
                contentDescription = "Centre",
                modifier = Modifier.align(Alignment.Center),
            )
        }
        Column(
            Modifier
                .padding(16.dp)
                .align(Alignment.BottomStart),
        ) {
            FloatingActionButton(
                onClick = {
                    mapState.controller.zoomIn()
                },
                shape = CircleShape,
            ) {
                Icon(
                    imageVector = Icons.Filled.ZoomIn,
                    contentDescription = "ZoomIn",
                )
            }
            Spacer(Modifier.height(16.dp))
            FloatingActionButton(
                onClick = {
                    mapState.controller.zoomOut()
                },
                shape = CircleShape,
            ) {
                Icon(
                    imageVector = Icons.Filled.ZoomOut,
                    contentDescription = "ZoomOut",
                )
            }
            Spacer(Modifier.height(16.dp))
            FloatingActionButton(
                onClick = {
                    checkLocationSetting(
                        context = context,
                        onDisabled = { intentSenderRequest ->
                            settingResultRequest.launch(intentSenderRequest)
                        },
                        onEnabled = {
                            if (hasLocationPermission(context)) {
                                if (!locationOverlay.isFollowLocationEnabled) {
                                    locationOverlay.enableFollowLocation()
                                } else {
                                    locationOverlay.disableFollowLocation()
                                }
                            } else {
                                requestPermissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.ACCESS_COARSE_LOCATION,
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                    ),
                                )
                            }
                        },
                    )
                },
                shape = CircleShape,
            ) {
                Icon(
                    imageVector =
                    if (!hasLocationEnabled(context) || !hasLocationPermission(context)) {
                        Icons.Filled.GpsOff
                    } else if (!locationOverlay.isFollowLocationEnabled) {
                        Icons.Filled.GpsNotFixed
                    } else {
                        Icons.Filled.GpsFixed
                    },
                    contentDescription = "GPS",
                )
            }
            Spacer(Modifier.height(16.dp))
            FloatingActionButton(
                onClick = {
                    mapViewState.value = mapViewState.value.copy(
                        showMapView = mapViewState.value.showMapView,
                        isFullscreen = !mapViewState.value.isFullscreen,
                    )
                },
                shape = CircleShape,
            ) {
                Icon(
                    imageVector = if (mapViewState.value.isFullscreen) {
                        Icons.Filled.ZoomInMap
                    } else {
                        Icons.Filled.ZoomOutMap
                    },
                    contentDescription = "Fullscreen",
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

fun checkLocationSetting(
    context: Context,
    onDisabled: (IntentSenderRequest) -> Unit,
    onEnabled: () -> Unit,
) {
    val locationRequest = LocationRequest.create().apply {
        interval = 1000
        fastestInterval = 1000
        priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }

    val client: SettingsClient = LocationServices.getSettingsClient(context)
    val builder: LocationSettingsRequest.Builder = LocationSettingsRequest
        .Builder()
        .addLocationRequest(locationRequest)

    val gpsSettingTask: Task<LocationSettingsResponse> =
        client.checkLocationSettings(builder.build())

    gpsSettingTask.addOnSuccessListener { onEnabled() }
    gpsSettingTask.addOnFailureListener { exception ->
        if (exception is ResolvableApiException) {
            try {
                val intentSenderRequest = IntentSenderRequest
                    .Builder(exception.resolution)
                    .build()
                onDisabled(intentSenderRequest)
            } catch (sendEx: IntentSender.SendIntentException) {
                // ignore here
            }
        }
    }
}

private fun hasLocationPermission(context: Context): Boolean =
    ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_COARSE_LOCATION,
    ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION,
    ) == PackageManager.PERMISSION_GRANTED

private fun hasLocationEnabled(context: Context): Boolean =
    getSystemService(context, LocationManager::class.java)?.isLocationEnabled == true
