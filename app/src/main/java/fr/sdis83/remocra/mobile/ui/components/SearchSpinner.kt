package fr.sdis83.remocra.mobile.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.toSize
import androidx.compose.ui.window.PopupProperties
import fr.sdis83.remocra.mobile.utils.pxToDp

@Composable
fun <T> SearchSpinner(
    modifier: Modifier = Modifier,
    items: List<T>,
    value: T?,
    valueToString: T.() -> String,
    label: String,
    onSelectionChanged: (selection: T?) -> Unit,
) {
    var textfieldSize by remember { mutableStateOf(Size.Zero) }
    var selectedOption = remember { mutableStateOf(value) }
    var search = remember { mutableStateOf<String?>(null) }
    var expanded = remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        OutlinedTextField(
            value = search.value ?: items.find { i -> i == selectedOption.value }?.valueToString()
                ?: "",
            onValueChange = {
                search.value = it
                expanded.value = !it.isNullOrBlank()
            },
            modifier = Modifier
                .fillMaxWidth()
                .onGloballyPositioned { coordinates ->
                    textfieldSize = coordinates.size.toSize()
                }
                .onFocusChanged {
                    if (!it.hasFocus) {
                        search.value = null
                    }
                },
            label = { Text(label) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = null,
                )
            },
            trailingIcon = {
                if (selectedOption.value != null || !search.value.isNullOrEmpty()) {
                    IconButton(onClick = {
                        search.value = null
                        selectedOption.value = null
                        onSelectionChanged(null)
                    }) {
                        Icon(imageVector = Icons.Filled.Delete, contentDescription = null)
                    }
                }
            },
        )
        Options(
            search,
            expanded,
            textfieldSize,
            selectedOption,
            items,
            valueToString,
            onSelectionChanged,
        )
    }
}

@Composable
private fun <T> Options(
    search: MutableState<String?>,
    expanded: MutableState<Boolean>,
    textfieldSize: Size,
    selectedOption: MutableState<T?>,
    items: List<T>,
    valueToString: T.() -> String,
    onSelectionChanged: (selection: T) -> Unit,
) {
    DropdownMenu(
        properties = PopupProperties(focusable = false),
        expanded = expanded.value,
        onDismissRequest = { expanded.value = false },
        modifier = Modifier
            .requiredSizeIn(maxHeight = 200.pxToDp)
            .width(with(LocalDensity.current) { textfieldSize.width.toDp() }),
    ) {
        if (!search.value.isNullOrBlank()) {
            if (search.value!!.length < 2) {
                Text(modifier = Modifier.padding(12.pxToDp), text = "Entrez au moins 2 lettres")
            } else {
                items.filter {
                    it.valueToString().lowercase().contains(search.value!!.lowercase())
                }.let {
                    if (it.isNullOrEmpty()) {
                        Text(modifier = Modifier.padding(12.pxToDp), text = "Aucun résultat")
                    } else {
                        it.forEach { item ->
                            DropdownMenuItem(
                                onClick = {
                                    selectedOption.value = item
                                    onSelectionChanged(item)
                                    expanded.value = false
                                    search.value = null
                                },
                                text = { Text(text = item.valueToString()) },
                            )
                        }
                    }
                }
            }
        }
    }
}
