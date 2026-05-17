package com.prolearn.codecraftfront.ui.effects

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOutBack
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prolearn.codecraftfront.ui.theme.NeonCyan
import com.prolearn.codecraftfront.ui.theme.NeonGreen
import com.prolearn.codecraftfront.ui.theme.NeonOrange
import com.prolearn.codecraftfront.ui.theme.NeonPurple
import nl.dionsegijn.konfetti.compose.KonfettiView
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.emitter.Emitter
import java.util.concurrent.TimeUnit

@Composable
fun CelebrationOverlay(
    visible: Boolean,
    xpReward: Int,
    onContinue: () -> Unit,
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(220)),
        exit = fadeOut(animationSpec = tween(220)),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.55f))
                .clickable(enabled = false) {},
            contentAlignment = Alignment.Center,
        ) {
            KonfettiView(
                modifier = Modifier.fillMaxSize(),
                parties = remember { buildParties() },
            )
            SuccessCard(xpReward = xpReward, onContinue = onContinue)
        }
    }
}

@Composable
private fun SuccessCard(
    xpReward: Int,
    onContinue: () -> Unit,
) {
    val cardScale = remember { Animatable(0.6f) }
    val checkScale = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        cardScale.animateTo(1f, animationSpec = tween(420, easing = EaseOutBack))
    }
    LaunchedEffect(Unit) {
        checkScale.animateTo(1f, animationSpec = tween(520, easing = EaseOutBack))
    }
    val transition = rememberInfiniteTransition(label = "celebrate")
    val ringPulse by transition.animateFloat(
        initialValue = 1f,
        targetValue = 1.18f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "ring",
    )

    Box(
        modifier = Modifier
            .padding(horizontal = 28.dp)
            .graphicsLayer {
                scaleX = cardScale.value
                scaleY = cardScale.value
            }
            .clip(RoundedCornerShape(28.dp))
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                    ),
                ),
            )
            .padding(horizontal = 24.dp, vertical = 28.dp),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .graphicsLayer {
                            scaleX = ringPulse
                            scaleY = ringPulse
                        }
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(
                                    NeonGreen.copy(alpha = 0.55f),
                                    NeonCyan.copy(alpha = 0.0f),
                                ),
                            ),
                        ),
                )
                Box(
                    modifier = Modifier
                        .size(86.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(listOf(NeonGreen, NeonCyan)),
                        )
                        .graphicsLayer {
                            scaleX = checkScale.value
                            scaleY = checkScale.value
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Check,
                        contentDescription = null,
                        tint = Color(0xFF062018),
                        modifier = Modifier.size(56.dp),
                    )
                }
            }

            Text(
                text = "Mission complete!",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(
                        Brush.horizontalGradient(listOf(NeonOrange, NeonPurple)),
                    )
                    .padding(horizontal = 18.dp, vertical = 8.dp),
            ) {
                Text(
                    text = "+$xpReward XP",
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                )
            }

            Text(
                text = "Coin collected. Next level unlocked.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(4.dp))

            Button(
                onClick = onContinue,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = NeonGreen,
                    contentColor = Color(0xFF062018),
                ),
            ) {
                Icon(
                    imageVector = Icons.Rounded.AutoAwesome,
                    contentDescription = null,
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    text = "Continue",
                    fontWeight = FontWeight.ExtraBold,
                )
            }
        }
    }
}

private fun buildParties(): List<Party> {
    val palette = listOf(
        0xFF39FF88.toInt(),
        0xFF42E8FF.toInt(),
        0xFF9B5CFF.toInt(),
        0xFFFF8A3D.toInt(),
        0xFFFFC44D.toInt(),
    )
    return listOf(
        Party(
            speed = 0f,
            maxSpeed = 32f,
            damping = 0.9f,
            spread = 70,
            angle = 60,
            colors = palette,
            position = Position.Relative(0.0, 0.45),
            emitter = Emitter(duration = 1500, TimeUnit.MILLISECONDS).perSecond(80),
        ),
        Party(
            speed = 0f,
            maxSpeed = 32f,
            damping = 0.9f,
            spread = 70,
            angle = 120,
            colors = palette,
            position = Position.Relative(1.0, 0.45),
            emitter = Emitter(duration = 1500, TimeUnit.MILLISECONDS).perSecond(80),
        ),
        Party(
            speed = 0f,
            maxSpeed = 30f,
            damping = 0.9f,
            spread = 360,
            colors = palette,
            position = Position.Relative(0.5, 0.35),
            emitter = Emitter(duration = 800, TimeUnit.MILLISECONDS).perSecond(140),
        ),
    )
}
