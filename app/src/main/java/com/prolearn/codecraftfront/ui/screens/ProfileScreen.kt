package com.prolearn.codecraftfront.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Logout
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.PersonAdd
import androidx.compose.material.icons.rounded.PrivacyTip
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.prolearn.codecraftfront.R
import androidx.compose.ui.res.stringResource
import com.prolearn.codecraftfront.ui.preferences.UserPreferencesViewModel
import com.prolearn.codecraftfront.ui.profile.ProfileViewModel
import com.prolearn.codecraftfront.ui.theme.NeonCyan
import com.prolearn.codecraftfront.ui.theme.NeonGreen
import com.prolearn.codecraftfront.ui.theme.NeonOrange
import com.prolearn.codecraftfront.ui.theme.NeonPurple

private data class FriendPreview(
    val name: String,
    val xp: Int,
    val color: Color,
)

@Composable
fun ProfileScreen(
    email: String?,
    onSignOut: () -> Unit,
) {
    val viewModel: ProfileViewModel = hiltViewModel()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val emailDerivedName = remember(email) {
        email?.substringBefore("@")?.replaceFirstChar { it.uppercase() } ?: "Cyber Cadet"
    }
    val displayName = state.displayName ?: emailDerivedName
    val initial = remember(displayName) {
        displayName.firstOrNull()?.uppercaseChar()?.toString() ?: "?"
    }
    val totalXp = state.xp
    val level = state.level
    val xpForNext = 400
    val xpInLevel = totalXp % xpForNext
    val streak = state.streak
    val wins = state.wins

    val friends = remember {
        listOf(
            FriendPreview("Mira", 2150, NeonGreen),
            FriendPreview("Hiro", 1820, NeonOrange),
            FriendPreview("Kai", 1495, NeonPurple),
            FriendPreview("Zoe", 1190, NeonCyan),
        )
    }

    val prefsViewModel: UserPreferencesViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    val prefs by prefsViewModel.preferences.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.surface,
                    ),
                ),
            )
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = stringResource(R.string.tab_profile),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold,
        )

        AvatarHeader(
            initial = initial,
            displayName = displayName,
            email = email,
            level = level,
            onEditAvatar = { /* open avatar editor later */ },
        )

        LevelXpCard(
            level = level,
            totalXp = totalXp,
            xpInLevel = xpInLevel,
            xpForNext = xpForNext,
        )

        MiniStatsRow(streak = streak, wins = wins, totalXp = totalXp)

        FriendsSection(friends = friends)

        SettingsSection(
            notificationsEnabled = prefs.notificationsEnabled,
            onToggleNotifications = prefsViewModel::setNotificationsEnabled,
            darkThemeEnabled = prefs.darkTheme,
            onToggleDarkTheme = prefsViewModel::setDarkTheme,
            soundEnabled = prefs.soundEnabled,
            onToggleSound = prefsViewModel::setSoundEnabled,
        )

        OutlinedButton(
            onClick = onSignOut,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.6f)),
        ) {
            Icon(
                imageVector = Icons.Rounded.Logout,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.profile_sign_out),
                color = MaterialTheme.colorScheme.error,
                fontWeight = FontWeight.Bold,
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun AvatarHeader(
    initial: String,
    displayName: String,
    email: String?,
    level: Int,
    onEditAvatar: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Row(
            modifier = Modifier
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            NeonPurple.copy(alpha = 0.18f),
                            NeonCyan.copy(alpha = 0.12f),
                        ),
                    ),
                )
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .size(86.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(listOf(NeonPurple, NeonCyan)),
                        ),
                )
                Box(
                    modifier = Modifier
                        .size(78.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = initial,
                        fontSize = 34.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .align(Alignment.BottomEnd)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                        .clickable(onClick = onEditAvatar),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Edit,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = displayName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                )
                Text(
                    text = email ?: stringResource(R.string.profile_anonymous),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(
                            Brush.horizontalGradient(listOf(NeonOrange, NeonPurple)),
                        )
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                ) {
                    Text(
                        text = "Level $level",
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        style = MaterialTheme.typography.labelMedium,
                    )
                }
            }
        }
    }
}

@Composable
private fun LevelXpCard(
    level: Int,
    totalXp: Int,
    xpInLevel: Int,
    xpForNext: Int,
) {
    val progress = (xpInLevel.toFloat() / xpForNext).coerceIn(0f, 1f)
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(
            modifier = Modifier
                .background(
                    Brush.verticalGradient(
                        listOf(NeonGreen.copy(alpha = 0.16f), Color.Transparent),
                    ),
                )
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        text = "Total XP",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = totalXp.toString(),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                    )
                }
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(listOf(NeonGreen, NeonCyan)),
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = level.toString(),
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF062018),
                        fontSize = 22.sp,
                    )
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .height(10.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            Brush.horizontalGradient(listOf(NeonGreen, NeonCyan)),
                        ),
                )
            }
            Text(
                text = "$xpInLevel / $xpForNext XP to level ${level + 1}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun MiniStatsRow(
    streak: Int,
    wins: Int,
    totalXp: Int,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        MiniStat(
            modifier = Modifier.weight(1f),
            icon = Icons.Rounded.LocalFireDepartment,
            color = NeonOrange,
            value = streak.toString(),
            label = "Streak",
        )
        MiniStat(
            modifier = Modifier.weight(1f),
            icon = Icons.Rounded.EmojiEvents,
            color = NeonPurple,
            value = wins.toString(),
            label = "Wins",
        )
        MiniStat(
            modifier = Modifier.weight(1f),
            icon = Icons.Rounded.Bolt,
            color = NeonGreen,
            value = totalXp.toString(),
            label = "XP",
        )
    }
}

@Composable
private fun MiniStat(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    color: Color,
    value: String,
    label: String,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(
            modifier = Modifier
                .background(
                    Brush.verticalGradient(
                        listOf(color.copy(alpha = 0.18f), Color.Transparent),
                    ),
                )
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
            )
            Text(
                text = value,
                fontWeight = FontWeight.ExtraBold,
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun FriendsSection(friends: List<FriendPreview>) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Friends",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
            )
            Row(
                modifier = Modifier.clickable { /* open invite later */ },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Rounded.PersonAdd,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Invite",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items(friends) { friend ->
                FriendChip(friend)
            }
        }
    }
}

@Composable
private fun FriendChip(friend: FriendPreview) {
    Card(
        modifier = Modifier.width(110.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(friend.color, friend.color.copy(alpha = 0.55f)),
                        ),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = friend.name.first().uppercaseChar().toString(),
                    color = Color(0xFF062018),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp,
                )
            }
            Text(
                text = friend.name,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                text = "${friend.xp} XP",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelSmall,
            )
        }
    }
}

@Composable
private fun SettingsSection(
    notificationsEnabled: Boolean,
    onToggleNotifications: (Boolean) -> Unit,
    darkThemeEnabled: Boolean,
    onToggleDarkTheme: (Boolean) -> Unit,
    soundEnabled: Boolean,
    onToggleSound: (Boolean) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        ) {
            Column {
                SettingsToggle(
                    icon = Icons.Rounded.Palette,
                    title = "Dark theme",
                    subtitle = "Neon-purple cosmic mode",
                    checked = darkThemeEnabled,
                    onCheckedChange = onToggleDarkTheme,
                )
                SettingsToggle(
                    icon = Icons.Rounded.VolumeUp,
                    title = "Sound effects",
                    subtitle = "Play tones on success and failure",
                    checked = soundEnabled,
                    onCheckedChange = onToggleSound,
                )
                SettingsToggle(
                    icon = Icons.Rounded.Notifications,
                    title = "Notifications",
                    subtitle = "Daily reminders and rewards",
                    checked = notificationsEnabled,
                    onCheckedChange = onToggleNotifications,
                )
                SettingsRow(
                    icon = Icons.Rounded.Language,
                    title = "App language",
                    subtitle = "English",
                )
                SettingsRow(
                    icon = Icons.Rounded.PrivacyTip,
                    title = "Privacy",
                    subtitle = "Manage data and visibility",
                )
            }
        }
    }
}

@Composable
private fun SettingsToggle(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SettingIcon(icon)
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontWeight = FontWeight.SemiBold)
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = NeonGreen,
                checkedTrackColor = NeonGreen.copy(alpha = 0.45f),
            ),
        )
    }
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit = {},
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SettingIcon(icon)
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontWeight = FontWeight.SemiBold)
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Icon(
            imageVector = Icons.Rounded.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun SettingIcon(icon: ImageVector) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
        )
    }
}