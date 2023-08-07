package fr.sdis83.remocra.mobile.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@Composable
fun HeaderAppBar(title: String, returnAction: () -> Unit){
    Row(modifier = Modifier.fillMaxWidth()
        .height(100.dp),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically) {
        IconButton(
            modifier = Modifier.padding(20.dp),
            onClick = { returnAction() }
        ) {
            Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "return",
                modifier = Modifier.size(30.dp)
            )
        }
        Text(
            text = title,
            fontWeight = FontWeight.Bold,
            fontSize = 35.sp)
    }
}