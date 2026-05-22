package fr.sdis83.remocra.mobile.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import fr.sdis83.remocra.mobile.utils.pxToDp

@Composable
fun SyncStatBadge(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    containerColor: Color,
    contentColor: Color,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(25.pxToDp),
        color = containerColor,
        tonalElevation = 1.pxToDp,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.pxToDp, vertical = 10.pxToDp),
        ) {
            Text(
                text = label,
                color = contentColor.copy(alpha = 0.9f),
                fontWeight = FontWeight.SemiBold,
                fontSize = 1.5.em,
            )
            Text(
                text = value,
                color = contentColor,
                fontWeight = FontWeight.Bold,
                fontSize = 2.em,
            )
        }
    }
}
