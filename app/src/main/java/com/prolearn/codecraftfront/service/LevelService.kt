package com.prolearn.codecraftfront.service

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.tasks.await
import com.google.firebase.auth.FirebaseAuth
import javax.inject.Inject
import javax.inject.Singleton

data class SubmissionResponse(
    val correct: Boolean,
    val xp_earned: Int,
    val hp_used: Int,
    val message: String
)

data class LevelData(
    val id: Long,
    val language: String,
    val track: String,
    val title: String,
    val description: String?,
    val difficulty: Int?,
    val xp_reward: Int?
)

@Singleton
class LevelService @Inject constructor(
    private val httpClient: HttpClient
) {
    // Backend URL
    private val backendUrl = "http://10.0.2.2:8080/api"  // Для эмулятора
    // Для real device: "https://your-backend-domain.com/api"

    /**
     * Отправить решение уровня на backend для проверки
     */
    suspend fun submitSolution(levelId: Long, code: String): SubmissionResponse? {
        return try {
            val token = getFirebaseToken() ?: run {
                android.util.Log.e("LevelService", "No Firebase token")
                return null
            }

            val response = httpClient.post("$backendUrl/levels/$levelId/submit") {
                header("Authorization", "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody(mapOf("code" to code))
            }

            if (response.status.isSuccess()) {
                val body = response.body<String>()
                parseSubmissionResponse(body)
            } else {
                android.util.Log.e("LevelService", "Submit failed with status ${response.status}")
                null
            }
        } catch (e: Exception) {
            android.util.Log.e("LevelService", "submitSolution error", e)
            null
        }
    }

    /**
     * Получить все уровни по языку и треку
     */
    suspend fun getLevels(language: String? = null, track: String? = null): List<LevelData> {
        return try {
            var url = "$backendUrl/levels"
            if (language != null) url += "?language=$language"
            if (track != null) url += if (language != null) "&track=$track" else "?track=$track"

            val response = httpClient.get(url)
            val body = response.body<String>()
            parseLevelsList(body)
        } catch (e: Exception) {
            android.util.Log.e("LevelService", "getLevels error", e)
            emptyList()
        }
    }

    /**
     * Получить детали одного уровня
     */
    suspend fun getLevel(levelId: Long): LevelData? {
        return try {
            val response = httpClient.get("$backendUrl/levels/$levelId")
            val body = response.body<String>()
            parseLevel(body)
        } catch (e: Exception) {
            android.util.Log.e("LevelService", "getLevel error", e)
            null
        }
    }

    /**
     * Получить прогресс пользователя по всем уровням
     */
    suspend fun getProgress(): Map<String, Any> {
        return try {
            val token = getFirebaseToken() ?: return emptyMap()
            val response = httpClient.get("$backendUrl/progress") {
                header("Authorization", "Bearer $token")
            }
            val body = response.body<String>()
            parseProgress(body)
        } catch (e: Exception) {
            android.util.Log.e("LevelService", "getProgress error", e)
            emptyMap()
        }
    }

    /**
     * Получить прогресс по конкретному уровню
     */
    suspend fun getLevelProgress(levelId: Long): Map<String, Any> {
        return try {
            val token = getFirebaseToken() ?: return emptyMap()
            val response = httpClient.get("$backendUrl/progress/level/$levelId") {
                header("Authorization", "Bearer $token")
            }
            val body = response.body<String>()
            parseProgress(body)
        } catch (e: Exception) {
            android.util.Log.e("LevelService", "getLevelProgress error", e)
            emptyMap()
        }
    }

    // Helpers
    private suspend fun getFirebaseToken(): String? {
        return try {
            val user = FirebaseAuth.getInstance().currentUser ?: return null
            user.getIdToken(false).result?.token
        } catch (e: Exception) {
            android.util.Log.e("LevelService", "Failed to get Firebase token", e)
            null
        }
    }

    private fun parseSubmissionResponse(json: String): SubmissionResponse? {
        return try {
            val parser = kotlinx.serialization.json.Json { ignoreUnknownKeys = true }
            parser.decodeFromString<SubmissionResponse>(json)
        } catch (e: Exception) {
            android.util.Log.e("LevelService", "Failed to parse submission response", e)
            null
        }
    }

    private fun parseLevelsList(json: String): List<LevelData> {
        return try {
            val parser = kotlinx.serialization.json.Json { ignoreUnknownKeys = true }
            parser.decodeFromString<List<LevelData>>(json)
        } catch (e: Exception) {
            android.util.Log.e("LevelService", "Failed to parse levels list", e)
            emptyList()
        }
    }

    private fun parseLevel(json: String): LevelData? {
        return try {
            val parser = kotlinx.serialization.json.Json { ignoreUnknownKeys = true }
            parser.decodeFromString<LevelData>(json)
        } catch (e: Exception) {
            android.util.Log.e("LevelService", "Failed to parse level", e)
            null
        }
    }

    private fun parseProgress(json: String): Map<String, Any> {
        return try {
            val parser = kotlinx.serialization.json.Json { ignoreUnknownKeys = true }
            parser.decodeFromString<Map<String, Any>>(json)
        } catch (e: Exception) {
            android.util.Log.e("LevelService", "Failed to parse progress", e)
            emptyMap()
        }
    }
}

