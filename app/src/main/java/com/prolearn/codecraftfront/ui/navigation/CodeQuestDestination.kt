package com.prolearn.codecraftfront.ui.navigation

import android.net.Uri
import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material.icons.rounded.Terminal
import androidx.compose.ui.graphics.vector.ImageVector
import com.prolearn.codecraftfront.R

sealed class CodeQuestDestination(
    val route: String,
    val icon: ImageVector,
    @StringRes val titleRes: Int,
) {
    data object Home : CodeQuestDestination("home", Icons.Rounded.Home, R.string.tab_home)
    data object Languages : CodeQuestDestination("languages", Icons.Rounded.Terminal, R.string.tab_languages)
    data object Play : CodeQuestDestination("play", Icons.Rounded.PlayCircle, R.string.tab_play)
    data object Stats : CodeQuestDestination("stats", Icons.Rounded.BarChart, R.string.tab_stats)
    data object Profile : CodeQuestDestination("profile", Icons.Rounded.Person, R.string.tab_profile)

    data object Levels {
        const val route = "levels/{language}"
        fun createRoute(language: String): String = "levels/${Uri.encode(language)}"
    }

    data object Mission {
        const val route = "mission/{language}/{track}/{levelId}"
        fun createRoute(language: String, track: String, levelId: Int): String =
            "mission/${Uri.encode(language)}/$track/$levelId"
    }

    data object Leaderboard {
        const val route = "leaderboard"
    }
}

val topLevelDestinations = listOf(
    CodeQuestDestination.Home,
    CodeQuestDestination.Languages,
    CodeQuestDestination.Play,
    CodeQuestDestination.Stats,
    CodeQuestDestination.Profile,
)
