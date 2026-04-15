package com.gymrat.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gymrat.app.ui.theme.GymRatColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutineScreen(
    initial: Map<String, String>,
    onSkip: () -> Unit,
    onDone: (map: Map<String, String>) -> Unit
) {
    val gradient = Brush.verticalGradient(
        colors = listOf(
            GymRatColors.VoidBlack,
            GymRatColors.VoidPurpleDeep,
            GymRatColors.VoidBlack
        )
    )

    val days = remember {
        listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
    }
    val categories = remember {
        listOf(
            "Chest",
            "Back",
            "Shoulders",
            "Biceps",
            "Triceps",
            "Forearms",
            "Legs",
            "Calves",
            "Core (Abs + Obliques + Lower Back)",
            "Mixed"
        )
    }

    val picked = remember(initial) { mutableStateMapOf<String, String>().also { it.putAll(initial) } }

    VoidPatternBackdrop(background = gradient) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(22.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(10.dp))
            Text(
                text = "Routine",
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
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Set your split (optional)", fontWeight = FontWeight.Black)
                    Text(
                        text = "Choose what you train each day.",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
                    )

                    days.forEach { day ->
                        DayPickerRow(
                            day = day,
                            value = picked[day].orEmpty(),
                            categories = categories,
                            onPick = { categoryOrNull ->
                                if (categoryOrNull.isNullOrBlank()) picked.remove(day)
                                else picked[day] = categoryOrNull
                            }
                        )
                    }

                    Spacer(Modifier.height(8.dp))
                    DualActionRow(
                        leftText = "Skip",
                        rightText = "Done!",
                        onLeft = onSkip,
                        onRight = { onDone(picked.toMap()) }
                    )
                }
            }
        }
    }
}

@Composable
private fun DayPickerRow(
    day: String,
    value: String,
    categories: List<String>,
    onPick: (String?) -> Unit
) {
    var expanded by remember(day) { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(day, modifier = Modifier.weight(0.38f), fontWeight = FontWeight.SemiBold)

        Box(modifier = Modifier.weight(0.62f)) {
            OutlinedTextField(
                value = if (value.isBlank()) "" else value,
                onValueChange = { },
                readOnly = true,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("Category") },
                trailingIcon = {
                    IconButton(onClick = { expanded = true }) {
                        Icon(Icons.Outlined.ExpandMore, contentDescription = "Pick")
                    }
                }
            )

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Rest / None") },
                    onClick = {
                        expanded = false
                        onPick(null)
                    }
                )
                categories.forEach { c ->
                    DropdownMenuItem(
                        text = { Text(c) },
                        onClick = {
                            expanded = false
                            onPick(c)
                        }
                    )
                }
            }
        }
    }
}
