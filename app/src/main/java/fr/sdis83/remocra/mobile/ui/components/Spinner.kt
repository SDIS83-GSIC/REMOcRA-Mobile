package fr.sdis83.remocra.mobile.ui.components

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.toSize

@Composable
fun <T> Spinner(
    modifier: Modifier = Modifier,
    items: List<T>,
    value: T? = items.firstOrNull(),
    valueToString: T.() -> String,
    label: String,
    onSelectionChanged: (selection: T) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    var selectedOption by remember { mutableStateOf(value) }
    var textfieldSize by remember { mutableStateOf(Size.Zero) }

    val icon = if (expanded) {
        Icons.Filled.ArrowDropUp
    } else {
        Icons.Filled.ArrowDropDown
    }

    Box(modifier = modifier) {
        OutlinedTextField(
            value = items.find { i -> i == value }?.valueToString() ?: "",
            onValueChange = { selectedOption?.valueToString() ?: "" },
            modifier = Modifier
                .fillMaxWidth()
                .onGloballyPositioned { coordinates ->
                    textfieldSize = coordinates.size.toSize()
                }
                .pointerInput(Unit) {
                    awaitEachGesture {
                        awaitFirstDown(pass = PointerEventPass.Initial)
                        val upEvent = waitForUpOrCancellation(pass = PointerEventPass.Initial)
                        if (upEvent != null) {
                            expanded = !expanded
                        }
                    }
                },
            label = { Text(label) },
            trailingIcon = {
                Icon(imageVector = icon, contentDescription = null)
            },
            readOnly = true,
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .width(with(LocalDensity.current) { textfieldSize.width.toDp() }),
        ) {
            items.forEach { item ->
                DropdownMenuItem(
                    onClick = {
                        selectedOption = item
                        onSelectionChanged(item)
                        expanded = false
                    },
                    text = { Text(text = item.valueToString()) },
                )
            }
        }
    }
}
