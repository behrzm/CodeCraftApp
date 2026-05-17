package com.prolearn.codecraftfront.ui.ai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import com.prolearn.codecraftfront.BuildConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HintMessage(
    val author: Author,
    val text: String,
) {
    enum class Author { Player, Assistant }
}

data class AiAssistantUiState(
    val isOpen: Boolean = false,
    val isLoading: Boolean = false,
    val messages: List<HintMessage> = emptyList(),
    val errorMessage: String? = null,
)

data class HintRequestContext(
    val language: String,
    val track: String,
    val levelId: Int,
    val storyPrompt: String,
    val playerCode: String,
    val lastError: String?,
    val failedAttempts: Int,
)

class AiAssistantViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(AiAssistantUiState())
    val uiState: StateFlow<AiAssistantUiState> = _uiState.asStateFlow()

    private val model: GenerativeModel? = runCatching {
        val key = BuildConfig.GEMINI_API_KEY
        if (key.isBlank()) null else GenerativeModel(modelName = "gemini-1.5-flash", apiKey = key)
    }.getOrNull()

    fun open() {
        _uiState.update { it.copy(isOpen = true, errorMessage = null) }
    }

    fun close() {
        _uiState.update { it.copy(isOpen = false) }
    }

    fun resetHistory() {
        _uiState.update { it.copy(messages = emptyList(), errorMessage = null) }
    }

    fun requestHint(context: HintRequestContext) {
        val gemini = model
        if (gemini == null) {
            _uiState.update {
                it.copy(
                    isOpen = true,
                    errorMessage = "Gemini API key is missing. Add GEMINI_API_KEY to local.properties.",
                )
            }
            return
        }
        val playerMessage = buildPlayerMessage(context)
        _uiState.update {
            it.copy(
                isOpen = true,
                isLoading = true,
                errorMessage = null,
                messages = it.messages + HintMessage(HintMessage.Author.Player, playerMessage),
            )
        }
        viewModelScope.launch {
            runCatching {
                gemini.generateContent(buildPrompt(context)).text.orEmpty()
            }.onSuccess { reply ->
                val cleaned = reply.ifBlank { "Sorry, I could not produce a hint right now." }
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        messages = it.messages + HintMessage(HintMessage.Author.Assistant, cleaned),
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = error.localizedMessage ?: "AI request failed.",
                    )
                }
            }
        }
    }

    private fun buildPlayerMessage(context: HintRequestContext): String = buildString {
        append("Help me on level ")
        append(context.levelId)
        append(" (")
        append(context.track)
        append(", ")
        append(context.language)
        append("). Failed attempts: ")
        append(context.failedAttempts)
        append('.')
    }

    private fun buildPrompt(context: HintRequestContext): String {
        val depth = when {
            context.failedAttempts >= 3 -> "FULL"
            context.failedAttempts == 2 -> "STRONG"
            else -> "GENTLE"
        }
        return """
            You are CodeQuest's friendly cyber-coach for a Duolingo-style coding game.
            The player controls a robot on a 5x5 grid using a tiny DSL with these commands:
              - move(n)        // n = positive integer, robot steps forward
              - turn(left|right)
              - collect()      // picks the coin if standing on it
            Robot always starts at (0,0). The grid is 5x5 (0..4 on each axis).
            
            Player context:
              - Language theme: ${context.language}
              - Track: ${context.track}
              - Level: ${context.levelId}
              - Mission story: ${context.storyPrompt}
              - Failed attempts so far: ${context.failedAttempts}
              - Last execution error (may be null): ${context.lastError ?: "none"}
            
            Current player code:
            ```
            ${context.playerCode}
            ```
            
            Hint mode = $depth:
              - GENTLE: a single short tip (1-2 sentences), no full solution.
              - STRONG: explain what is wrong + a partial code suggestion (1-2 lines).
              - FULL: full friendly explanation + corrected DSL snippet inside a code block.
            
            Style: warm, energetic, gaming tone. Use English. Avoid shaming. Keep response under 120 words.
        """.trimIndent()
    }
}
