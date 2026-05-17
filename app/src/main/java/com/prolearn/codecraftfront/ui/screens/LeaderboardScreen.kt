package com.prolearn.codecraftfront.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.prolearn.codecraftfront.data.LeaderboardEntry
import com.prolearn.codecraftfront.ui.leaderboard.LeaderboardViewModel
import com.prolearn.codecraftfront.ui.theme.NeonCyan
import com.prolearn.codecraftfront.ui.theme.NeonGreen
import com.prolearn.codecraftfront.ui.theme.NeonOrange
import com.prolearn.codecraftfront.ui.theme.NeonPurple

private val Gold = Color(0xFFFFC44D)
private val Silver = Color(0xFFB6C2D6)
private val Bronze = Color(0xFFE08A4D)

@Composable
fun LeaderboardScreen() {
    val viewModel: LeaderboardViewModel = viewModel()
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Leaderboard",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                )
                Text(
                    text = if (state.isMock) {
                        "Showcasing demo data — be the first to climb up."
                    } else {
                        "Top coders racing for the cyber crown."
                    },
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            IconButton(onClick = viewModel::refresh) {
                Icon(
                    imageVector = Icons.Rounded.Refresh,
                    contentDescription = "Refresh",
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        }

        when {
            state.isLoading -> LoadingState()
            state.entries.isEmpty() -> EmptyState()
            else -> LeaderboardContent(
                entries = state.entries,
                currentUserId = state.currentUserId,
            )
        }
    }
}

@Composable
private fun LoadingState() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 64.dp),
        contentAlignment = Alignment.TopCenter,
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun EmptyState() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 64.dp),
        contentAlignment = Alignment.TopCenter,
    ) {
        Text(
            text = "No players yet. Be the first!",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun LeaderboardContent(
    entries: List<LeaderboardEntry>,
    currentUserId: String?,
) {
    val podium = entries.take(3)
    val rest = entries.drop(3)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        if (podium.isNotEmpty()) {
            item { Podium(podium = podium, currentUserId = currentUserId) }
        }
        if (rest.isNotEmpty()) {
            item {
                Text(
                    text = "Top players",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(top = 6.dp),
                )
            }
            itemsIndexed(rest) { index, entry ->
                LeaderboardRow(
                    rank = index + 4,
                    entry = entry,
                    isCurrentUser = entry.userId == currentUserId,
                )
            }
        }
        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

@Composable
private fun Podium(
    podium: List<LeaderboardEntry>,
    currentUserId: String?,
) {
    val first = podium.getOrNull(0)
    val second = podium.getOrNull(1)
    val third = podium.getOrNull(2)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Row(
            modifier = Modifier
                .background(
                    Brush.verticalGradient(
                        listOf(
                            NeonPurple.copy(alpha = 0.18f),
                            Color.Transparent,
                        ),
                    ),
                )
                .padding(vertical = 18.dp, horizontal = 12.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            if (second != null) {
                PodiumColumn(
                    rank = 2,
                    entry = second,
                    color = Silver,
                    podiumHeight = 64.dp,
                    isCurrentUser = second.userId == currentUserId,
                )
            }
            if (first != null) {
                PodiumColumn(
                    rank = 1,
                    entry = first,
                    color = Gold,
                    podiumHeight = 92.dp,
                    isCurrentUser = first.userId == currentUserId,
                    crowned = true,
                )
            }
            if (third != null) {
                PodiumColumn(
                    rank = 3,
                    entry = third,
                    color = Bronze,
                    podiumHeight = 48.dp,
                    isCurrentUser = third.userId == currentUserId,
                )
            }
        }
    }
}

@Composable
private fun PodiumColumn(
    rank: Int,
    entry: LeaderboardEntry,
    color: Color,
    podiumHeight: androidx.compose.ui.unit.Dp,
    isCurrentUser: Boolean,
    crowned: Boolean = false,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.width(96.dp),
    ) {
        if (crowned) {
            Icon(
                imageVector = Icons.Rounded.EmojiEvents,
                contentDescription = null,
                tint = Gold,
                modifier = Modifier.size(22.dp),
            )
        } else {
            Spacer(modifier = Modifier.height(22.dp))
        }
        Box(
            modifier = Modifier
                .size(if (crowned) 70.dp else 56.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        listOf(color, color.copy(alpha = 0.55f)),
                    ),
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = entry.displayName.first().uppercaseChar().toString(),
                color = Color(0xFF1A1300),
                fontWeight = FontWeight.ExtraBold,
                fontSize = if (crowned) 26.sp else 20.sp,
            )
        }
        Text(
            text = entry.displayName,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            maxLines = 1,
        )
        Text(
            text = "${entry.xp} XP",
            color = color,
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.labelSmall,
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(podiumHeight)
                .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(color, color.copy(alpha = 0.4f)),
                    ),
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = rank.toString(),
                color = Color(0xFF1A1300),
                fontWeight = FontWeight.ExtraBold,
                fontSize = if (crowned) 28.sp else 20.sp,
            )
        }
        if (isCurrentUser) {
            Text(
                text = "You",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun LeaderboardRow(
    rank: Int,
    entry: LeaderboardEntry,
    isCurrentUser: Boolean,
) {
    val accent = listOf(NeonGreen, NeonOrange, NeonPurple, NeonCyan)[rank % 4]
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrentUser) MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
            else MaterialTheme.colorScheme.surface,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "#$rank",
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.width(40.dp),
            )
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(listOf(accent, accent.copy(alpha = 0.5f))),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = entry.displayName.first().uppercaseChar().toString(),
                    color = Color(0xFF062018),
                    fontWeight = FontWeight.ExtraBold,
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entry.displayName + if (isCurrentUser) "  •  You" else "",
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "Level ${entry.level}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                text = "${entry.xp} XP",
                fontWeight = FontWeight.ExtraBold,
                color = accent,
            )
        }
    }
}
