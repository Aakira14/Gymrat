package com.gymrat.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.dp
import com.gymrat.app.ui.theme.GymRatColors

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GymLevelScreen(
    initialLevel: String?,
    onNext: (level: String) -> Unit
) {
    val gradient = Brush.verticalGradient(
        colors = listOf(
            GymRatColors.VoidBlack,
            GymRatColors.VoidPurpleDeep,
            GymRatColors.VoidBlack
        )
    )

    var level by remember(initialLevel) { mutableStateOf(initialLevel) }

    VoidPatternBackdrop(background = gradient) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(22.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Gym level",
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
                    Text("Pick your level", fontWeight = FontWeight.Black)
                    FlowRow {
                        listOf(
                            "Beginner" to "beginner",
                            "Intermediate" to "intermediate",
                            "Pro" to "pro"
                        ).forEach { (label, v) ->
                            OptionPill(text = label, selected = level == v) { level = v }
                        }
                    }

                    Spacer(Modifier.height(6.dp))
                    GlowNextButton(
                        text = "Next",
                        enabled = !level.isNullOrBlank(),
                        onClick = { onNext(level!!) }
                    )
                }
            }
        }
    }
}

