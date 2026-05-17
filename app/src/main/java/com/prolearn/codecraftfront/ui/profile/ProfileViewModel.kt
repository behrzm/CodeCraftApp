package com.prolearn.codecraftfront.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prolearn.codecraftfront.data.LeaderboardEntry
import com.prolearn.codecraftfront.data.LeaderboardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val xp: Int = 0,
    val level: Int = 1,
    val displayName: String? = null,
    val streak: Int = 7,
    val wins: Int = 0,
    val isLoaded: Boolean = false,
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repository: LeaderboardRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            val entry = repository.fetchCurrentUser()
            _uiState.update {
                if (entry != null) {
                    it.copy(
                        xp = entry.xp,
                        level = entry.level,
                        displayName = entry.displayName,
                        wins = entry.xp / 50,
                        isLoaded = true,
                    )
                } else {
                    it.copy(isLoaded = true)
                }
            }
        }
    }
}