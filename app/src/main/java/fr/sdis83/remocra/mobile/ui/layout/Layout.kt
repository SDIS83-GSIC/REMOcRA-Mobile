package fr.sdis83.remocra.mobile.ui.layout

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Button
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.em
import androidx.navigation.NavController
import fr.sdis83.remocra.mobile.navigation.Screens
import fr.sdis83.remocra.mobile.utils.pxToDp
import kotlinx.coroutines.launch

@Composable
fun Layout(
    navController: NavController?,
    versionName: String,
    logout: (context: Context) -> Unit,
    content: @Composable (PaddingValues) -> Unit = {},
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val items = listOf(
        Screens.Tournees,
        Screens.Sync,
        Screens.SyncTournee,
        Screens.Settings,
        Screens.Export,
    )
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val selectedItem = remember { mutableStateOf(items[0]) }

    ModalNavigationDrawer(
        gesturesEnabled = drawerState.isOpen,
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(Modifier.height(12.pxToDp))
                Column(
                    Modifier.fillMaxHeight(0.8f),
                ) {
                    items.forEach { item ->
                        if (item.isVisible) {
                            NavigationDrawerItem(
                                icon = { Icon(item.icon!!, contentDescription = item.title!!) },
                                label = {
                                    Text(
                                        text = item.title!!,
                                        fontSize = 2.5.em,
                                    )
                                },
                                selected = item == selectedItem.value,
                                onClick = {
                                    navController?.navigate(item.route)
                                    selectedItem.value = item
                                    scope.launch {
                                        drawerState.close()
                                    }
                                },
                                modifier = Modifier
                                    .padding(NavigationDrawerItemDefaults.ItemPadding)
                                    .verticalScroll(
                                        rememberScrollState(),
                                    ),
                            )
                        }
                    }
                }
                Column(
                    Modifier
                        .fillMaxHeight(0.5f)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Button(
                        modifier = Modifier
                            .padding(all = 10.pxToDp),
                        onClick = {
                            logout(context)
                        },
                    ) {
                        Text(
                            text = "Se déconnecter",
                            fontWeight = FontWeight.Bold,
                            fontSize = 2.em,
                            textAlign = TextAlign.Center,
                        )
                    }
                }

                Column(
                    Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.Bottom,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = "Version : $versionName",
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        fontSize = 2.5.em,
                    )
                }
            }
        },

    ) {
        Scaffold(
            floatingActionButton = {
                FloatingActionButton(
                    modifier = Modifier
                        .padding(all = 16.pxToDp),
                    onClick = {
                        scope.launch {
                            drawerState.open()
                        }
                    },
                    shape = MaterialTheme.shapes.small.copy(CornerSize(percent = 50)),
                ) {
                    Icon(imageVector = Icons.Filled.Menu, contentDescription = "Menu")
                }
            },
            content = content,
        )
    }
}
