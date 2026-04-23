package com.bfy.schedule_app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.bfy.schedule_app.ui.components.BfyButton
import com.bfy.schedule_app.ui.components.BfyButtonStyle
import com.bfy.schedule_app.ui.components.BfyTextField
import com.bfy.schedule_app.ui.state.ProfileUiState
import com.bfy.schedule_app.ui.theme.BfyTheme
import androidx.compose.material3.Surface
import org.jetbrains.compose.resources.stringResource
import schedule_app.composeapp.generated.resources.Res
import schedule_app.composeapp.generated.resources.badges
import schedule_app.composeapp.generated.resources.bio
import schedule_app.composeapp.generated.resources.change_password
import schedule_app.composeapp.generated.resources.dark_theme
import schedule_app.composeapp.generated.resources.edit_profile
import schedule_app.composeapp.generated.resources.focus_stats
import schedule_app.composeapp.generated.resources.full_name
import schedule_app.composeapp.generated.resources.language
import schedule_app.composeapp.generated.resources.logout
import schedule_app.composeapp.generated.resources.profile_email
import schedule_app.composeapp.generated.resources.rank
import schedule_app.composeapp.generated.resources.save_changes
import schedule_app.composeapp.generated.resources.task_stats
import schedule_app.composeapp.generated.resources.timezone

@Composable
fun ProfileScreen(
    state: ProfileUiState,
    onProfileUpdate: (String, String, String, String) -> Unit,
    onToggleTheme: (Boolean) -> Unit,
    onToggleLanguage: (Boolean) -> Unit,
    onLogout: () -> Unit
) {
    var fullName by remember(state.fullName) { mutableStateOf(state.fullName) }
    var email by remember(state.email) { mutableStateOf(state.email) }
    var bio by remember(state.bio) { mutableStateOf(state.bio) }
    var timezone by remember(state.timezone) { mutableStateOf(state.timezone) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = BfyTheme.dimens.spacing16),
        verticalArrangement = Arrangement.spacedBy(BfyTheme.dimens.spacing12)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = BfyTheme.dimens.cardElevation)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(BfyTheme.dimens.spacing16),
                    horizontalArrangement = Arrangement.spacedBy(BfyTheme.dimens.spacing12),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(BfyTheme.dimens.spacing32 * 2)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = initialsOf(state.fullName),
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(BfyTheme.dimens.spacing4)) {
                        Text(state.fullName, style = MaterialTheme.typography.titleLarge)
                        Text(state.email, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = BfyTheme.dimens.cardElevation)
            ) {
                Column(
                    modifier = Modifier.padding(BfyTheme.dimens.spacing16),
                    verticalArrangement = Arrangement.spacedBy(BfyTheme.dimens.spacing12)
                ) {
                    Text(text = stringResource(Res.string.edit_profile), style = MaterialTheme.typography.titleMedium)
                    BfyTextField(
                        value = fullName,
                        onValueChange = { fullName = it },
                        label = stringResource(Res.string.full_name)
                    )
                    BfyTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = stringResource(Res.string.profile_email)
                    )
                    BfyTextField(
                        value = bio,
                        onValueChange = { bio = it },
                        label = stringResource(Res.string.bio),
                        singleLine = false
                    )
                    BfyTextField(
                        value = timezone,
                        onValueChange = { timezone = it },
                        label = stringResource(Res.string.timezone)
                    )
                    BfyButton(
                        text = stringResource(Res.string.save_changes),
                        onClick = { onProfileUpdate(fullName.trim(), email.trim(), bio.trim(), timezone.trim()) }
                    )
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = BfyTheme.dimens.cardElevation)
            ) {
                Column(modifier = Modifier.padding(BfyTheme.dimens.spacing16)) {
                    Text(text = stringResource(Res.string.rank), style = MaterialTheme.typography.labelLarge)
                    Text(text = state.rank, style = MaterialTheme.typography.headlineMedium)
                    Text(text = "${state.totalExp} EXP", style = MaterialTheme.typography.bodyLarge)
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = BfyTheme.dimens.cardElevation)
            ) {
                Column(
                    modifier = Modifier.padding(BfyTheme.dimens.spacing16),
                    verticalArrangement = Arrangement.spacedBy(BfyTheme.dimens.spacing8)
                ) {
                    Text(text = stringResource(Res.string.focus_stats), style = MaterialTheme.typography.titleMedium)
                    Text(text = "${state.focusedMinutes} minutes focused")
                    Text(text = stringResource(Res.string.task_stats), style = MaterialTheme.typography.titleMedium)
                    Text(text = "${state.completedTasks} completed tasks")
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = BfyTheme.dimens.cardElevation)
            ) {
                Column(
                    modifier = Modifier.padding(BfyTheme.dimens.spacing16),
                    verticalArrangement = Arrangement.spacedBy(BfyTheme.dimens.spacing8)
                ) {
                    Text(text = stringResource(Res.string.badges), style = MaterialTheme.typography.titleMedium)
                    state.badges.forEach { badge ->
                        Text(
                            text = if (badge.isUnlocked) "Unlocked: ${badge.title}" else "Locked: ${badge.title}",
                            color = if (badge.isUnlocked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = BfyTheme.dimens.cardElevation)
            ) {
                Column(
                    modifier = Modifier.padding(BfyTheme.dimens.spacing16),
                    verticalArrangement = Arrangement.spacedBy(BfyTheme.dimens.spacing12)
                ) {
                    SettingToggle(
                        title = stringResource(Res.string.dark_theme),
                        checked = state.darkThemeEnabled,
                        onCheckedChange = onToggleTheme
                    )
                    SettingToggle(
                        title = stringResource(Res.string.language),
                        checked = state.englishLanguageEnabled,
                        onCheckedChange = onToggleLanguage
                    )
                    BfyButton(
                        text = stringResource(Res.string.change_password),
                        onClick = {},
                        style = BfyButtonStyle.SECONDARY
                    )
                    BfyButton(
                        text = stringResource(Res.string.logout),
                        onClick = onLogout,
                        style = BfyButtonStyle.DANGER
                    )
                }
            }
        }
    }
}

private fun initialsOf(name: String): String {
    val parts = name.trim().split(" ").filter { it.isNotBlank() }
    if (parts.isEmpty()) {
        return "BF"
    }
    if (parts.size == 1) {
        return parts.first().take(2).uppercase()
    }
    return (parts.first().first().toString() + parts.last().first().toString()).uppercase()
}

@Composable
private fun SettingToggle(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
