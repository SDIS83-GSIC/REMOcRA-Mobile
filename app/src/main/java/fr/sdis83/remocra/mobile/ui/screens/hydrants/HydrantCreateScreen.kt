package fr.sdis83.remocra.mobile.ui.screens.hydrants

import android.app.Application
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
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import fr.sdis83.remocra.mobile.R
import fr.sdis83.remocra.mobile.database.Gestionnaire
import fr.sdis83.remocra.mobile.database.TypeHydrantNature
import fr.sdis83.remocra.mobile.database.TypeHydrantNatureDeci
import fr.sdis83.remocra.mobile.ui.components.Spinner
import fr.sdis83.remocra.mobile.viewmodels.HydrantCreateViewModel
import fr.sdis83.remocra.mobile.viewmodels.MapViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun HydrantCreateScreen(
    navController: NavController,
    mapViewModel: MapViewModel,
) {
    val context = LocalContext.current
    val hydrantCreateViewModel = HydrantCreateViewModel(context.applicationContext as Application)

    HydrantCreateScreen(hydrantCreateViewModel, mapViewModel, navController)
}

@Composable
private fun HydrantCreateScreen(
    hydrantCreateViewModel: HydrantCreateViewModel,
    mapViewModel: MapViewModel,
    navController: NavController,
) {
    val coroutineScope = rememberCoroutineScope()
    val hydrantForm by hydrantCreateViewModel.hydrantCreateState.collectAsState()

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
                        .padding(10.dp),
                ) {
                    Button(onClick = { navController.popBackStack() }) {
                        Text(
                            text = stringResource(id = R.string.retour),
                        )
                    }
                    Text(
                        text = stringResource(id = R.string.creationHydrant),
                        fontSize = MaterialTheme.typography.headlineLarge.fontSize,
                        fontWeight = FontWeight.Bold,
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                ) {
                    HydrantCreateForm(
                        coroutineScope = coroutineScope,
                        mapViewModel = mapViewModel,
                        hydrantCreateViewModel = hydrantCreateViewModel,
                        navController = navController,
                        hydrantForm = hydrantForm,
                    )
                }
            }
        }
    }
}

@Composable
private fun HydrantCreateForm(
    coroutineScope: CoroutineScope,
    mapViewModel: MapViewModel,
    hydrantCreateViewModel: HydrantCreateViewModel,
    navController: NavController,
    hydrantForm: HydrantCreateViewModel.HydrantForm,
) {
    val typeHydrantNatureList by hydrantCreateViewModel.typeHydrantNatureList.observeAsState()
    val typeHydrantNatureDeciList by hydrantCreateViewModel.typeHydrantNatureDeciList.observeAsState()
    val gestionnaireList by hydrantCreateViewModel.gestionnaireList.observeAsState()

    val point = mapViewModel.mapCenter

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .padding(10.dp)
            .verticalScroll(rememberScrollState()),
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = hydrantForm.x?.toString() ?: "",
                onValueChange = {
                    it.toDoubleOrNull()?.let { value ->
                        hydrantCreateViewModel.updateForm(
                            hydrantForm.copy(
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
                value = hydrantForm.y?.toString() ?: "",
                onValueChange = {
                    it.toDoubleOrNull()?.let { value ->
                        hydrantCreateViewModel.updateForm(
                            hydrantForm.copy(
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
                point.value?.let {
                    hydrantCreateViewModel.updateForm(
                        hydrantForm = hydrantForm.copy(
                            x = it.longitude,
                            y = it.latitude,
                            lon = it.longitude,
                            lat = it.latitude,
                        ),
                    )
                }
            }) {
                Icon(imageVector = Icons.Filled.GpsFixed, contentDescription = "")
            }
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            Spinner(
                modifier = Modifier.weight(1f),
                items = typeHydrantNatureList ?: listOf(),
                value = typeHydrantNatureList?.find { i -> i.idRemocra == hydrantForm.nature?.idRemocra },
                valueToString = TypeHydrantNature::nom,
                label = stringResource(id = R.string.type),
                onSelectionChanged = {
                    hydrantCreateViewModel.updateForm(
                        hydrantForm.copy(
                            nature = it,
                        ),
                    )
                },
            )
            Spinner(
                modifier = Modifier.weight(1f),
                items = typeHydrantNatureDeciList ?: listOf(),
                value = typeHydrantNatureDeciList?.find { i -> i.idRemocra == hydrantForm.natureDeci?.idRemocra },
                valueToString = TypeHydrantNatureDeci::nom,
                label = stringResource(id = R.string.statut),
                onSelectionChanged = {
                    hydrantCreateViewModel.updateForm(
                        hydrantForm.copy(
                            natureDeci = it,
                        ),
                    )
                },
            )
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            Spinner(
                items = gestionnaireList?.sortedBy { it.nom } ?: listOf(),
                value = gestionnaireList?.find { i -> i.idGestionnaire == hydrantForm.gestionnaire?.idGestionnaire },
                valueToString = Gestionnaire::nom,
                label = stringResource(id = R.string.gestionnaire),
                onSelectionChanged = {
                    hydrantCreateViewModel.updateForm(
                        hydrantForm.copy(
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
                    .defaultMinSize(minHeight = 128.dp),
                value = hydrantForm.observation ?: "",
                onValueChange = {
                    hydrantCreateViewModel.updateForm(
                        hydrantForm = hydrantForm.copy(observation = it),
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
                        hydrantCreateViewModel.save()
                        navController.popBackStack()
                    }
                },
                enabled = hydrantForm.isValid,
            ) {
                Text(stringResource(id = R.string.valider))
            }
        }
    }
}
