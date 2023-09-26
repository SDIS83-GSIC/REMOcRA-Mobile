package fr.sdis83.remocra.mobile.ui.screens.tournees

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import fr.sdis83.remocra.mobile.R
import fr.sdis83.remocra.mobile.viewmodels.ChoixTourneeViewModel

@Composable
fun AnnulerReservationTourneeDialog(choixTourneeViewModel: ChoixTourneeViewModel, idTournee: Long, onDismiss: () -> Unit) {
    val context = LocalContext.current

    Dialog(
        onDismissRequest = { onDismiss() },
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
        ),
    ) {
        Card(
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.size(1600.dp, 300.dp),
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .background(Color.White),
            ) {
                Text(
                    text = stringResource(R.string.annuler_reservation),
                    modifier = Modifier.padding(10.dp),
                    fontSize = 25.sp,
                    fontWeight = FontWeight.Bold,
                )

                Row(
                    Modifier
                        .padding(15.dp)
                        .fillMaxHeight(0.5f),
                ) {
                    Text(
                        text = stringResource(id = R.string.warning_annuler_reservation),
                    )
                }
                Row(Modifier.padding(top = 10.dp)) {
                    OutlinedButton(
                        onClick = { onDismiss() },
                        Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                            .weight(1F),
                    ) {
                        Text(text = stringResource(R.string.non))
                    }

                    Button(
                        onClick = {
                            // Fait un appel pour réserver les tournées selectionnées
                            choixTourneeViewModel.annulerReservationTournee(context, idTournee)
                            onDismiss()
                        },
                        Modifier
                            .padding(8.dp)
                            .weight(1F),
                    ) {
                        Text(
                            text = stringResource(id = R.string.oui),
                        )
                    }
                }
            }
        }
    }
}
