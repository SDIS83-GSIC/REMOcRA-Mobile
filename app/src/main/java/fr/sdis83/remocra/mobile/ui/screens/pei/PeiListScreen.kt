package fr.sdis83.remocra.mobile.ui.screens.pei

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.navigation.NavController
import fr.sdis83.remocra.mobile.R
import fr.sdis83.remocra.mobile.utils.pxToDp
import fr.sdis83.remocra.mobile.viewmodels.MapViewModel
import fr.sdis83.remocra.mobile.viewmodels.PeiListViewModel
import kotlinx.coroutines.launch
import java.util.UUID

@Composable
fun PeiListScreen(navController: NavController, mapViewModel: MapViewModel) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val peiListViewModel = PeiListViewModel(context.applicationContext as Application)
    val peiList by peiListViewModel.peiList.observeAsState()

    val openDialog = remember { mutableStateOf<UUID?>(null) }

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
                    Spacer(Modifier.height(16.pxToDp))
                    Text(
                        text = stringResource(id = R.string.hydrantCrees),
                        fontSize = 5.em,
                        fontWeight = FontWeight.Bold,
                    )
                }
                if (!peiList.isNullOrEmpty()) {
                    Row {
                        if (openDialog.value != null) {
                            AlertDialog(
                                onDismissRequest = {
                                    openDialog.value = null
                                },
                                title = {
                                    Text(text = stringResource(id = R.string.suppressionHydrant))
                                },
                                text = {
                                    Text(stringResource(id = R.string.confirmSuppressionHydrant))
                                },
                                confirmButton = {
                                    Button(
                                        onClick = {
                                            openDialog.value?.let {
                                                coroutineScope.launch {
                                                    peiListViewModel.deletePei(it)
                                                    openDialog.value = null
                                                }
                                            }
                                        },
                                    ) {
                                        Text(stringResource(id = R.string.oui))
                                    }
                                },
                                dismissButton = {
                                    Button(
                                        onClick = {
                                            openDialog.value = null
                                        },
                                    ) {
                                        Text(stringResource(id = R.string.non))
                                    }
                                },
                            )
                        }
                        LazyColumn {
                            items(peiList!!) { peiItem ->
                                Row(
                                    Modifier
                                        .padding(8.pxToDp)
                                        .fillMaxWidth(),
                                ) {
                                    Box(
                                        modifier =
                                        Modifier
                                            .clip(RoundedCornerShape(8.pxToDp))
                                            .background(Color(0xDDE9F3FF))
                                            .padding(16.pxToDp)
                                            .fillMaxWidth(),
                                    ) {
                                        Column {
                                            Row(modifier = Modifier.fillMaxWidth()) {
                                                Text(text = peiItem.pei.peiNumeroComplet ?: "N/A")
                                                Spacer(modifier = Modifier.width(16.pxToDp))
                                                IconButton(onClick = {
                                                    mapViewModel.goToPei(peiItem.pei.peiId, true)
                                                }) {
                                                    Icon(
                                                        imageVector = Icons.Filled.GpsFixed,
                                                        contentDescription = "",
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(16.pxToDp))
                                                IconButton(onClick = {
                                                    openDialog.value = peiItem.pei.peiId
                                                }) {
                                                    Icon(
                                                        imageVector = Icons.Filled.Delete,
                                                        contentDescription = "",
                                                    )
                                                }
                                            }
                                            Row(modifier = Modifier.fillMaxWidth()) {
                                                Text(
                                                    text = peiItem.nature.natureLibelle,
                                                )
                                            }
                                            Row(modifier = Modifier.fillMaxWidth()) {
                                                Text(
                                                    text = peiItem.natureDeci.natureDeciLibelle,
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
