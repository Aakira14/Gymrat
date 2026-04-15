package com.gymrat.app.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.gymrat.app.auth.ProfileGender

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun EditProfileDialog(
    currentGender: ProfileGender?,
    currentAvatarResName: String?,
    onDismiss: () -> Unit,
    onSave: (ProfileGender, String) -> Unit
) {
    var gender by remember { mutableStateOf(currentGender) }
    var avatar by remember { mutableStateOf(currentAvatarResName) }
    val context = LocalContext.current

    val male = listOf("avatar_m1", "avatar_m2", "avatar_m3", "avatar_m4", "avatar_m5")
    val female = listOf("avatar_f1", "avatar_f2", "avatar_f3", "avatar_f4", "avatar_f5")

    val list = when (gender) {
        ProfileGender.Male -> male
        ProfileGender.Female -> female
        null -> emptyList()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    val g = gender ?: return@Button
                    val a = avatar ?: return@Button
                    onSave(g, a)
                },
                enabled = gender != null && avatar != null,
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss, shape = RoundedCornerShape(14.dp)) { Text("Close") }
        },
        title = { Text("Edit profile") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Select gender")
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    GenderChip(
                        label = "Male",
                        selected = gender == ProfileGender.Male,
                        onClick = {
                            gender = ProfileGender.Male
                            avatar = avatar ?: "avatar_m1"
                        }
                    )
                    GenderChip(
                        label = "Female",
                        selected = gender == ProfileGender.Female,
                        onClick = {
                            gender = ProfileGender.Female
                            avatar = avatar ?: "avatar_f1"
                        }
                    )
                }

                if (gender != null) {
                    Text("Choose avatar")
                    FlowRow(
                        maxItemsInEachRow = 5,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        list.forEach { resName ->
                            val resId = remember(resName) {
                                context.resources.getIdentifier(resName, "drawable", context.packageName)
                            }
                            val selected = avatar == resName
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(
                                    2.dp,
                                    if (selected) MaterialTheme.colorScheme.primary else Color.Transparent
                                ),
                                onClick = { avatar = resName }
                            ) {
                                Box(
                                    modifier = Modifier
                                        .padding(6.dp)
                                        .size(58.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (resId != 0) {
                                        Image(
                                            painter = painterResource(resId),
                                            contentDescription = null,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .size(58.dp)
                                                .clip(RoundedCornerShape(14.dp))
                                        )
                                    } else {
                                        Text("?")
                                    }
                                }
                            }
                        }
                    }
                } else {
                    Text(
                        "Pick gender to see avatars.",
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    )
}

@Composable
private fun GenderChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val border = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
    Card(
        shape = RoundedCornerShape(999.dp),
        border = BorderStroke(2.dp, border),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        onClick = onClick
    ) {
        Text(label, modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp))
    }
}
