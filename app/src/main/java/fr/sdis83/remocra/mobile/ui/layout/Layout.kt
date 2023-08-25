package fr.sdis83.remocra.mobile.ui.layout

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import fr.sdis83.remocra.mobile.navigation.Screens
import kotlinx.coroutines.launch

@Composable
fun Layout(
    navController: NavController?,
    content: @Composable (PaddingValues) -> Unit = {},
) {
    val scope = rememberCoroutineScope()
    val items = listOf(
        Screens.Tournees,
        Screens.Sync,
        Screens.Settings,
    )
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val selectedItem = remember { mutableStateOf(items[0]) }
    ModalNavigationDrawer(
        gesturesEnabled = drawerState.isOpen,
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(Modifier.height(12.dp))
                items.forEach { item ->
                    NavigationDrawerItem(
                        icon = { Icon(item.icon!!, contentDescription = item.title!!) },
                        label = { Text(item.title!!) },
                        selected = item == selectedItem.value,
                        onClick = {
                            navController?.navigate(item.route)
                            selectedItem.value = item
                            scope.launch {
                                drawerState.close()
                            }
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
                    )
                }
            }
        },

    ) {
        Scaffold(
            floatingActionButton = {
                FloatingActionButton(
                    modifier = Modifier
                        .padding(all = 16.dp),
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

@Preview(showSystemUi = true, device = "spec:width=1280dp,height=800dp,dpi=480")
@Composable
fun homeScreenPreview() {
    Box {
        Layout(null)
    }
}
