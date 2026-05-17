package com.prolearn.codecraftfront.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prolearn.codecraftfront.data.LeaderboardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlayViewModel @Inject constructor(
    private val repository: LeaderboardRepository
) : ViewModel() {
    fun awardXp(displayName: String, amount: Int) {
        viewModelScope.launch {
            repository.incrementCurrentUserXp(displayName, amount)
        }
    }
}