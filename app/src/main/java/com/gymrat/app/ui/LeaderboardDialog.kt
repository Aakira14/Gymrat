package com.gymrat.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.gymrat.app.data.FirebaseLeaderboardRepository
import com.gymrat.app.data.LeaderboardRepository
import com.gymrat.app.ui.theme.GymRatColors

@Composable
fun LeaderboardDialog(
    currentUsername: String?,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val repo = remember { LeaderboardRepository(context) }
    val firebaseRepo = remember { FirebaseLeaderboardRepository(context) }

    val firebaseError by firebaseRepo.lastError.collectAsState(initial = null)

    val entries by (
        if (firebaseRepo.enabled) firebaseRepo.topEntries(limit = 50) else repo.entries
        ).collectAsState(initial = emptyList())

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        val gradient = Brush.verticalGradient(
            colors = listOf(
                GymRatColors.VoidBlack,
                GymRatColors.VoidPurpleDeep,
                GymRatColors.VoidBlack
            )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(gradient)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Leaderboard", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
                Button(onClick = onDismiss, shape = RoundedCornerShape(14.dp)) { Text("Close") }
            }

            Spacer(Modifier.height(4.dp))

            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                itemsIndexed(entries.take(50)) { idx, entry ->
                    val me = !currentUsername.isNullOrBlank() && currentUsername == entry.username
                    val border = if (me) MaterialTheme.colorScheme.primary else Color.Transparent
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = GymRatColors.VoidCard),
                        border = androidx.compose.foundation.BorderStroke(2.dp, border)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("#${idx + 1}  ${entry.username}", fontWeight = FontWeight.SemiBold)
                                if (me) Text("You", color = MaterialTheme.colorScheme.secondary)
                            }
                            Text("${entry.xp} XP", fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }

                if (entries.isEmpty()) {
                    item {
                        Text(
                            "No XP yet. Complete a challenge to appear here.",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            Text(
                text = when {
                    !firebaseRepo.enabled ->
                        "Firebase not configured yet, showing local leaderboard. Add `google-services.json` to enable global leaderboard."
                    !firebaseError.isNullOrBlank() ->
                        "Firebase error: $firebaseError"
                    else ->
                        "Global leaderboard (Firebase)."
                },
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
            )
        }
    }
}
