package com.prolearn.codecraftfront.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Profile(
    val id: String,
    @SerialName("display_name") val displayName: String? = null,
    val email: String? = null,
    val xp: Int = 0,
    val level: Int = 1,
    val streak: Int = 0
)

@Serializable
data class XpHistoryEntry(
    val id: Long,
    @SerialName("user_id") val userId: String,
    val amount: Int,
    val reason: String? = null,
    @SerialName("created_at") val createdAt: String? = null
)

@Serializable
data class Achievement(
    val id: Long,
    @SerialName("user_id") val userId: String,
    @SerialName("badge_code") val badgeCode: String,
    @SerialName("unlocked_at") val unlockedAt: String? = null
)

@Serializable
data class LevelProgress(
    val id: Long,
    @SerialName("user_id") val userId: String,
    val language: String,
    val track: String,
    @SerialName("level_id") val levelId: Int,
    val stars: Int = 0,
    val completed: Boolean = false,
    @SerialName("completed_at") val completedAt: String? = null
)

@Serializable
data class LearningProfile(
    val id: Long,
    @SerialName("user_id") val userId: String,
    val language: String,
    val track: String,
    @SerialName("level_id") val levelId: Int,
    val attempts: Int = 0,
    @SerialName("failed_commands") val failedCommands: String? = null,
    @SerialName("last_error") val lastError: String? = null
)
