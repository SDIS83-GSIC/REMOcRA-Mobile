package fr.sdis83.remocra.mobile.ui.screens.hydrants

import android.app.Application
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import fr.sdis83.remocra.mobile.database.Hydrant
import fr.sdis83.remocra.mobile.database.HydrantVisiteDao.HydrantVisiteWithAnomalies
import fr.sdis83.remocra.mobile.database.ReferentielDao
import fr.sdis83.remocra.mobile.database.TypeHydrantSaisie
import fr.sdis83.remocra.mobile.ui.components.LabelledCheckbox
import fr.sdis83.remocra.mobile.ui.components.Spinner
import fr.sdis83.remocra.mobile.viewmodels.HydrantVisiteViewModel
import fr.sdis83.remocra.mobile.viewmodels.MapViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.UUID

private val DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy")
private val HOUR_FORMAT = DateTimeFormatter.ofPattern("HH:mm")

@Composable
fun HydrantVisiteScreen(
    navController: NavController,
    idTournee: UUID,
    idHydrant: UUID,
    mapViewModel: MapViewModel,
) {
    val context = LocalContext.current
    val hydrantVisiteViewModel =
        HydrantVisiteViewModel(context.applicationContext as Application, idTournee, idHydrant)

    mapViewModel.goToHydrant(idHydrant)
    HydrantVisiteScreenInner(hydrantVisiteViewModel, navController)
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HydrantVisiteScreenInner(
    hydrantVisiteViewModel: HydrantVisiteViewModel,
    navController: NavController,
) {
    val coroutineScope = rememberCoroutineScope()
    val pagerState = rememberPagerState()
    val nbSteps = 3

    val hydrantVisite by hydrantVisiteViewModel.hydrantVisiteState.collectAsState()

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
                        .padding(10.dp)
                ) {
                    Button(onClick = {
                        if (pagerState.currentPage == pagerState.initialPage) {
                            navController.popBackStack()
                        } else {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage - 1)
                            }
                        }
                    }) {
                        Text(
                            text = "Retour"
                        )
                    }
                    Text(
                        text = "Visite d'un point d'eau : ${pagerState.currentPage + 1} / $nbSteps",
                        fontSize = MaterialTheme.typography.headlineLarge.fontSize,
                        fontWeight = FontWeight.Bold
                    )
                }
                HydrantVisiteForm(
                    coroutineScope = coroutineScope,
                    pagerState = pagerState,
                    hydrantVisiteViewModel = hydrantVisiteViewModel,
                    navController = navController,
                    hydrantVisite = hydrantVisite,
                )

            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HydrantVisiteForm(
    coroutineScope: CoroutineScope,
    pagerState: PagerState,
    hydrantVisiteViewModel: HydrantVisiteViewModel,
    navController: NavController,
    hydrantVisite: HydrantVisiteWithAnomalies?,
) {
    if (hydrantVisite == null) return

    val typeSaisieList by hydrantVisiteViewModel.typeSaisieList.observeAsState()
    val anomalieList by hydrantVisiteViewModel.anomalieList.observeAsState()

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
                hydrantVisite = hydrantVisite,
                onClick = {
                    if (hydrantVisite.hydrantVisite.isValid) {
                        coroutineScope.launch {
                            hydrantVisiteViewModel.save()
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    }
                },
                onValueChange = hydrantVisiteViewModel::updateForm,
                typeSaisieList = typeSaisieList ?: listOf()
            )

            1 -> StepTwo(
                hydrantVisite = hydrantVisite,
                onValueChange = hydrantVisiteViewModel::updateForm,
                onClick = {
                    coroutineScope.launch {
                        hydrantVisiteViewModel.save()
                        pagerState.animateScrollToPage(pagerState.currentPage + 1)
                    }
                },
                anomalieList = anomalieList ?: listOf(),
                hydrantVisiteViewModel.hydrantState
            )

            2 -> StepThree(
                hydrantVisite = hydrantVisite,
                onValueChange = hydrantVisiteViewModel::updateForm
            ) {
                coroutineScope.launch {
                    hydrantVisiteViewModel.save(close = true)
                    navController.popBackStack()
                }
            }
        }
    }
}

@Composable
fun StepOne(
    hydrantVisite: HydrantVisiteWithAnomalies,
    onValueChange: (HydrantVisiteWithAnomalies) -> Unit = {},
    onClick: () -> Unit,
    typeSaisieList: List<TypeHydrantSaisie>
) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    val datePickerDialog = DatePickerDialog(
        context,
        { _, year: Int, month: Int, dayOfMonth: Int ->
            val date = ZonedDateTime.of(
                year, month + 1, dayOfMonth,
                hydrantVisite.hydrantVisite.dateVisite.hour,
                hydrantVisite.hydrantVisite.dateVisite.minute,
                hydrantVisite.hydrantVisite.dateVisite.second,
                hydrantVisite.hydrantVisite.dateVisite.nano,
                ZoneId.systemDefault()
            )
            onValueChange(
                hydrantVisite.copy(
                    hydrantVisite = hydrantVisite.hydrantVisite.copy(
                        dateVisite = date
                    ),
                )
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
                hydrantVisite.hydrantVisite.dateVisite.year,
                hydrantVisite.hydrantVisite.dateVisite.monthValue,
                hydrantVisite.hydrantVisite.dateVisite.dayOfMonth,
                hour,
                minute,
                hydrantVisite.hydrantVisite.dateVisite.second,
                hydrantVisite.hydrantVisite.dateVisite.nano,
                ZoneId.systemDefault()
            )
            onValueChange(
                hydrantVisite.copy(
                    hydrantVisite = hydrantVisite.hydrantVisite.copy(
                        dateVisite = date
                    ),
                )
            )
        },
        calendar.get(Calendar.HOUR),
        calendar.get(Calendar.MINUTE),
        true
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
            .padding(10.dp)
            .verticalScroll(rememberScrollState()),
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                readOnly = true,
                value = DATE_FORMAT.format(hydrantVisite.hydrantVisite.dateVisite),
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
                interactionSource = datePickerInteractionSource
            )
            OutlinedTextField(
                readOnly = true,
                value = HOUR_FORMAT.format(hydrantVisite.hydrantVisite.dateVisite),
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
                interactionSource = timePickerInteractionSource
            )
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            Spinner(
                items = typeSaisieList,
                value = typeSaisieList.find { i -> i.idRemocra == hydrantVisite.hydrantVisite.idTypeHydrantSaisie },
                valueToString = TypeHydrantSaisie::nom,
                label = "Type de visite",
                onSelectionChanged = {
                    if (it.code == "CTRL") {
                        onValueChange(
                            hydrantVisite.copy(
                                hydrantVisite = hydrantVisite.hydrantVisite.copy(
                                    idTypeHydrantSaisie = it.idRemocra
                                ),
                            )
                        )
                    } else {
                        onValueChange(
                            hydrantVisite.copy(
                                hydrantVisite = hydrantVisite.hydrantVisite.copy(
                                    idTypeHydrantSaisie = it.idRemocra,
                                    ctrlDebitPression = false,
                                    debit = null,
                                    pressionDyn = null,
                                    pression = null
                                ),
                            )
                        )
                    }
                }
            )
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = hydrantVisite.hydrantVisite.agent1 ?: "",
                onValueChange = {
                    onValueChange(
                        hydrantVisite.copy(
                            hydrantVisite = hydrantVisite.hydrantVisite.copy(agent1 = it),
                        )
                    )
                },
                label = {
                    Text(text = "Agent 1")
                },
                placeholder = {
                    Text(text = "Agent 1")
                },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = hydrantVisite.hydrantVisite.agent2 ?: "",
                onValueChange = {
                    onValueChange(
                        hydrantVisite.copy(
                            hydrantVisite = hydrantVisite.hydrantVisite.copy(agent2 = it),
                        )
                    )
                },
                label = {
                    Text(text = "Agent 2")
                },
                placeholder = {
                    Text(text = "Agent 2")
                },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
        }
        if (typeSaisieList.find { it.idRemocra == hydrantVisite.hydrantVisite.idTypeHydrantSaisie }?.code == "CTRL") {
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(text = "Contrôle débit et pression")
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(text = "Non")
                Spacer(modifier = Modifier.width(16.dp))
                Switch(
                    checked = hydrantVisite.hydrantVisite.ctrlDebitPression,
                    onCheckedChange = {
                        if (it) {
                            onValueChange(
                                hydrantVisite.copy(
                                    hydrantVisite = hydrantVisite.hydrantVisite.copy(
                                        ctrlDebitPression = true
                                    ),
                                )
                            )
                        } else {
                            onValueChange(
                                hydrantVisite.copy(
                                    hydrantVisite = hydrantVisite.hydrantVisite.copy(
                                        ctrlDebitPression = false,
                                        debit = null,
                                        pressionDyn = null,
                                        pression = null
                                    ),
                                )
                            )
                        }
                    }
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(text = "Oui")
            }
            if (hydrantVisite.hydrantVisite.ctrlDebitPression) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = hydrantVisite.hydrantVisite.debit?.toString() ?: "",
                        onValueChange = {
                            it.toIntOrNull()?.let { value ->
                                onValueChange(
                                    hydrantVisite.copy(
                                        hydrantVisite = hydrantVisite.hydrantVisite.copy(debit = value),
                                    )
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
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = hydrantVisite.hydrantVisite.pressionDyn?.toString() ?: "",
                        onValueChange = {
                            it.toDoubleOrNull()?.let { value ->
                                onValueChange(
                                    hydrantVisite.copy(
                                        hydrantVisite = hydrantVisite.hydrantVisite.copy(pressionDyn = value),
                                    )
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
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = hydrantVisite.hydrantVisite.pression?.toString() ?: "",
                        onValueChange = {
                            it.toDoubleOrNull()?.let { value ->
                                onValueChange(
                                    hydrantVisite.copy(
                                        hydrantVisite = hydrantVisite.hydrantVisite.copy(pression = value),
                                    )
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
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            Button(onClick = onClick, enabled = hydrantVisite.hydrantVisite.isValid) {
                Text("Suivant")
            }
        }
    }
}

@Composable
fun StepTwo(
    hydrantVisite: HydrantVisiteWithAnomalies,
    onValueChange: (HydrantVisiteWithAnomalies) -> Unit = {},
    onClick: () -> Unit,
    anomalieList: List<ReferentielDao.AnomalieItem>,
    hydrantState: StateFlow<Hydrant?>,
) {

    val options = anomalieList.filter { it.anomalieNature.idTypeHydrantNature == hydrantState.value?.idNature }.groupBy { it.critere }.mapValues { entry ->
        entry.value.map { item ->
            val checked =
                remember { mutableStateOf(hydrantVisite.anomalies.contains(item.anomalie)) }

            Option(
                checked = checked.value,
                onCheckedChange = {
                    checked.value = it
                    if (it) {
                        onValueChange(
                            hydrantVisite.copy(
                                anomalies = hydrantVisite.anomalies.apply { add(item.anomalie) }
                            )
                        )
                    } else {
                        onValueChange(
                            hydrantVisite.copy(
                                anomalies = hydrantVisite.anomalies.apply { remove(item.anomalie) }
                            )
                        )
                    }
                },
                label = { Text(text = item.anomalie.nom,
                fontWeight = if (item.anomalieNature.valIndispoTerrestre >= 5) FontWeight.Bold else null) },
                enabled = hydrantVisite.hydrantVisite.hasAnomalieChanges,
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .padding(10.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(fontWeight = FontWeight.Bold, text = "Anomalies")
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LabelledCheckbox(
                checked = !hydrantVisite.hydrantVisite.hasAnomalieChanges,
                onCheckedChange = {
                    onValueChange(
                        hydrantVisite.copy(
                            hydrantVisite = hydrantVisite.hydrantVisite.copy(hasAnomalieChanges = !it),
                        )
                    )
                },
                label = { Text("Ne rien modifier") }
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f, fill = false)
                .verticalScroll(
                    rememberScrollState()
                )
        ) {
            if (!options.isNullOrEmpty()) {
                Column {
                    options.keys.forEach { critere ->
                        val opened = remember { mutableStateOf(false) }
                        Row(modifier = Modifier
                            .clickable { opened.value = !opened.value }
                            .clip(RoundedCornerShape(8.dp))
                            .padding(8.dp)
                            .fillMaxWidth()) {
                            Icon(
                                imageVector =
                                if (opened.value) {
                                    Icons.Filled.ExpandLess
                                } else {
                                    Icons.Filled.ExpandMore
                                }, contentDescription = "Open/Close"
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(fontWeight = FontWeight.Bold, text = critere.nom)
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(text = "(${options[critere]!!.count { it.checked }}/${options[critere]!!.size})")

                        }
                        if (opened.value) {
                            options[critere]!!.forEach { option ->
                                LabelledCheckbox(
                                    checked = option.checked,
                                    onCheckedChange = option.onCheckedChange,
                                    label = option.label,
                                    enabled = option.enabled
                                )
                            }
                        }
                    }
                }
            }
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            Button(onClick = onClick) {
                Text("Suivant")
            }
        }
    }
}

@Composable
fun StepThree(
    hydrantVisite: HydrantVisiteWithAnomalies,
    onValueChange: (HydrantVisiteWithAnomalies) -> Unit = {},
    onClick: () -> Unit
) {
    Column(
        Modifier
            .fillMaxHeight()
            .padding(10.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Text("Observations")
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 128.dp),
                value = hydrantVisite.hydrantVisite.observations ?: "",
                onValueChange = {
                    onValueChange(
                        hydrantVisite.copy(
                            hydrantVisite = hydrantVisite.hydrantVisite.copy(observations = it),
                        )
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
            Button(onClick = onClick, enabled = hydrantVisite.hydrantVisite.isValid) {
                Text("Valider")
            }
        }
    }
}

data class Option(
    var checked: Boolean,
    var onCheckedChange: (Boolean) -> Unit = {},
    val label: @Composable () -> Unit,
    var enabled: Boolean = true
)
