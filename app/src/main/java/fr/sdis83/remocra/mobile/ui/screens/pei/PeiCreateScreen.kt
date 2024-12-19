package fr.sdis83.remocra.mobile.ui.screens.pei

import android.app.Application
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.GpsNotFixed
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.em
import androidx.navigation.NavController
import fr.sdis83.remocra.mobile.R
import fr.sdis83.remocra.mobile.database.Gestionnaire
import fr.sdis83.remocra.mobile.database.Nature
import fr.sdis83.remocra.mobile.database.NatureDeci
import fr.sdis83.remocra.mobile.ui.components.SearchSpinner
import fr.sdis83.remocra.mobile.ui.components.Spinner
import fr.sdis83.remocra.mobile.utils.pxToDp
import fr.sdis83.remocra.mobile.viewmodels.MapViewModel
import fr.sdis83.remocra.mobile.viewmodels.PeiCreateViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch

@Composable
fun PeiCreateScreen(
    navController: NavController,
    mapViewModel: MapViewModel,
) {
    val context = LocalContext.current
    val peiCreateViewModel = PeiCreateViewModel(context.applicationContext as Application)

    PeiCreateScreen(peiCreateViewModel, mapViewModel, navController)
}

@Composable
private fun PeiCreateScreen(
    peiCreateViewModel: PeiCreateViewModel,
    mapViewModel: MapViewModel,
    navController: NavController,
) {
    val coroutineScope = rememberCoroutineScope()
    val peiForm by peiCreateViewModel.peiCreateState.collectAsState()

    Log.e("peiForm", peiForm.toString())

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
                    Button(onClick = { navController.popBackStack() }) {
                        Text(
                            text = stringResource(id = R.string.retour),
                        )
                    }
                    Text(
                        text = stringResource(id = R.string.creationPei),
                        fontSize = 5.em,
                        fontWeight = FontWeight.Bold,
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.pxToDp),
                ) {
                    PeiCreateForm(
                        coroutineScope = coroutineScope,
                        mapViewModel = mapViewModel,
                        peiCreateViewModel = peiCreateViewModel,
                        navController = navController,
                        peiForm = peiForm,
                    )
                }
            }
        }
    }
}

@Composable
private fun PeiCreateForm(
    coroutineScope: CoroutineScope,
    mapViewModel: MapViewModel,
    peiCreateViewModel: PeiCreateViewModel,
    navController: NavController,
    peiForm: PeiCreateViewModel.PeiForm,
) {
    val natureList by peiCreateViewModel.natureList.observeAsState()
    val natureDeciList by peiCreateViewModel.natureDeciList.observeAsState()
    val gestionnaireList by peiCreateViewModel.gestionnaireList.observeAsState()
    var withGps by remember { mutableStateOf(false) }
    var x: Double? by remember { mutableStateOf(null) }
    var y: Double? by remember { mutableStateOf(null) }

    DisposableEffect(LocalLifecycleOwner.current) {
        onDispose {
            mapViewModel.showCenter(false)
        }
    }

    LaunchedEffect(Unit) {
        snapshotFlow { mapViewModel.mapCenter.value }
            .debounce(100).collect {
                if (withGps && it != null) {
                    x = it.longitude
                    y = it.latitude
                }
            }
    }

    if (withGps && peiForm.x != x && peiForm.y != y) {
        peiCreateViewModel.updateForm(
            peiForm.copy(
                x = x,
                y = y,
                lon = x,
                lat = y,
            ),
        )
    }

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .padding(10.pxToDp)
            .verticalScroll(rememberScrollState()),
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = if (x != null) x.toString() else "",
                onValueChange = {
                    it.toDoubleOrNull()?.let { value ->
                        x = value
                        peiCreateViewModel.updateForm(
                            peiForm.copy(
                                x = value,
                                lon = value, // FIXME
                            ),
                        )
                    }
                },
                label = {
                    Text(text = stringResource(id = R.string.x))
                },
                placeholder = {
                    Text(text = stringResource(id = R.string.x))
                },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            )
            OutlinedTextField(
                value = if (y != null) y.toString() else "",
                onValueChange = {
                    it.toDoubleOrNull()?.let { value ->
                        y = value
                        peiCreateViewModel.updateForm(
                            peiForm.copy(
                                y = value,
                                lat = value, // FIXME
                            ),
                        )
                    }
                },
                label = {
                    Text(text = stringResource(id = R.string.y))
                },
                placeholder = {
                    Text(text = stringResource(id = R.string.y))
                },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            )
            IconButton(onClick = {
                withGps = !withGps
                mapViewModel.showCenter(withGps)
            }) {
                Icon(imageVector = if (withGps) Icons.Filled.GpsFixed else Icons.Filled.GpsNotFixed, contentDescription = "")
            }
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            Spinner(
                modifier = Modifier.weight(1f),
                items = natureList ?: listOf(),
                value = natureList?.find { i -> i.natureId == peiForm.nature?.natureId },
                valueToString = Nature::natureLibelle,
                label = stringResource(id = R.string.type),
                onSelectionChanged = {
                    peiCreateViewModel.updateForm(
                        peiForm.copy(
                            nature = it,
                        ),
                    )
                },
            )
            Spinner(
                modifier = Modifier.weight(1f),
                items = natureDeciList ?: listOf(),
                value = natureDeciList?.find { i -> i.natureDeciId == peiForm.natureDeci?.natureDeciId },
                valueToString = NatureDeci::natureDeciLibelle,
                label = stringResource(id = R.string.statut),
                onSelectionChanged = {
                    peiCreateViewModel.updateForm(
                        peiForm.copy(
                            natureDeci = it,
                        ),
                    )
                },
            )
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            SearchSpinner(
                items = gestionnaireList?.sortedBy { it.gestionnaireLibelle } ?: listOf(),
                value = gestionnaireList?.find { i -> i.gestionnaireId == peiForm.gestionnaire?.gestionnaireId },
                valueToString = Gestionnaire::gestionnaireLibelle,
                label = stringResource(id = R.string.gestionnaire),
                onSelectionChanged = {
                    peiCreateViewModel.updateForm(
                        peiForm.copy(
                            gestionnaire = it,
                        ),
                    )
                },
            )
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 128.pxToDp),
                value = peiForm.observation ?: "",
                onValueChange = {
                    peiCreateViewModel.updateForm(
                        peiForm = peiForm.copy(observation = it),
                    )
                },
                label = {
                    Text(text = stringResource(id = R.string.observations))
                },
                placeholder = {
                    Text(text = stringResource(id = R.string.observations))
                },
                singleLine = false,
            )
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick = {
                    coroutineScope.launch {
                        peiCreateViewModel.save()
                        navController.popBackStack()
                    }
                },
                enabled = peiForm.isValid,
            ) {
                Text(stringResource(id = R.string.valider))
            }
        }
    }
}
