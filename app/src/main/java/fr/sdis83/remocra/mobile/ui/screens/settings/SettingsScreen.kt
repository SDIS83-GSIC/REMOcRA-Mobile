package fr.sdis83.remocra.mobile.ui.screens.settings

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import fr.sdis83.remocra.mobile.R
import fr.sdis83.remocra.mobile.navigation.Screens
import fr.sdis83.remocra.mobile.ui.components.HeaderAppBar

@Composable
fun SettingScreen(navController: NavController?) {
    BackHandler { navController?.navigate(Screens.Tournees.route) }
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Top,
    ) {
        HeaderAppBar(
            title = stringResource(R.string.parametrage),
            returnAction = { navController?.popBackStack() },
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(50.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(0.6f),
            ) {
                Text(text = stringResource(R.string.gestionnaireST), fontWeight = FontWeight.Bold)
                Text(text = stringResource(R.string.gestionnaireSubST), fontWeight = FontWeight.Normal)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Button(
                        modifier = Modifier
                            .fillMaxWidth(.5f)
                            .padding(20.dp),
                        onClick = { navController?.navigate(Screens.CreateGestionnaire.route) },
                    ) {
                        Text(text = stringResource(R.string.createGestionnaireBTN))
                    }
                    Button(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        onClick = { navController?.navigate(Screens.ListGestionnaire.route) },
                    ) {
                        Text(text = stringResource(R.string.manageGestionnaireBTN))
                    }
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.1f)
                    .padding(10.dp),
            ) {
                Spacer(
                    modifier = Modifier
                        .width(2.dp)
                        .fillMaxHeight(.9f)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                )
            }
            Column(
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(text = stringResource(R.string.createPeiST), fontWeight = FontWeight.Bold)
                Text(text = stringResource(R.string.createPeiSubST), fontWeight = FontWeight.Normal)
                Button(onClick = { navController?.navigate(Screens.HydrantCreate.route) }) {
                    Text(text = stringResource(R.string.createPeiBTN))
                }
                Button(onClick = { navController?.navigate(Screens.HydrantList.route) }) {
                    Text(text = stringResource(R.string.listingPeiBTN))
                }
            }
        }
    }
}
