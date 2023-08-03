package fr.sdis83.remocra.mobile.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import fr.sdis83.remocra.mobile.database.Contact
import fr.sdis83.remocra.mobile.navigation.Screens

@Composable
fun ContactCard(contact: Contact, navController: NavController) {
    Card(Modifier.padding(5.dp)) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            var nomComplet: String = "${contact.nom ?: ""} ${contact.prenom ?: ""}"
            Column(
                Modifier
                    .padding(5.dp)
                    .weight(.7f)) {
                Text(
                    text = if (nomComplet!=" ") nomComplet else "n/a",
                    fontWeight = FontWeight.Bold,
                    fontSize = 30.sp
                )
                Text(
                    text = contact.fonction ?: "Fonction n/a",
                    fontSize = 20.sp
                )
            }
            Column(Modifier.padding(5.dp)) {
                IconButton(
                    onClick = {
                        navController.navigate(
                            Screens.EditContact.route
                                .replace(
                                    oldValue = "{idGestionnaire}",
                                    newValue = contact.idGestionnaire.toString()
                                )
                                .replace(
                                    oldValue = "{idContact}",
                                    newValue = contact.idContact.toString()
                                )
                        )
                    }
                ) {
                    Icon(imageVector = Icons.Filled.Edit, contentDescription = "EditGestionnaire", Modifier.size(30.dp))
                }
            }
        }
    }
}