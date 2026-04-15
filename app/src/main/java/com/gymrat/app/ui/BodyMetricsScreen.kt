package com.gymrat.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.gymrat.app.ui.theme.GymRatColors

@Composable
fun BodyMetricsScreen(
    username: String,
    initialWeightKg: Float?,
    initialHeightCm: Float?,
    onNext: (weightKg: Float, heightCm: Float) -> Unit
) {
    val gradient = Brush.verticalGradient(
        colors = listOf(
            GymRatColors.VoidBlack,
            GymRatColors.VoidPurpleDeep,
            GymRatColors.VoidBlack
        )
    )

    var weightText by remember(initialWeightKg) { mutableStateOf(initialWeightKg?.toString().orEmpty()) }
    var heightText by remember(initialHeightCm) { mutableStateOf(initialHeightCm?.toString().orEmpty()) }

    val weightKg = weightText.toFloatOrNull()
    val heightCm = heightText.toFloatOrNull()
    val ok = weightKg != null && heightCm != null && weightKg > 0f && heightCm > 0f

    VoidPatternBackdrop(background = gradient) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(22.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Body metrics",
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
                    Text("Let’s personalize your tracking, $username.", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f))

                    Text("Your weight", fontWeight = FontWeight.Black)
                    OutlinedTextField(
                        value = weightText,
                        onValueChange = { weightText = filterDecimal(it) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        label = { Text("Weight") },
                        trailingIcon = { Text("kg", fontWeight = FontWeight.Bold) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )

                    Text("Your height", fontWeight = FontWeight.Black)
                    OutlinedTextField(
                        value = heightText,
                        onValueChange = { heightText = filterDecimal(it) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        label = { Text("Height") },
                        trailingIcon = { Text("cm", fontWeight = FontWeight.Bold) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )

                    Spacer(Modifier.height(4.dp))
                    GlowNextButton(
                        text = "Next",
                        modifier = Modifier,
                        enabled = ok,
                        onClick = { onNext(weightKg!!, heightCm!!) }
                    )
                }
            }
        }
    }
}

private fun filterDecimal(input: String): String {
    val trimmed = input.trim()
    val out = StringBuilder()
    var dot = false
    for (ch in trimmed) {
        if (ch.isDigit()) out.append(ch)
        else if (ch == '.' && !dot) {
            dot = true
            out.append(ch)
        }
    }
    return out.toString()
}
