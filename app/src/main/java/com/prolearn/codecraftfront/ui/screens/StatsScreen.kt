package com.prolearn.codecraftfront.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import com.prolearn.codecraftfront.ui.theme.NeonCyan
import com.prolearn.codecraftfront.ui.theme.NeonGreen
import com.prolearn.codecraftfront.ui.theme.NeonOrange
import com.prolearn.codecraftfront.ui.theme.NeonPurple

private data class XpPoint(val day: Int, val xp: Float)

private data class LanguageAccuracy(
    val name: String,
    val accuracy: Float,
    val color: Color,
)

private data class StreakDay(val label: String, val value: Float)

private data class Badge(
    val title: String,
    val unlocked: Boolean,
    val color: Color,
    val icon: ImageVector,
    val description: String,
)

@Composable
fun StatsScreen(
    onOpenLeaderboard: () -> Unit = {},
) {
    val xpHistory = remember {
        listOf(
            XpPoint(1, 80f), XpPoint(2, 120f), XpPoint(3, 95f), XpPoint(4, 220f),
            XpPoint(5, 180f), XpPoint(6, 260f), XpPoint(7, 240f), XpPoint(8, 310f),
            XpPoint(9, 295f), XpPoint(10, 360f), XpPoint(11, 410f), XpPoint(12, 380f),
            XpPoint(13, 470f), XpPoint(14, 540f),
        )
    }
    val languageAccuracy = remember {
        listOf(
            LanguageAccuracy("Python", 0.92f, NeonGreen),
            LanguageAccuracy("JavaScript", 0.78f, NeonOrange),
            LanguageAccuracy("Kotlin", 0.65f, NeonPurple),
            LanguageAccuracy("Java", 0.41f, NeonCyan),
        )
    }
    val streakDays = remember {
        listOf(
            StreakDay("Mon", 0.4f), StreakDay("Tue", 0.7f), StreakDay("Wed", 0.55f),
            StreakDay("Thu", 0.85f), StreakDay("Fri", 0.6f), StreakDay("Sat", 1f),
            StreakDay("Sun", 0.9f),
        )
    }
    val badges = remember {
        listOf(
            Badge("First Steps", true, NeonGreen, Icons.Rounded.Star, "Complete your first mission"),
            Badge("Combo x5", true, NeonOrange, Icons.Rounded.Bolt, "Win 5 levels in a row"),
            Badge("Streak Master", true, NeonPurple, Icons.Rounded.LocalFireDepartment, "7-day streak"),
            Badge("Polyglot", false, NeonCyan, Icons.Rounded.TrendingUp, "Try 3 languages"),
            Badge("Bug Hunter", false, NeonGreen, Icons.Rounded.EmojiEvents, "Solve a level without errors"),
            Badge("Cyber Champ", false, NeonPurple, Icons.Rounded.Star, "Reach level 20"),
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "Your stats",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold,
        )
        Text(
            text = "Track XP, accuracy and streak to keep climbing the leaderboard.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        MetricsRow(
            totalXp = xpHistory.sumOf { it.xp.toInt() },
            avgAccuracy = languageAccuracy.map { it.accuracy }.average().toFloat(),
            streak = 7,
        )

        LeaderboardCta(onClick = onOpenLeaderboard)

        ChartCard(title = "XP over time", subtitle = "Last 14 days") {
            XpLineChart(
                points = xpHistory,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
            )
        }

        ChartCard(title = "Accuracy by language", subtitle = "Higher is better") {
            AccuracyBars(items = languageAccuracy)
        }

        ChartCard(title = "Streak history", subtitle = "Last 7 days") {
            StreakColumns(
                days = streakDays,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
            )
        }

        Text(
            text = "Badges",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold,
        )
        BadgesGrid(badges = badges)

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun MetricsRow(
    totalXp: Int,
    avgAccuracy: Float,
    streak: Int,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        MetricCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Rounded.Bolt,
            color = NeonGreen,
            value = totalXp.toString(),
            label = "Total XP",
        )
        MetricCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Rounded.TrendingUp,
            color = NeonPurple,
            value = "${(avgAccuracy * 100).toInt()}%",
            label = "Accuracy",
        )
        MetricCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Rounded.LocalFireDepartment,
            color = NeonOrange,
            value = streak.toString(),
            label = "Day streak",
        )
    }
}

@Composable
private fun MetricCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    color: Color,
    value: String,
    label: String,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier
                .background(
                    Brush.verticalGradient(
                        listOf(
                            color.copy(alpha = 0.18f),
                            Color.Transparent,
                        ),
                    ),
                )
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.22f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                )
            }
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ChartCard(
    title: String,
    subtitle: String,
    content: @Composable () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            content()
        }
    }
}

@Composable
private fun XpLineChart(
    points: List<XpPoint>,
    modifier: Modifier = Modifier,
) {
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    Canvas(modifier = modifier) {
        if (points.isEmpty()) return@Canvas
        val maxXp = points.maxOf { it.xp }
        val minXp = 0f
        val width = size.width
        val height = size.height
        val left = 0f
        val bottom = height - 16f
        val top = 8f
        val usableWidth = width
        val usableHeight = bottom - top

        val gridColor = onSurfaceVariant.copy(alpha = 0.15f)
        val gridLines = 4
        for (i in 0..gridLines) {
            val y = top + usableHeight * (i.toFloat() / gridLines)
            drawLine(
                color = gridColor,
                start = Offset(left, y),
                end = Offset(left + usableWidth, y),
                strokeWidth = 1f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f)),
            )
        }

        val stepX = if (points.size > 1) usableWidth / (points.size - 1) else usableWidth
        val pathPoints = points.mapIndexed { idx, p ->
            val x = left + idx * stepX
            val ratio = ((p.xp - minXp) / (maxXp - minXp)).coerceIn(0f, 1f)
            val y = bottom - ratio * usableHeight
            Offset(x, y)
        }

        val fillPath = Path().apply {
            moveTo(pathPoints.first().x, bottom)
            pathPoints.forEach { lineTo(it.x, it.y) }
            lineTo(pathPoints.last().x, bottom)
            close()
        }
        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(NeonGreen.copy(alpha = 0.45f), Color.Transparent),
                startY = top,
                endY = bottom,
            ),
        )

        val linePath = Path().apply {
            moveTo(pathPoints.first().x, pathPoints.first().y)
            pathPoints.drop(1).forEach { lineTo(it.x, it.y) }
        }
        drawPath(
            path = linePath,
            brush = Brush.horizontalGradient(listOf(NeonGreen, NeonCyan)),
            style = Stroke(width = 5f),
        )

        pathPoints.forEach { p ->
            drawCircle(color = NeonCyan.copy(alpha = 0.35f), radius = 9f, center = p)
            drawCircle(color = NeonCyan, radius = 4.5f, center = p)
        }
    }
}

@Composable
private fun AccuracyBars(items: List<LanguageAccuracy>) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items.forEach { item ->
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = item.name,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = "${(item.accuracy * 100).toInt()}%",
                        color = item.color,
                        fontWeight = FontWeight.Bold,
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(item.accuracy)
                            .height(10.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(item.color.copy(alpha = 0.6f), item.color),
                                ),
                            ),
                    )
                }
            }
        }
    }
}

@Composable
private fun StreakColumns(
    days: List<StreakDay>,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        days.forEach { day ->
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    contentAlignment = Alignment.BottomCenter,
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp * day.value)
                            .clip(RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp))
                            .background(
                                Brush.verticalGradient(
                                    listOf(NeonOrange, NeonPurple),
                                ),
                            ),
                    )
                }
                Text(
                    text = day.label,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun BadgesGrid(badges: List<Badge>) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(((badges.size + 2) / 3 * 140).dp),
        userScrollEnabled = false,
    ) {
        items(badges) { badge ->
            BadgeChip(badge)
        }
    }
}

@Composable
private fun LeaderboardCta(onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Row(
            modifier = Modifier
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            NeonOrange.copy(alpha = 0.22f),
                            NeonPurple.copy(alpha = 0.18f),
                        ),
                    ),
                )
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(androidx.compose.foundation.shape.CircleShape)
                    .background(NeonOrange.copy(alpha = 0.25f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Rounded.EmojiEvents,
                    contentDescription = null,
                    tint = NeonOrange,
                )
            }
            Spacer(modifier = Modifier.size(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Global leaderboard",
                    fontWeight = FontWeight.ExtraBold,
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = "See how you rank against other coders",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            Icon(
                imageVector = Icons.Rounded.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun BadgeChip(badge: Badge) {
    val transition = rememberInfiniteTransition(label = "badge")
    val pulse by transition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulse",
    )
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.85f),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (badge.unlocked) MaterialTheme.colorScheme.surface
            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    if (badge.unlocked) {
                        Brush.verticalGradient(
                            listOf(badge.color.copy(alpha = 0.22f), Color.Transparent),
                        )
                    } else {
                        Brush.verticalGradient(listOf(Color.Transparent, Color.Transparent))
                    },
                )
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .graphicsLayer {
                        if (badge.unlocked) {
                            scaleX = pulse
                            scaleY = pulse
                        }
                    }
                    .clip(CircleShape)
                    .background(
                        if (badge.unlocked) badge.color.copy(alpha = 0.25f)
                        else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = if (badge.unlocked) badge.icon else Icons.Rounded.Lock,
                    contentDescription = null,
                    tint = if (badge.unlocked) badge.color else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                text = badge.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )
            Text(
                text = badge.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )
        }
    }
}
