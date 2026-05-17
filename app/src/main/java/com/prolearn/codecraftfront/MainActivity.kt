package com.prolearn.codecraftfront

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.prolearn.codecraftfront.ui.auth.AuthViewModel
import com.prolearn.codecraftfront.ui.navigation.AuthRoutes
import com.prolearn.codecraftfront.ui.navigation.CodeQuestBottomBar
import com.prolearn.codecraftfront.ui.navigation.CodeQuestNavGraph
import com.prolearn.codecraftfront.ui.navigation.topLevelDestinations
import com.prolearn.codecraftfront.ui.theme.CodeCraftFrontTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    // Получаем AuthViewModel из Hilt
    private val authViewModel: AuthViewModel by viewModels()

    override fun onDestroy() {
        super.onDestroy()
        com.prolearn.codecraftfront.ui.sound.SoundFx.release()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val prefsViewModel: com.prolearn.codecraftfront.ui.preferences.UserPreferencesViewModel =
                androidx.lifecycle.viewmodel.compose.viewModel()
            val prefs by prefsViewModel.preferences.collectAsStateWithLifecycle()
            androidx.compose.runtime.LaunchedEffect(prefs.soundEnabled) {
                com.prolearn.codecraftfront.ui.sound.SoundFx.setEnabled(prefs.soundEnabled)
            }
            CodeCraftFrontTheme(darkTheme = prefs.darkTheme) {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                val route = currentDestination?.route.orEmpty()
                val showBottomBar = when (route) {
                    AuthRoutes.Splash, AuthRoutes.Login, AuthRoutes.Register -> false
                    else -> {
                        val tabMatch = topLevelDestinations.any { destination ->
                            currentDestination?.hierarchy?.any { it.route == destination.route } == true
                        }
                        val inLevelFlow = route.startsWith("levels/") ||
                                route.startsWith("mission/") ||
                                route == "leaderboard"
                        tabMatch || inLevelFlow
                    }
                }

                Surface {
                    Scaffold(
                        modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars),
                        bottomBar = {
                            if (showBottomBar) {
                                CodeQuestBottomBar(
                                    destinations = topLevelDestinations,
                                    currentDestination = currentDestination,
                                    onNavigate = { route ->
                                        navController.navigate(route) {
                                            popUpTo(navController.graph.startDestinationId) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    },
                                )
                            }
                        },
                        contentWindowInsets = WindowInsets.navigationBars,
                    ) { innerPadding ->
                        CodeQuestNavGraph(
                            navController = navController,
                            modifier = Modifier.padding(innerPadding),
                        )
                    }
                }
            }
        }

        // Обрабатываем deep link, если активность была запущена по ссылке
        handleDeepLink(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // Обрабатываем deep link, когда активность уже запущена
        handleDeepLink(intent)
    }

    private fun handleDeepLink(intent: Intent?) {
        val data = intent?.data ?: return
        // Проверяем, что ссылка содержит /email-signin
        if (data.scheme == "https" &&
            data.host == "codequestdiplomaproject.web.app" &&
            data.path?.startsWith("/email-signin") == true
        ) {
            // Передаём ссылку в ViewModel для завершения входа
            authViewModel.completeEmailLinkSignIn(data.toString())
        }
    }
}