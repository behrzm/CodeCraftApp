package com.prolearn.codecraftfront.ui.navigation

import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.*
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.*
import com.prolearn.codecraftfront.ui.auth.AuthViewModel
import com.prolearn.codecraftfront.ui.screens.*
import com.prolearn.codecraftfront.ui.theme.*

@Composable
fun CodeQuestNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    val authViewModel: AuthViewModel = hiltViewModel()
    val authState = authViewModel.uiState.collectAsStateWithLifecycle().value
    val context = LocalContext.current

    NavHost(
        navController = navController,
        startDestination = AuthRoutes.Splash,
        modifier = modifier,
        enterTransition = { fadeIn() },
        exitTransition = { fadeOut() },
    ) {
        composable(AuthRoutes.Splash) {
            SplashScreen(
                onStart = {
                    authViewModel.refreshAuthState()
                    val destination = if (authState.isAuthenticated) {
                        CodeQuestDestination.Home.route
                    } else {
                        AuthRoutes.Login
                    }
                    navController.navigate(destination) {
                        popUpTo(AuthRoutes.Splash) { inclusive = true }
                    }
                },
            )
        }

        composable(AuthRoutes.Login) {
            LoginScreen(
                state = authState,
                onLogin = { email, password ->
                    authViewModel.login(email, password) {
                        navController.navigate(CodeQuestDestination.Home.route) {
                            popUpTo(AuthRoutes.Login) { inclusive = true }
                        }
                    }
                },
                onGoogleToken = { idToken ->
                    authViewModel.loginWithGoogle(idToken) {
                        navController.navigate(CodeQuestDestination.Home.route) {
                            popUpTo(AuthRoutes.Login) { inclusive = true }
                        }
                    }
                },
                onGoogleError = { message -> authViewModel.setError(message) },
                onForgotPassword = { email -> authViewModel.sendPasswordReset(email) },
                onDismissMessage = authViewModel::clearMessages,
                onOpenRegister = { navController.navigate(AuthRoutes.Register) },
                onPhoneCheckExists = { phoneNumber ->
                    authViewModel.checkIfPhoneExists(phoneNumber)
                },
                onSendPhoneCode = { phoneNumber, activity ->
                    authViewModel.sendPhoneVerificationCode(phoneNumber, activity)
                },
                onVerifyPhoneCode = { smsCode ->
                    authViewModel.signInWithPhoneCode(smsCode) {
                        navController.navigate(CodeQuestDestination.Home.route) {
                            popUpTo(AuthRoutes.Login) { inclusive = true }
                        }
                    }
                },
                onGithubLogin = { code ->
                    authViewModel.completeGitHubSignIn(code) {
                        navController.navigate(CodeQuestDestination.Home.route) {
                            popUpTo(AuthRoutes.Login) { inclusive = true }
                        }
                    }
                },
                onGuestSignIn = {
                    authViewModel.signInAnonymously {
                        navController.navigate(CodeQuestDestination.Home.route) {
                            popUpTo(AuthRoutes.Login) { inclusive = true }
                        }
                    }
                }
            )
        }

        composable(AuthRoutes.Register) {
            RegisterScreen(
                state = authState,
                onRegister = { email, password, confirmPassword ->
                    authViewModel.register(email, password, confirmPassword) {
                        // Не переходим сразу, ждём привязку телефона
                    }
                },
                onDismissMessage = authViewModel::clearMessages,
                onOpenLogin = { navController.popBackStack() },
                onSendEmailLink = { email ->
                    authViewModel.sendEmailLink(email)
                },
                onCompleteEmailLink = { link ->
                    authViewModel.completeEmailLinkSignIn(link)
                },
                onSetPasswordAfterLink = { password, confirmPassword ->
                    authViewModel.setPasswordAfterEmailLink(password, confirmPassword) {
                        navController.navigate(CodeQuestDestination.Home.route) {
                            popUpTo(AuthRoutes.Register) { inclusive = true }
                        }
                    }
                },
                onLinkPhone = { phoneNumber ->
                    val activity = context as? android.app.Activity ?: return@RegisterScreen
                    authViewModel.sendPhoneLinkCode(phoneNumber, activity)
                },
                onVerifyPhoneLink = { smsCode ->
                    authViewModel.verifyPhoneLinkCode(smsCode)
                },
                onSkipPhoneLink = {
                    navController.navigate(CodeQuestDestination.Home.route) {
                        popUpTo(AuthRoutes.Register) { inclusive = true }
                    }
                }
            )
        }

        composable(CodeQuestDestination.Home.route) {
            HomeScreen(
                onStartDailyChallenge = {
                    val cal = java.util.Calendar.getInstance()
                    val dayOfYear = cal.get(java.util.Calendar.DAY_OF_YEAR)
                    val levelId = (dayOfYear % 5) + 1
                    navController.navigate(
                        CodeQuestDestination.Mission.createRoute(
                            language = "Python",
                            track = "daily",
                            levelId = levelId,
                        ),
                    )
                },
            )
        }

        composable(CodeQuestDestination.Languages.route) {
            LanguagesScreen(
                onOpenLevels = { language ->
                    navController.navigate(CodeQuestDestination.Levels.createRoute(language))
                },
            )
        }

        composable(
            route = CodeQuestDestination.Levels.route,
            arguments = listOf(navArgument("language") { type = NavType.StringType }),
        ) { backStackEntry ->
            LevelsScreen(
                language = Uri.decode(backStackEntry.arguments?.getString("language").orEmpty()),
                onOpenPlay = { lang, track, levelId ->
                    navController.navigate(CodeQuestDestination.Mission.createRoute(lang, track, levelId))
                },
            )
        }

        composable(CodeQuestDestination.Play.route) {
            PlayHubScreen(
                onOpenLanguages = { navController.navigate(CodeQuestDestination.Languages.route) },
            )
        }

        composable(
            route = CodeQuestDestination.Mission.route,
            arguments = listOf(
                navArgument("language") { type = NavType.StringType },
                navArgument("track") { type = NavType.StringType },
                navArgument("levelId") { type = NavType.IntType },
            ),
        ) { entry ->
            PlayScreen(
                language = Uri.decode(entry.arguments?.getString("language").orEmpty()),
                track = entry.arguments?.getString("track").orEmpty(),
                levelId = entry.arguments?.getInt("levelId") ?: 1,
            )
        }

        composable(CodeQuestDestination.Stats.route) {
            StatsScreen(
                onOpenLeaderboard = { navController.navigate(CodeQuestDestination.Leaderboard.route) },
            )
        }

        composable(CodeQuestDestination.Leaderboard.route) { LeaderboardScreen() }

        composable(CodeQuestDestination.Profile.route) {
            ProfileScreen(
                email = authState.email,
                onSignOut = {
                    authViewModel.signOut {
                        navController.navigate(AuthRoutes.Login) {
                            popUpTo(navController.graph.id) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                },
            )
        }
    }
}

object AuthRoutes {
    const val Splash = "splash"
    const val Login = "login"
    const val Register = "register"
}

private val TabAccents = listOf(NeonGreen, NeonCyan, NeonOrange, NeonPurple, NeonGreen)

@Composable
fun CodeQuestBottomBar(
    destinations: List<CodeQuestDestination>,
    currentDestination: NavDestination?,
    onNavigate: (String) -> Unit,
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            destinations.forEachIndexed { index, destination ->
                val selected = currentDestination?.hierarchy?.any { it.route == destination.route } == true
                val accent = TabAccents[index % TabAccents.size]
                BottomBarPill(
                    destination = destination,
                    selected = selected,
                    accent = accent,
                    onClick = { onNavigate(destination.route) },
                )
            }
        }
    }
}

@Composable
private fun BottomBarPill(
    destination: CodeQuestDestination,
    selected: Boolean,
    accent: Color,
    onClick: () -> Unit,
) {
    val iconScale by animateFloatAsState(
        targetValue = if (selected) 1.15f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = 380f),
        label = "iconScale",
    )
    val verticalPadding by animateDpAsState(
        targetValue = if (selected) 10.dp else 12.dp,
        animationSpec = spring(stiffness = 360f),
        label = "pillPad",
    )
    val interactionSource = remember { MutableInteractionSource() }

    Row(
        modifier = Modifier
            .height(46.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(
                brush = if (selected) {
                    Brush.horizontalGradient(
                        listOf(accent.copy(alpha = 0.22f), accent.copy(alpha = 0.10f)),
                    )
                } else {
                    Brush.horizontalGradient(listOf(Color.Transparent, Color.Transparent))
                },
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            )
            .padding(horizontal = if (selected) 14.dp else 12.dp, vertical = verticalPadding),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = destination.icon,
            contentDescription = stringResource(destination.titleRes),
            tint = if (selected) accent else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.scale(iconScale),
        )
        AnimatedVisibility(
            visible = selected,
            enter = fadeIn(animationSpec = spring(stiffness = 360f)) +
                    expandHorizontally(animationSpec = spring(stiffness = 360f)),
            exit = fadeOut(animationSpec = spring(stiffness = 360f)) +
                    shrinkHorizontally(animationSpec = spring(stiffness = 360f)),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(destination.titleRes),
                    color = accent,
                    fontWeight = FontWeight.ExtraBold,
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }
    }
}