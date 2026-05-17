package com.prolearn.codecraftfront.ui.screens

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.HelpOutline
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.prolearn.codecraftfront.R
import com.prolearn.codecraftfront.ui.ai.AiAssistantBottomSheet
import com.prolearn.codecraftfront.ui.ai.AiAssistantViewModel
import com.prolearn.codecraftfront.ui.ai.HintRequestContext
import com.prolearn.codecraftfront.ui.effects.CelebrationOverlay
import com.prolearn.codecraftfront.ui.theme.NeonCyan
import com.prolearn.codecraftfront.ui.theme.NeonGreen
import com.prolearn.codecraftfront.ui.theme.NeonOrange
import com.prolearn.codecraftfront.ui.theme.NeonPurple
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import dagger.hilt.android.components.ActivityComponent

@EntryPoint
@InstallIn(ActivityComponent::class)
interface RepositoryEntryPoint {
    fun leaderboardRepository(): com.prolearn.codecraftfront.data.LeaderboardRepository
}

@Composable
fun HomeScreen(
    onStartDailyChallenge: () -> Unit = {},
) {
    val scrollState = rememberScrollState()
    var rewardClaimed by remember { mutableStateOf(false) }
    val pulse = rememberInfiniteTransition(label = "pulse")
    val fireScale by pulse.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(900),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "fireScale",
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.surface,
                    ),
                ),
            )
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text(
            text = stringResource(R.string.home_greeting),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold,
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
            ),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = stringResource(R.string.home_streak_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = stringResource(R.string.home_streak_value),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Icon(
                    imageVector = Icons.Rounded.LocalFireDepartment,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.scale(fireScale),
                )
            }
        }

        DailyChallengeCard(onStart = onStartDailyChallenge)

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f),
            ),
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Bolt, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.home_xp_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                }
                Text(
                    text = stringResource(R.string.home_xp_value),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                )
                Text(
                    text = stringResource(R.string.home_xp_subtitle),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.75f),
            ),
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    text = stringResource(R.string.home_reward_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
                Text(text = stringResource(R.string.home_reward_subtitle))
                AnimatedVisibility(visible = !rewardClaimed) {
                    Button(
                        onClick = { rewardClaimed = true },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    ) {
                        Text(stringResource(R.string.home_reward_claim))
                    }
                }
                AnimatedVisibility(visible = rewardClaimed) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.home_reward_done),
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }
        }

        JuicyQuickStartButton()
        Spacer(modifier = Modifier.height(12.dp))
    }
}

@Composable
private fun DailyChallengeCard(onStart: () -> Unit) {
    var remainingMs by remember { mutableStateOf(timeUntilMidnight()) }
    LaunchedEffect(Unit) {
        while (true) {
            remainingMs = timeUntilMidnight()
            delay(1000)
        }
    }
    val pulse = rememberInfiniteTransition(label = "challengePulse")
    val glow by pulse.animateFloat(
        initialValue = 0.55f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "challengeGlow",
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(
            modifier = Modifier
                .background(
                    brush = Brush.horizontalGradient(
                        listOf(
                            NeonOrange.copy(alpha = 0.30f * glow),
                            NeonPurple.copy(alpha = 0.28f * glow),
                        ),
                    ),
                )
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Rounded.Bolt,
                    contentDescription = null,
                    tint = NeonOrange,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Daily Challenge",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                )
            }
            Text(
                text = "Solve today's special mission for +200 XP and a rare badge.",
                color = MaterialTheme.colorScheme.onSurface,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        text = "Resets in",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = formatCountdown(remainingMs),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = NeonOrange,
                    )
                }
                Button(
                    onClick = onStart,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NeonOrange,
                        contentColor = Color(0xFF1A1300),
                    ),
                ) {
                    Text(
                        text = "Take challenge",
                        fontWeight = FontWeight.ExtraBold,
                    )
                }
            }
        }
    }
}

private fun timeUntilMidnight(): Long {
    val now = java.util.Calendar.getInstance()
    val midnight = (now.clone() as java.util.Calendar).apply {
        add(java.util.Calendar.DAY_OF_YEAR, 1)
        set(java.util.Calendar.HOUR_OF_DAY, 0)
        set(java.util.Calendar.MINUTE, 0)
        set(java.util.Calendar.SECOND, 0)
        set(java.util.Calendar.MILLISECOND, 0)
    }
    return (midnight.timeInMillis - now.timeInMillis).coerceAtLeast(0L)
}

private fun formatCountdown(ms: Long): String {
    val totalSec = ms / 1000
    val h = totalSec / 3600
    val m = (totalSec % 3600) / 60
    val s = totalSec % 60
    return "%02d:%02d:%02d".format(h, m, s)
}

@Composable
private fun JuicyQuickStartButton() {
    var tapped by remember { mutableStateOf(false) }
    val scale by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (tapped) 0.96f else 1f,
        animationSpec = spring(dampingRatio = 0.45f, stiffness = 420f),
        label = "ctaScale",
    )

    OutlinedButton(
        onClick = { tapped = !tapped },
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
    ) {
        Text(
            text = stringResource(R.string.home_quick_start),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
fun LanguagesScreen(
    onOpenLevels: (String) -> Unit,
) {
    val cards = remember {
        listOf(
            LanguageUiModel("Python", "Easy entry and many quests", false, Color(0xFF3478F6), Color(0xFF6AE3FF)),
            LanguageUiModel("JavaScript", "Web logic and interactivity", false, Color(0xFFF7C948), Color(0xFFFF8A3D)),
            LanguageUiModel("Kotlin", "Native Android and backend", false, Color(0xFF9B5CFF), Color(0xFF42E8FF)),
            LanguageUiModel("Java", "Unlocks at level 5", true, Color(0xFF4ED39A), Color(0xFF7A8CFF)),
        )
    }
    val listState = rememberLazyListState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = stringResource(R.string.languages_title),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(horizontal = 16.dp),
        )
        Text(
            text = stringResource(R.string.languages_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp),
        )

        LazyRow(
            state = listState,
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            itemsIndexed(cards) { index, card ->
                val centerOffset = (listState.firstVisibleItemIndex - index) +
                        (listState.firstVisibleItemScrollOffset / 300f)
                val rotation = (centerOffset * 8f).coerceIn(-18f, 18f)
                LanguageCard(
                    model = card,
                    onClick = { onOpenLevels(card.title) },
                    modifier = Modifier.graphicsLayer {
                        rotationY = rotation
                        cameraDistance = 16f * density
                    },
                )
            }
        }
    }
}

@Composable
private fun LanguageCard(
    model: LanguageUiModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .width(280.dp)
            .height(200.dp)
            .clickable(enabled = !model.locked) { onClick() },
        shape = RoundedCornerShape(26.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.linearGradient(listOf(model.startColor, model.endColor)))
                .padding(18.dp),
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = if (model.locked) Icons.Rounded.Lock else Icons.Rounded.Code,
                        contentDescription = null,
                        tint = Color.White,
                    )
                    Text(
                        text = if (model.locked) stringResource(R.string.languages_locked) else stringResource(R.string.languages_available),
                        color = Color.White.copy(alpha = 0.92f),
                        style = MaterialTheme.typography.labelLarge,
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = model.title,
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                    )
                    Text(
                        text = model.description,
                        color = Color.White.copy(alpha = 0.92f),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
    }
}

@Composable
fun LevelsScreen(
    language: String,
    onOpenPlay: (language: String, track: String, levelId: Int) -> Unit,
) {
    val tracks = listOf(
        stringResource(R.string.levels_beginner),
        stringResource(R.string.levels_advanced),
    )
    var selectedTrack by remember { mutableIntStateOf(0) }
    val levels = remember(language, selectedTrack) {
        val isBeginner = selectedTrack == 0
        (1..12).map { index ->
            val unlocked = if (isBeginner) index <= 4 else index <= 2
            val progress = if (unlocked) (0.25f * (index % 4)).coerceAtLeast(0.15f) else 0f
            LevelUiModel(
                id = index,
                title = if (isBeginner) "B$index" else "A$index",
                stars = if (unlocked) index % 4 else 0,
                progress = progress,
                unlocked = unlocked,
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = stringResource(R.string.levels_title, language),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold,
        )
        Text(
            text = stringResource(R.string.levels_subtitle),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        TabRow(selectedTabIndex = selectedTrack) {
            tracks.forEachIndexed { idx, title ->
                Tab(
                    selected = selectedTrack == idx,
                    onClick = { selectedTrack = idx },
                    text = { Text(title) },
                )
            }
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            items(levels) { level ->
                LevelCard(
                    level = level,
                    onClick = {
                        if (level.unlocked) {
                            val track = if (selectedTrack == 0) "beginner" else "advanced"
                            onOpenPlay(language, track, level.id)
                        }
                    },
                )
            }
        }
    }
}

@Composable
private fun LevelCard(
    level: LevelUiModel,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .height(110.dp)
            .fillMaxWidth()
            .clickable(enabled = level.unlocked, onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (level.unlocked) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.75f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
            },
        ),
        border = BorderStroke(
            1.dp,
            if (level.unlocked) MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
            else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = level.title,
                    fontWeight = FontWeight.Bold,
                )
                Icon(
                    imageVector = if (level.unlocked) Icons.Rounded.Code else Icons.Rounded.Lock,
                    contentDescription = null,
                    tint = if (level.unlocked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            CircularProgressIndicator(
                progress = { level.progress },
                modifier = Modifier.fillMaxWidth(),
                strokeWidth = 5.dp,
                color = if (level.unlocked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                trackColor = MaterialTheme.colorScheme.surface,
            )

            Text(
                text = stringResource(R.string.levels_stars_format, level.stars),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private data class LanguageUiModel(
    val title: String,
    val description: String,
    val locked: Boolean,
    val startColor: Color,
    val endColor: Color,
)

private data class LevelUiModel(
    val id: Int,
    val title: String,
    val stars: Int,
    val progress: Float,
    val unlocked: Boolean,
)

@Composable
fun PlayHubScreen(
    onOpenLanguages: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    ),
                ),
            )
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(R.string.play_hub_title),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold,
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = stringResource(R.string.play_hub_subtitle),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onOpenLanguages,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.play_hub_cta))
        }
    }
}

@Composable
fun PlayScreen(
    language: String,
    track: String,
    levelId: Int,
) {
    val isDaily = track.equals("daily", ignoreCase = true)
    val trackLabel = when {
        isDaily -> stringResource(R.string.levels_daily)
        track.equals("advanced", ignoreCase = true) -> stringResource(R.string.levels_advanced)
        else -> stringResource(R.string.levels_beginner)
    }
    val xpReward = if (isDaily) 200 else 50
    val missionHeader = stringResource(R.string.play_mission_header, language, trackLabel, levelId)

    val playTitle = stringResource(R.string.play_title)
    val playStory = stringResource(R.string.play_story)
    val editorLabel = stringResource(R.string.play_editor_label)
    val invalidCodeText = stringResource(R.string.play_invalid_code)
    val failedText = stringResource(R.string.play_failed)
    val successText = stringResource(R.string.play_success)
    val coinMissingText = stringResource(R.string.play_coin_missing)
    val runningText = stringResource(R.string.play_running)
    val runText = stringResource(R.string.play_run)
    val resetDoneText = stringResource(R.string.play_reset_done)
    val resetText = stringResource(R.string.play_reset)

    val mission = remember(language, track, levelId) {
        buildMissionForLevel(language, track, levelId)
    }

    var editorText by remember(language, track, levelId) {
        mutableStateOf(TextFieldValue(starterCodeForMission(mission)))
    }
    var robotState by remember(language, track, levelId) {
        mutableStateOf(mission.toRobotState())
    }
    var lives by remember(language, track, levelId) { mutableIntStateOf(3) }
    var isRunning by remember(language, track, levelId) { mutableStateOf(false) }
    var statusText by remember(language, track, levelId) { mutableStateOf<String?>(null) }
    var lastError by remember(language, track, levelId) { mutableStateOf<String?>(null) }
    var failedAttempts by remember(language, track, levelId) { mutableIntStateOf(0) }
    var showCelebration by remember(language, track, levelId) { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val aiViewModel: AiAssistantViewModel = viewModel()
    val aiState by aiViewModel.uiState.collectAsStateWithLifecycle()

    // Получаем LeaderboardRepository через Hilt EntryPoint
    val context = LocalContext.current
    val repo = remember {
        EntryPointAccessors.fromActivity(
            context as Activity,
            RepositoryEntryPoint::class.java
        ).leaderboardRepository()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = missionHeader,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = playTitle,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
            )
            Text(
                text = playStory,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                repeat(3) { idx ->
                    Icon(
                        imageVector = Icons.Rounded.Favorite,
                        contentDescription = null,
                        tint = if (idx < lives) Color(0xFFFF5D7D) else MaterialTheme.colorScheme.outlineVariant,
                    )
                }
            }

            RobotArena(
                robot = robotState,
                mission = mission,
                modifier = Modifier.fillMaxWidth(),
            )

            com.prolearn.codecraftfront.ui.components.CodeEditorField(
                value = editorText,
                onValueChange = { editorText = it },
                modifier = Modifier.fillMaxWidth(),
                label = editorLabel,
                minLines = 8,
            )

            Button(
                onClick = {
                    if (isRunning || lives <= 0) return@Button
                    isRunning = true
                    statusText = null
                    robotState = mission.toRobotState()

                    scope.launch {
                        val commands = parseCommands(editorText.text)
                        if (commands.isFailure) {
                            val msg = commands.exceptionOrNull()?.message ?: invalidCodeText
                            statusText = msg
                            lastError = msg
                            failedAttempts += 1
                            lives = (lives - 1).coerceAtLeast(0)
                            isRunning = false
                            com.prolearn.codecraftfront.ui.sound.SoundFx
                                .play(com.prolearn.codecraftfront.ui.sound.SoundFx.Effect.Failure)
                            return@launch
                        }
                        for (command in commands.getOrThrow()) {
                            delay(260)
                            val result = applyCommand(robotState, command, mission)
                            if (result.isFailure) {
                                val msg = result.exceptionOrNull()?.message ?: failedText
                                statusText = msg
                                lastError = msg
                                failedAttempts += 1
                                lives = (lives - 1).coerceAtLeast(0)
                                isRunning = false
                                com.prolearn.codecraftfront.ui.sound.SoundFx
                                    .play(com.prolearn.codecraftfront.ui.sound.SoundFx.Effect.Failure)
                                return@launch
                            }
                            robotState = result.getOrThrow()
                        }
                        isRunning = false
                        if (robotState.collectedCoin) {
                            statusText = successText
                            lastError = null
                            showCelebration = true
                            com.prolearn.codecraftfront.ui.sound.SoundFx
                                .play(com.prolearn.codecraftfront.ui.sound.SoundFx.Effect.Success)
                            val player = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
                            val displayName = player?.email?.substringBefore("@")
                                ?.replaceFirstChar { it.uppercase() }
                                ?: "Cyber Cadet"
                            scope.launch {
                                repo.incrementCurrentUserXp(displayName, xpReward)
                            }
                        } else {
                            statusText = coinMissingText
                            lastError = coinMissingText
                            failedAttempts += 1
                            lives = (lives - 1).coerceAtLeast(0)
                            com.prolearn.codecraftfront.ui.sound.SoundFx
                                .play(com.prolearn.codecraftfront.ui.sound.SoundFx.Effect.Failure)
                        }
                    }
                },
                enabled = !isRunning && lives > 0,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (isRunning) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .height(16.dp)
                            .padding(end = 8.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                }
                Text(if (isRunning) runningText else runText)
            }

            OutlinedButton(
                onClick = {
                    aiViewModel.requestHint(
                        HintRequestContext(
                            language = language,
                            track = trackLabel,
                            levelId = levelId,
                            storyPrompt = playStory,
                            playerCode = editorText.text,
                            lastError = lastError,
                            failedAttempts = failedAttempts,
                        ),
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
            ) {
                Icon(
                    imageVector = Icons.Rounded.HelpOutline,
                    contentDescription = null,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.play_help_button))
            }

            if (lives <= 0) {
                OutlinedButton(
                    onClick = {
                        lives = 3
                        robotState = mission.toRobotState()
                        statusText = resetDoneText
                        lastError = null
                        failedAttempts = 0
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(resetText)
                }
            }

            AnimatedVisibility(visible = statusText != null) {
                Text(
                    text = statusText.orEmpty(),
                    color = if (robotState.collectedCoin) Color(0xFF39FF88) else MaterialTheme.colorScheme.tertiary,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }

        CelebrationOverlay(
            visible = showCelebration,
            xpReward = xpReward,
            onContinue = { showCelebration = false },
        )
    }

    AiAssistantBottomSheet(
        state = aiState,
        onDismiss = aiViewModel::close,
        onClearHistory = aiViewModel::resetHistory,
    )
}

@Composable
private fun RobotArena(
    robot: RobotState,
    mission: PlayMission,
    modifier: Modifier = Modifier,
) {
    val animX by animateFloatAsState(
        targetValue = robot.x.toFloat(),
        animationSpec = spring(dampingRatio = 0.7f, stiffness = Spring.StiffnessMediumLow),
        label = "robotX",
    )
    val animY by animateFloatAsState(
        targetValue = robot.y.toFloat(),
        animationSpec = spring(dampingRatio = 0.7f, stiffness = Spring.StiffnessMediumLow),
        label = "robotY",
    )
    val targetAngle = when (robot.direction) {
        RobotDirection.UP -> -90f
        RobotDirection.RIGHT -> 0f
        RobotDirection.DOWN -> 90f
        RobotDirection.LEFT -> 180f
    }
    val animAngle by animateFloatAsState(
        targetValue = targetAngle,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 380f),
        label = "robotAngle",
    )
    val collectScale by animateFloatAsState(
        targetValue = if (robot.collectedCoin) 1.25f else 1f,
        animationSpec = spring(dampingRatio = 0.4f, stiffness = 600f),
        label = "collectScale",
    )

    val infinite = rememberInfiniteTransition(label = "arenaInfinite")
    val coinPulse by infinite.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.18f,
        animationSpec = infiniteRepeatable(
            animation = tween(900),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "coinPulse",
    )
    val gridGlow by infinite.animateFloat(
        initialValue = 0.10f,
        targetValue = 0.20f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "gridGlow",
    )
    val bob by infinite.animateFloat(
        initialValue = -1.5f,
        targetValue = 1.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(1300),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "robotBob",
    )

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .padding(12.dp),
        ) {
            val cell = maxWidth / 5
            Canvas(modifier = Modifier.fillMaxSize()) {
                val cellPx = cell.toPx()

                if (!robot.collectedCoin) {
                    drawRoundRect(
                        color = NeonOrange.copy(alpha = 0.16f),
                        topLeft = Offset(mission.targetX * cellPx + 4f, mission.targetY * cellPx + 4f),
                        size = Size(cellPx - 8f, cellPx - 8f),
                        cornerRadius = CornerRadius(12f, 12f),
                    )
                }

                for (i in 0..5) {
                    drawLine(
                        color = NeonCyan.copy(alpha = gridGlow),
                        start = Offset(i * cellPx, 0f),
                        end = Offset(i * cellPx, size.height),
                        strokeWidth = 1.4f,
                    )
                    drawLine(
                        color = NeonCyan.copy(alpha = gridGlow),
                        start = Offset(0f, i * cellPx),
                        end = Offset(size.width, i * cellPx),
                        strokeWidth = 1.4f,
                    )
                }

                if (!robot.collectedCoin) {
                    val cx = (mission.targetX + 0.5f) * cellPx
                    val cy = (mission.targetY + 0.5f) * cellPx
                    drawCircle(
                        color = NeonOrange.copy(alpha = 0.32f),
                        radius = cellPx * 0.30f * coinPulse,
                        center = Offset(cx, cy),
                    )
                    drawCircle(
                        color = NeonOrange,
                        radius = cellPx * 0.18f,
                        center = Offset(cx, cy),
                    )
                    drawCircle(
                        color = Color(0xFFFFE4A8),
                        radius = cellPx * 0.07f,
                        center = Offset(cx - cellPx * 0.04f, cy - cellPx * 0.05f),
                    )
                }

                val rx = (animX + 0.5f) * cellPx
                val ry = (animY + 0.5f) * cellPx + bob
                val robotSize = cellPx * 0.72f * collectScale

                drawCircle(
                    color = NeonCyan.copy(alpha = 0.28f),
                    radius = robotSize * 0.65f,
                    center = Offset(rx, ry),
                )

                rotate(degrees = animAngle, pivot = Offset(rx, ry)) {
                    drawRoundRect(
                        color = NeonPurple,
                        topLeft = Offset(rx - robotSize / 2f, ry - robotSize / 2f),
                        size = Size(robotSize, robotSize),
                        cornerRadius = CornerRadius(robotSize * 0.28f, robotSize * 0.28f),
                    )
                    drawRoundRect(
                        color = NeonCyan.copy(alpha = 0.6f),
                        topLeft = Offset(rx - robotSize / 2f + robotSize * 0.10f, ry - robotSize / 2f + robotSize * 0.10f),
                        size = Size(robotSize * 0.80f, robotSize * 0.80f),
                        cornerRadius = CornerRadius(robotSize * 0.22f, robotSize * 0.22f),
                        style = Stroke(width = 3f, cap = StrokeCap.Round),
                    )

                    val tri = Path().apply {
                        moveTo(rx + robotSize * 0.55f, ry)
                        lineTo(rx + robotSize * 0.20f, ry - robotSize * 0.22f)
                        lineTo(rx + robotSize * 0.20f, ry + robotSize * 0.22f)
                        close()
                    }
                    drawPath(tri, color = NeonOrange)

                    val eyeOffset = Offset(rx + robotSize * 0.05f, ry - robotSize * 0.12f)
                    drawCircle(
                        color = Color(0xFF062018),
                        radius = robotSize * 0.18f,
                        center = eyeOffset,
                    )
                    drawCircle(
                        color = NeonGreen,
                        radius = robotSize * 0.10f,
                        center = eyeOffset,
                    )
                    drawCircle(
                        color = Color.White,
                        radius = robotSize * 0.04f,
                        center = Offset(eyeOffset.x + robotSize * 0.025f, eyeOffset.y - robotSize * 0.025f),
                    )

                    val antennaTop = Offset(rx, ry - robotSize * 0.6f)
                    drawLine(
                        color = NeonGreen,
                        start = Offset(rx, ry - robotSize * 0.5f),
                        end = antennaTop,
                        strokeWidth = 4f,
                        cap = StrokeCap.Round,
                    )
                    drawCircle(
                        color = NeonGreen,
                        radius = robotSize * 0.07f,
                        center = antennaTop,
                    )
                    drawCircle(
                        color = NeonGreen.copy(alpha = 0.4f),
                        radius = robotSize * 0.13f,
                        center = antennaTop,
                    )
                }
            }
        }
    }
}

private fun starterCodeForMission(mission: PlayMission): String =
    """
        // Goal: reach (${mission.targetX}, ${mission.targetY}), then collect()
        move(2)
        turn(right)
        collect()
    """.trimIndent()

private fun buildMissionForLevel(language: String, track: String, levelId: Int): PlayMission {
    fun modPositive(value: Int, m: Int): Int = ((value % m) + m) % m

    val advanced = track.equals("advanced", ignoreCase = true)
    val bonus = if (advanced) 2 else 0
    val seed = language.lowercase().hashCode() xor "track:$track".hashCode() xor (levelId * 7919)
    var tx = modPositive(seed + levelId + bonus, 5)
    var ty = modPositive(seed / 7 + levelId * 2 + bonus, 5)
    if (tx == 0 && ty == 0) {
        tx = modPositive(levelId + 1, 4) + 1
        ty = modPositive(levelId + 2, 4) + 1
    }
    val dirOrdinal = modPositive(seed, 4)
    return PlayMission(
        startX = 0,
        startY = 0,
        targetX = tx.coerceIn(0, 4),
        targetY = ty.coerceIn(0, 4),
        direction = RobotDirection.entries[dirOrdinal],
    )
}

private data class PlayMission(
    val startX: Int,
    val startY: Int,
    val targetX: Int,
    val targetY: Int,
    val direction: RobotDirection,
) {
    fun toRobotState(): RobotState = RobotState(
        x = startX,
        y = startY,
        direction = direction,
        collectedCoin = false,
    )
}

private data class RobotState(
    val x: Int,
    val y: Int,
    val direction: RobotDirection,
    val collectedCoin: Boolean,
)

private enum class RobotDirection {
    UP, RIGHT, DOWN, LEFT
}

private sealed interface DslCommand {
    data class Move(val steps: Int) : DslCommand
    data class Turn(val direction: String) : DslCommand
    data object Collect : DslCommand
}

private fun parseCommands(code: String): Result<List<DslCommand>> = runCatching {
    val commands = code
        .lines()
        .map { it.trim() }
        .filter { it.isNotBlank() }
        .map { line ->
            when {
                line.startsWith("move(") && line.endsWith(")") -> {
                    val steps = line.removePrefix("move(").removeSuffix(")").trim().toIntOrNull()
                        ?: error("move() expects integer steps.")
                    DslCommand.Move(steps)
                }

                line.startsWith("turn(") && line.endsWith(")") -> {
                    val side = line.removePrefix("turn(").removeSuffix(")").trim().lowercase()
                    if (side != "left" && side != "right") {
                        error("turn() expects left or right.")
                    }
                    DslCommand.Turn(side)
                }

                line == "collect()" -> DslCommand.Collect
                else -> error("Unknown command: $line")
            }
        }
    commands
}

private fun applyCommand(
    state: RobotState,
    command: DslCommand,
    mission: PlayMission,
): Result<RobotState> = runCatching {
    when (command) {
        is DslCommand.Move -> {
            var x = state.x
            var y = state.y
            repeat(command.steps) {
                when (state.direction) {
                    RobotDirection.UP -> y--
                    RobotDirection.RIGHT -> x++
                    RobotDirection.DOWN -> y++
                    RobotDirection.LEFT -> x--
                }
                if (x !in 0..4 || y !in 0..4) error("Robot crashed into wall.")
            }
            state.copy(x = x, y = y)
        }

        is DslCommand.Turn -> {
            val next = when (state.direction) {
                RobotDirection.UP -> if (command.direction == "left") RobotDirection.LEFT else RobotDirection.RIGHT
                RobotDirection.RIGHT -> if (command.direction == "left") RobotDirection.UP else RobotDirection.DOWN
                RobotDirection.DOWN -> if (command.direction == "left") RobotDirection.RIGHT else RobotDirection.LEFT
                RobotDirection.LEFT -> if (command.direction == "left") RobotDirection.DOWN else RobotDirection.UP
            }
            state.copy(direction = next)
        }

        DslCommand.Collect -> {
            if (state.x == mission.targetX && state.y == mission.targetY) {
                state.copy(collectedCoin = true)
            } else {
                error("No coin at current position.")
            }
        }
    }
}


@Composable
private fun PlaceholderScreen(
    title: String,
    subtitle: String,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.alpha(0.95f).clickable(enabled = false) {},
            )
        }
    }
}
