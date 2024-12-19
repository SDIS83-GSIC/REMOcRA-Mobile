package fr.sdis83.remocra.mobile.ui.screens.pei

import android.Manifest
import android.app.Application
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.em
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import fr.sdis83.remocra.mobile.R
import fr.sdis83.remocra.mobile.database.Pei
import fr.sdis83.remocra.mobile.database.PhotoPei
import fr.sdis83.remocra.mobile.database.ReferentielDao
import fr.sdis83.remocra.mobile.database.TypeVisite
import fr.sdis83.remocra.mobile.database.VisiteDao.VisiteWithAnomalies
import fr.sdis83.remocra.mobile.navigation.Screens
import fr.sdis83.remocra.mobile.ui.components.CameraCapture
import fr.sdis83.remocra.mobile.ui.components.LabelledCheckbox
import fr.sdis83.remocra.mobile.ui.components.Spinner
import fr.sdis83.remocra.mobile.utils.GlobalConstants
import fr.sdis83.remocra.mobile.utils.deleteFile
import fr.sdis83.remocra.mobile.utils.pxToDp
import fr.sdis83.remocra.mobile.viewmodels.AgentViewModel
import fr.sdis83.remocra.mobile.viewmodels.MapViewModel
import fr.sdis83.remocra.mobile.viewmodels.PhotoPeiViewModel
import fr.sdis83.remocra.mobile.viewmodels.VisiteViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.UUID
import kotlin.reflect.KFunction1

private val DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy")
private val HOUR_FORMAT = DateTimeFormatter.ofPattern("HH:mm")

@Composable
fun VisiteScreen(
    navController: NavController,
    tourneeId: UUID,
    peiId: UUID,
    mapViewModel: MapViewModel,
) {
    val context = LocalContext.current
    val agentViewModel = AgentViewModel(context.applicationContext as Application)
    val gestionAgents by agentViewModel.gestionAgents.observeAsState()
    val listAgent1 by agentViewModel.listAgent1.observeAsState()
    val listAgent2 by agentViewModel.listAgent2.observeAsState()
    val visiteViewModel =
        VisiteViewModel(context.applicationContext as Application, tourneeId, peiId, gestionAgents)

    val photoPeiVisiteViewModel = PhotoPeiViewModel(context.applicationContext as Application, peiId)

    val photos = photoPeiVisiteViewModel.photos.observeAsState()

    mapViewModel.goToPei(peiId, false)
    VisiteScreenInner(visiteViewModel, navController, photos.value, photoPeiVisiteViewModel, gestionAgents, listAgent1, listAgent2)
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun VisiteScreenInner(
    visiteViewModel: VisiteViewModel,
    navController: NavController,
    photos: List<PhotoPei>?,
    photoPeiVisiteViewModel: PhotoPeiViewModel,
    gestionAgents: String?,
    listAgent1: List<String>?,
    listAgent2: List<String>?,
) {
    val coroutineScope = rememberCoroutineScope()
    val pagerState = rememberPagerState()
    val nbSteps = 3

    val visite by visiteViewModel.visiteState.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        Row(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.pxToDp),
                ) {
                    Button(onClick = {
                        navController.navigate(
                            Screens.TourneePei.route
                                .replace(
                                    oldValue = "{tourneeId}",
                                    newValue = visite.visite.tourneeId.toString(),
                                ),
                        ) {
                            popUpTo(Screens.TourneePei.route) {
                                inclusive = true
                            }
                        }
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage - 1)
                        }
                    }) {
                        Text(text = "Retour")
                    }
                    Text(
                        modifier = Modifier.padding(10.pxToDp, 0.pxToDp),
                        text = "Visite du point d'eau n°${visite.numeroPei} : ${pagerState.currentPage + 1} / $nbSteps",
                        fontSize = 5.em,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 1.em,
                    )
                }
                VisiteForm(
                    coroutineScope = coroutineScope,
                    pagerState = pagerState,
                    visiteViewModel = visiteViewModel,
                    navController = navController,
                    visite = visite,
                    photos = photos,
                    photoPeiViewModel = photoPeiVisiteViewModel,
                    gestionAgents = gestionAgents,
                    listAgent1 = listAgent1,
                    listAgent2 = listAgent2,
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun VisiteForm(
    coroutineScope: CoroutineScope,
    pagerState: PagerState,
    visiteViewModel: VisiteViewModel,
    photoPeiViewModel: PhotoPeiViewModel,
    navController: NavController,
    visite: VisiteWithAnomalies?,
    photos: List<PhotoPei>?,
    gestionAgents: String?,
    listAgent1: List<String>?,
    listAgent2: List<String>?,
) {
    if (visite == null) return

    val typeSaisieList by visiteViewModel.typeVisiteList.observeAsState()
    val anomalieList by visiteViewModel.anomalieList.collectAsState(listOf())

    HorizontalPager(
        modifier = Modifier
            .fillMaxHeight(),
        state = pagerState,
        verticalAlignment = Alignment.Top,
        pageCount = 3,
        userScrollEnabled = false,
    ) {
        when (it) {
            0 -> StepOne(
                onNext = {
                    coroutineScope.launch {
                        if (visite.visite.isValid) {
                            visiteViewModel.save()
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    }
                },
                visite = visite,
                onValueChange = visiteViewModel::updateForm,
                typeVisite = typeSaisieList ?: listOf(),
                gestionAgents = gestionAgents,
                listAgent1 = listAgent1,
                listAgent2 = listAgent2,
            )

            1 -> StepTwo(
                onPrevious = {
                    coroutineScope.launch {
                        visiteViewModel.save(close = false)
                        pagerState.animateScrollToPage(pagerState.currentPage - 1)
                    }
                },
                onNext = {
                    coroutineScope.launch {
                        visiteViewModel.save()
                        pagerState.animateScrollToPage(pagerState.currentPage + 1)
                    }
                },
                visite = visite,
                onValueChange = visiteViewModel::updateForm,
                anomalieList = anomalieList,
                peiState = visiteViewModel.peiState,
            )
            2 -> StepThree(
                onPrevious = {
                    coroutineScope.launch {
                        visiteViewModel.save(close = false)
                        pagerState.animateScrollToPage(pagerState.currentPage - 1)
                    }
                },
                onNext = {
                    coroutineScope.launch {
                        visiteViewModel.save(close = true)
                        navController.popBackStack(Screens.TourneePei.route, inclusive = false)
                    }
                },
                visite = visite,
                onValueChange = visiteViewModel::updateForm,
                onPictureTaken = photoPeiViewModel::onPictureTaken,
                photos = photos,
                deletePhoto = {
                    deleteFile(listOf(it.path))
                    coroutineScope.launch {
                        photoPeiViewModel.deletePhotoPei(it)
                    }
                },
            )
        }
    }
}

@Composable
private fun StepOne(
    onNext: () -> Unit,
    visite: VisiteWithAnomalies,
    onValueChange: (VisiteWithAnomalies) -> Unit = {},
    typeVisite: List<TypeVisite>,
    gestionAgents: String?,
    listAgent1: List<String>?,
    listAgent2: List<String>?,
) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    val datePickerDialog = DatePickerDialog(
        context,
        { _, year: Int, month: Int, dayOfMonth: Int ->
            val date = ZonedDateTime.of(
                year,
                month + 1,
                dayOfMonth,
                visite.visite.dateVisite.hour,
                visite.visite.dateVisite.minute,
                visite.visite.dateVisite.second,
                visite.visite.dateVisite.nano,
                ZoneId.systemDefault(),
            )
            onValueChange(
                visite.copy(
                    visite = visite.visite.copy(
                        dateVisite = date,
                    ),
                ),
            )
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH),
    )

    val timePickerDialog = TimePickerDialog(
        context,
        { _, hour: Int, minute: Int ->
            val date = ZonedDateTime.of(
                visite.visite.dateVisite.year,
                visite.visite.dateVisite.monthValue,
                visite.visite.dateVisite.dayOfMonth,
                hour,
                minute,
                visite.visite.dateVisite.second,
                visite.visite.dateVisite.nano,
                ZoneId.systemDefault(),
            )
            onValueChange(
                visite.copy(
                    visite = visite.visite.copy(
                        dateVisite = date,
                    ),
                ),
            )
        },
        calendar.get(Calendar.HOUR),
        calendar.get(Calendar.MINUTE),
        true,
    )

    val datePickerInteractionSource = remember { MutableInteractionSource() }
    val isDatePickerPressed: Boolean by datePickerInteractionSource.collectIsPressedAsState()

    LaunchedEffect(isDatePickerPressed) {
        if (isDatePickerPressed) {
            datePickerDialog.show()
        }
    }

    val timePickerInteractionSource = remember { MutableInteractionSource() }
    val isTimePickerPressed: Boolean by timePickerInteractionSource.collectIsPressedAsState()

    LaunchedEffect(isTimePickerPressed) {
        if (isTimePickerPressed) {
            timePickerDialog.show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .padding(10.pxToDp)
            .verticalScroll(rememberScrollState()),
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                readOnly = true,
                value = DATE_FORMAT.format(visite.visite.dateVisite),
                onValueChange = {},
                trailingIcon = {
                    Icon(Icons.Default.EditCalendar, contentDescription = null)
                },
                label = {
                    Text(text = "Date")
                },
                placeholder = {
                    Text(text = "Date")
                },
                modifier = Modifier.weight(1f),
                interactionSource = datePickerInteractionSource,
            )
            OutlinedTextField(
                readOnly = true,
                value = HOUR_FORMAT.format(visite.visite.dateVisite),
                onValueChange = {},
                trailingIcon = {
                    Icon(Icons.Default.Timer, contentDescription = null)
                },
                label = {
                    Text(text = "Heure")
                },
                placeholder = {
                    Text(text = "Heure")
                },
                modifier = Modifier.weight(1f),
                interactionSource = timePickerInteractionSource,
            )
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            Spinner(
                items = typeVisite,
                value = typeVisite.find { i -> i.typeVisiteId == visite.visite.typeVisiteId },
                valueToString = TypeVisite::typeVisiteLibelle,
                label = "Type de visite",
                onSelectionChanged = {
                    if (it.typeVisiteCode == "CTRL") {
                        onValueChange(
                            visite.copy(
                                visite = visite.visite.copy(
                                    typeVisiteId = it.typeVisiteId,
                                ),
                            ),
                        )
                    } else {
                        onValueChange(
                            visite.copy(
                                visite = visite.visite.copy(
                                    typeVisiteId = it.typeVisiteId,
                                    ctrlDebitPression = false,
                                    debit = null,
                                    pressionDyn = null,
                                    pression = null,
                                ),
                            ),
                        )
                    }
                },
            )
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            Spinner(
                value = visite.visite.agent1 ?: "",
                items = listAgent1 ?: listOf(),
                onSelectionChanged = {
                    onValueChange(
                        visite.copy(
                            visite = visite.visite.copy(agent1 = it),
                        ),
                    )
                },
                onValueChange = {
                    onValueChange(
                        visite.copy(
                            visite = visite.visite.copy(agent1 = it),
                        ),
                    )
                },
                valueToString = String::toString,
                label = "Agent 1",
                placeholder = "Agent 1",
                singleLine = true,
                modifier = Modifier.weight(1f),
                enabled = gestionAgents != GlobalConstants.UTILISATEUR_CONNECTE_OBLIGATOIRE,
                readOnly = false,
            )
            Spinner(
                value = visite.visite.agent2 ?: "",
                items = listAgent2 ?: listOf(),
                onSelectionChanged = {
                    onValueChange(
                        visite.copy(
                            visite = visite.visite.copy(agent2 = it),
                        ),
                    )
                },
                onValueChange = {
                    onValueChange(
                        visite.copy(
                            visite = visite.visite.copy(agent2 = it),
                        ),
                    )
                },
                valueToString = String::toString,
                label = "Agent 2",
                placeholder = "Agent 2",
                singleLine = true,
                modifier = Modifier.weight(1f),
                readOnly = false,
            )
        }
        if (typeVisite.find { it.typeVisiteId == visite.visite.typeVisiteId }?.typeVisiteCode == "CTRL") {
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(text = "Contrôle débit et pression")
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(text = "Non")
                Spacer(modifier = Modifier.width(16.pxToDp))
                Switch(
                    checked = visite.visite.ctrlDebitPression,
                    onCheckedChange = {
                        if (it) {
                            onValueChange(
                                visite.copy(
                                    visite = visite.visite.copy(
                                        ctrlDebitPression = true,
                                    ),
                                ),
                            )
                        } else {
                            onValueChange(
                                visite.copy(
                                    visite = visite.visite.copy(
                                        ctrlDebitPression = false,
                                        debit = null,
                                        pressionDyn = null,
                                        pression = null,
                                    ),
                                ),
                            )
                        }
                    },
                )
                Spacer(modifier = Modifier.width(16.pxToDp))
                Text(text = "Oui")
            }
            if (visite.visite.ctrlDebitPression) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = visite.visite.debit?.toString() ?: "",
                        onValueChange = {
                            it.toIntOrNull()?.let { value ->
                                onValueChange(
                                    visite.copy(
                                        visite = visite.visite.copy(debit = value),
                                    ),
                                )
                            }
                        },
                        label = {
                            Text(text = "Débit à 1 bar (㎥/h)")
                        },
                        placeholder = {
                            Text(text = "Débit à 1 bar (㎥/h)")
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                    )
                    OutlinedTextField(
                        value = visite.visite.pressionDyn?.toString() ?: "",
                        onValueChange = {
                            it.toDoubleOrNull()?.let { value ->
                                onValueChange(
                                    visite.copy(
                                        visite = visite.visite.copy(pressionDyn = value),
                                    ),
                                )
                            }
                        },
                        label = {
                            Text(text = "Pression dynamique à 60 ㎥ (bar)")
                        },
                        placeholder = {
                            Text(text = "Pression dynamique à 60 ㎥ (bar)")
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                    )
                }
                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = visite.visite.pression?.toString() ?: "",
                        onValueChange = {
                            it.toDoubleOrNull()?.let { value ->
                                onValueChange(
                                    visite.copy(
                                        visite = visite.visite.copy(pression = value),
                                    ),
                                )
                            }
                        },
                        label = {
                            Text(text = "Pression statique (bar)")
                        },
                        placeholder = {
                            Text(text = "Pression statique (bar)")
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            Button(onClick = onNext, enabled = visite.visite.isValid) {
                Text(stringResource(id = R.string.suivant))
            }
        }
    }
}

@Composable
private fun StepTwo(
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    visite: VisiteWithAnomalies,
    onValueChange: (VisiteWithAnomalies) -> Unit = {},
    anomalieList: List<ReferentielDao.AnomalieItem>,
    peiState: StateFlow<Pei?>,
) {
    val options =
        anomalieList.groupBy { it.categorie }.mapValues { entry ->
            entry.value.map { item ->
                val checked =
                    remember { mutableStateOf(visite.anomalies.contains(item.anomalie)) }

                Option(
                    checked = checked.value,
                    onCheckedChange = {
                        checked.value = it
                        if (it) {
                            onValueChange(
                                visite.copy(
                                    anomalies = visite.anomalies.apply { add(item.anomalie) },
                                ),
                            )
                        } else {
                            onValueChange(
                                visite.copy(
                                    anomalies = visite.anomalies.apply { remove(item.anomalie) },
                                ),
                            )
                        }
                    },
                    text = item.anomalie.anomalieLibelle,
                    label = {
                        Text(
                            text = item.anomalie.anomalieLibelle,
                            fontWeight = if (item.valIndispoTerrestre >= 5) FontWeight.Bold else null,
                        )
                    },
                    enabled = visite.visite.hasAnomalieChanges,
                )
            }
        }

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .padding(10.pxToDp)
            .verticalScroll(rememberScrollState()),
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(fontWeight = FontWeight.Bold, text = "Anomalies")
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            LabelledCheckbox(
                checked = !visite.visite.hasAnomalieChanges,
                onCheckedChange = {
                    onValueChange(
                        visite.copy(
                            visite = visite.visite.copy(hasAnomalieChanges = !it),
                        ),
                    )
                },
                label = { Text("Ne rien modifier") },
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f, fill = false)
                .verticalScroll(
                    rememberScrollState(),
                ),
        ) {
            if (!options.isNullOrEmpty()) {
                Column {
                    options.keys.sortedBy { k -> k.anomalieCategorieCode }.forEach { critere ->
                        val opened = remember { mutableStateOf(false) }
                        Row(
                            modifier = Modifier
                                .clickable { opened.value = !opened.value }
                                .clip(RoundedCornerShape(8.pxToDp))
                                .padding(8.pxToDp)
                                .fillMaxWidth(),
                        ) {
                            Icon(
                                imageVector =
                                if (opened.value) {
                                    Icons.Filled.ExpandLess
                                } else {
                                    Icons.Filled.ExpandMore
                                },
                                contentDescription = "Open/Close",
                            )
                            Spacer(modifier = Modifier.width(16.pxToDp))
                            Text(fontWeight = FontWeight.Bold, text = critere.anomalieCategorieLibelle)
                            Spacer(modifier = Modifier.width(16.pxToDp))
                            Text(text = "(${options[critere]!!.count { it.checked }}/${options[critere]!!.size})")
                        }
                        if (opened.value) {
                            options[critere]!!.sortedBy { o -> o.text }.forEach { option ->
                                LabelledCheckbox(
                                    checked = option.checked,
                                    onCheckedChange = option.onCheckedChange,
                                    label = option.label,
                                    enabled = option.enabled,
                                )
                            }
                        }
                    }
                }
            }
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            Button(onClick = onPrevious) {
                Text(stringResource(id = R.string.precedent))
            }
            Spacer(modifier = Modifier.width(16.pxToDp))
            Button(onClick = onNext) {
                Text(stringResource(id = R.string.suivant))
            }
        }
    }
}

@Composable
private fun StepThree(
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    visite: VisiteWithAnomalies,
    onValueChange: (VisiteWithAnomalies) -> Unit = {},
    onPictureTaken: KFunction1<Bitmap, Unit>,
    photos: List<PhotoPei>?,
    deletePhoto: (PhotoPei) -> Unit,
) {
    val context = LocalContext.current
    val requestPermissionLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
            onResult = { isGranted: Boolean ->
                if (isGranted) {
                    Log.d("appDebug", "Accepted")
                } else {
                    Log.d("appDebug", "Denied")
                }
            },
        )

    var showCustomDialog by remember {
        mutableStateOf(false)
    }

    if (showCustomDialog) {
        Dialog(
            onDismissRequest = { showCustomDialog = false },
            properties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = true,
                usePlatformDefaultWidth = false,
            ),
        ) {
            CameraCapture(
                onPictureTaken = onPictureTaken,
            ) { showCustomDialog = false }
        }
    }

    Column(
        Modifier
            .fillMaxHeight()
            .padding(10.pxToDp)
            .verticalScroll(rememberScrollState()),
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Text("Observations")
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 128.pxToDp),
                value = visite.visite.observations ?: "",
                onValueChange = {
                    onValueChange(
                        visite.copy(
                            visite = visite.visite.copy(observations = it),
                        ),
                    )
                },
                label = {
                    Text(text = "Observations")
                },
                placeholder = {
                    Text(text = "Observations")
                },
                singleLine = false,
            )
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            Button(onClick = {
                if (ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.CAMERA,
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    showCustomDialog = !showCustomDialog
                } else {
                    requestPermissionLauncher.launch(
                        Manifest.permission.CAMERA,
                    )
                }
            }) {
                Icon(imageVector = Icons.Filled.CameraAlt, contentDescription = "Photo")
                Spacer(modifier = Modifier.width(16.pxToDp))
                Text(
                    text = "Ajouter",
                )
            }
        }
        PhotoList(photos ?: listOf(), deletePhoto)

        Row(modifier = Modifier.fillMaxWidth()) {
            Button(onClick = onPrevious) {
                Text(stringResource(id = R.string.precedent))
            }
            Spacer(modifier = Modifier.width(16.pxToDp))
            Button(onClick = onNext, enabled = visite.visite.isValid) {
                Text(stringResource(id = R.string.valider))
            }
        }
    }
}

@Composable
private fun PhotoList(photos: List<PhotoPei>, deletePhoto: (PhotoPei) -> Unit) {
    LazyRow {
        items(photos) { photo ->
            Image(
                painter = rememberAsyncImagePainter(File(photo.path)),
                contentDescription = photo.photoId.toString(),
                modifier = Modifier
                    .height(150.pxToDp)
                    .padding(10.pxToDp)
                    .aspectRatio(1f)
                    .clipToBounds(),
            )
            IconButton(onClick = {
                deletePhoto(photo)
            }) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "EditContact",
                    Modifier.size(60.pxToDp),
                )
            }
        }
    }
}

data class Option(
    var checked: Boolean,
    var onCheckedChange: (Boolean) -> Unit = {},
    val text: String,
    val label: @Composable () -> Unit,
    var enabled: Boolean = true,
)
