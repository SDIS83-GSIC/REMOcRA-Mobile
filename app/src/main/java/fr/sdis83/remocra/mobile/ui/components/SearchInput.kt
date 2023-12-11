package fr.sdis83.remocra.mobile.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import fr.sdis83.remocra.mobile.utils.pxToDp

@Composable
fun SearchInput(
    search: String,
    onChange: (String) -> Unit,
    size: Int,
) {
    Column(
        Modifier.padding(10.pxToDp),
    ) {
        OutlinedTextField(
            modifier = Modifier.padding(10.pxToDp),
            value = search,
            onValueChange = { str -> onChange(str) },
            label = { Text(text = "Recherche") },
            placeholder = { Text(text = "Recherche") },
            singleLine = true,
        )
        Spacer(modifier = Modifier.width(16.pxToDp))
        Text(text = "$size résultats")
    }
}
