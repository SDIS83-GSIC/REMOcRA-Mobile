package fr.sdis83.remocra.mobile.ui.layout

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import fr.sdis83.remocra.mobile.navigation.Screens
import kotlinx.coroutines.launch

@Composable
fun Layout(
    navController: NavController?,
    content: @Composable (PaddingValues) -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    val contextForToast = LocalContext.current.applicationContext
    val items = listOf(
        NavigationItem(Icons.Default.Face, Screens.Hydrants),
        NavigationItem(Icons.Default.Face, Screens.Tournees),
        NavigationItem(Icons.Default.Face, Screens.Sync),
        NavigationItem(Icons.Default.Settings, Screens.Settings)
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
                        icon = { Icon(item.icon, contentDescription = null) },
                        label = { Text(item.screen.route) },
                        selected = item == selectedItem.value,
                        onClick = {
                            navController?.navigate(item.screen.route)
                            selectedItem.value = item
                            Toast.makeText(contextForToast, item.screen.route, Toast.LENGTH_LONG)
                                .show()
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                }
            }
        }

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
                    contentColor = Color.Green,
                ) {
                    Icon(imageVector = Icons.Filled.Home, contentDescription = "Add")
                }
            },
            content = content
        )
    }
}

data class NavigationItem(
    val icon: ImageVector,
    val screen: Screens
)

@Preview(showSystemUi = true, device = "spec:width=1280dp,height=800dp,dpi=480")
@Composable
fun homeScreenPreview() {
    Box {
        Layout(null)
    }
}

