package com.gymrat.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gymrat.app.ui.theme.GymRatColors
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BirthdayScreen(
    username: String,
    initialBirthdateIso: String?,
    onSave: (birthdateIso: String) -> Unit
) {
    val gradient = Brush.verticalGradient(
        colors = listOf(
            GymRatColors.VoidBlack,
            GymRatColors.VoidPurpleDeep,
            GymRatColors.VoidBlack
        )
    )

    val isoFormatter = remember { DateTimeFormatter.ISO_LOCAL_DATE }
    val displayFormatter = remember { DateTimeFormatter.ofPattern("dd/MM/yy", Locale.getDefault()) }

    var pickedDate by remember(initialBirthdateIso) {
        mutableStateOf(
            initialBirthdateIso?.let { runCatching { LocalDate.parse(it, isoFormatter) }.getOrNull() }
        )
    }
    var pickerOpen by remember { mutableStateOf(false) }

    val dateText = remember(pickedDate) {
        pickedDate?.let { displayFormatter.format(it) } ?: ""
    }

    VoidPatternBackdrop(background = gradient) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(22.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Quick setup",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Black
            )
            Spacer(Modifier.height(10.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = GymRatColors.VoidCard)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Birthday", fontWeight = FontWeight.Black)
                    Text(
                        text = "This helps personalize your stats, $username.",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
                    )

                    OutlinedTextField(
                        value = dateText,
                        onValueChange = { },
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true,
                        singleLine = true,
                        label = { Text("DD/MM/YY") },
                        trailingIcon = {
                            IconButton(onClick = { pickerOpen = true }) {
                                Icon(Icons.Outlined.ExpandMore, contentDescription = "Pick date")
                            }
                        }
                    )

                    Spacer(Modifier.height(4.dp))

                    GlowNextButton(
                        text = "Next →",
                        enabled = pickedDate != null,
                        onClick = {
                            val d = pickedDate ?: return@GlowNextButton
                            onSave(isoFormatter.format(d))
                        }
                    )
                }
            }
        }

        if (pickerOpen) {
            val initMillis = pickedDate?.atStartOfDay(ZoneId.systemDefault())?.toInstant()?.toEpochMilli()
            val state = rememberDatePickerState(initialSelectedDateMillis = initMillis)
            DatePickerDialog(
                onDismissRequest = { pickerOpen = false },
                confirmButton = {
                    androidx.compose.material3.TextButton(
                        onClick = {
                            val millis = state.selectedDateMillis
                            if (millis != null) {
                                val date = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
                                pickedDate = date
                            }
                            pickerOpen = false
                        }
                    ) { Text("OK") }
                },
                dismissButton = {
                    androidx.compose.material3.TextButton(onClick = { pickerOpen = false }) { Text("Cancel") }
                }
            ) {
                DatePicker(state = state)
            }
        }
    }
}
