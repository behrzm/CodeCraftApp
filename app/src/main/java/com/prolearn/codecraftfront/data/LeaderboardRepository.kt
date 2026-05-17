package com.prolearn.codecraftfront.data

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.Serializable
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import com.google.firebase.auth.FirebaseAuth

@Serializable
data class ProfileDto(
    val id: String,
    val display_name: String? = null,
    val xp: Int = 0,
    val level: Int = 1,
    val streak: Int = 0
)

data class LeaderboardEntry(
    val userId: String,
    val displayName: String,
    val xp: Int,
    val level: Int = 1
)

@Singleton
class LeaderboardRepository @Inject constructor(
    private val httpClient: HttpClient
) {
    // Backend URL - указывайте свой,адрес
    private val backendUrl = "http://10.0.2.2:8080/api"  // 10.0.2.2 доступ к localhost от эмулятора
    // Для реального устройства: "https://your-backend-domain.com/api"
    
    // Fallback на Supabase если backend не доступен
    private val supabaseUrl = "https://diuvzzrbwxdufagcdbuz.supabase.co/rest/v1"
    private val anonKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImRpdXZ6enJid3hkdWZhZ2NkYnV6Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3Nzg4NTMwODIsImV4cCI6MjA5NDQyOTA4Mn0.Feg92AbV0UE3H3lFNnrOKqMVKNlhsO6hOhL1bVyQzk4"

    suspend fun fetchTop(limit: Int = 25): List<LeaderboardEntry> {
        return try {
            // Пробуем backend
            val response = httpClient.get("$backendUrl/leaderboard?limit=$limit")
                .body<String>()
            val profiles = parseProfiles(response)
            profiles.map {
                LeaderboardEntry(
                    userId = it.id,
                    displayName = it.display_name ?: "Coder",
                    xp = it.xp,
                    level = it.level
                )
            }
        } catch (e: Exception) {
            // Fallback на Supabase
            android.util.Log.w("LeaderboardRepository", "Backend failed, using Supabase fallback", e)
            fetchTopFromSupabase(limit)
        }
    }

    suspend fun fetchCurrentUser(): LeaderboardEntry? {
        return try {
            val token = getFirebaseToken() ?: return null
            val response = httpClient.get("$backendUrl/profile/me") {
                header("Authorization", "Bearer $token")
            }.body<String>()
            val profile = parseProfile(response)
            if (profile != null) {
                LeaderboardEntry(
                    userId = profile.id,
                    displayName = profile.display_name ?: "You",
                    xp = profile.xp,
                    level = profile.level
                )
            } else null
        } catch (e: Exception) {
            android.util.Log.w("LeaderboardRepository", "Backend failed, using Supabase fallback", e)
            fetchCurrentUserFromSupabase()
        }
    }

    suspend fun incrementCurrentUserXp(displayName: String, amount: Int) {
        try {
            val token = getFirebaseToken() ?: return
            val response = httpClient.post("$backendUrl/profile/me/xp") {
                header("Authorization", "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody(mapOf("amount" to amount, "reason" to "level_completed"))
            }
            if (!response.status.isSuccess()) {
                throw Exception("Failed to increment XP: ${response.status}")
            }
        } catch (e: Exception) {
            android.util.Log.e("LeaderboardRepository", "incrementCurrentUserXp failed", e)
        }
    }

    // FALLBACK методы для работы с Supabase
    private suspend fun fetchTopFromSupabase(limit: Int): List<LeaderboardEntry> {
        val response: HttpResponse = httpClient.get("$supabaseUrl/profiles") {
            header("apikey", anonKey)
            parameter("select", "*")
            parameter("order", "xp.desc")
            parameter("limit", limit)
        }
        val body = response.body<String>()
        val profiles = parseProfiles(body)
        return profiles.map {
            LeaderboardEntry(
                userId = it.id,
                displayName = it.display_name ?: "Coder",
                xp = it.xp,
                level = it.level
            )
        }
    }

    private suspend fun fetchCurrentUserFromSupabase(): LeaderboardEntry? {
        val supabaseUserId = currentUserSupabaseId() ?: return null
        val response: HttpResponse = httpClient.get("$supabaseUrl/profiles") {
            header("apikey", anonKey)
            parameter("select", "*")
            parameter("id", "eq.$supabaseUserId")
        }
        val body = response.body<String>()
        val profiles = parseProfiles(body)
        val profile = profiles.firstOrNull() ?: return null
        return LeaderboardEntry(
            userId = profile.id,
            displayName = profile.display_name ?: "You",
            xp = profile.xp,
            level = profile.level
        )
    }

    // Utility: get Firebase token
    private suspend fun getFirebaseToken(): String? {
        return try {
            val user = FirebaseAuth.getInstance().currentUser ?: return null
            user.getIdToken(false).result?.token
        } catch (e: Exception) {
            android.util.Log.e("LeaderboardRepository", "Failed to get Firebase token", e)
            null
        }
    }

    // Utility: convert firebase uid -> UUID string
    private fun firebaseUidToUUID(firebaseUid: String): UUID {
        val namespace = UUID.fromString("6ba7b810-9dad-11d1-80b4-00c04fd430c8")
        return UUID.nameUUIDFromBytes((namespace.toString() + firebaseUid).toByteArray())
    }

    private fun currentUserSupabaseId(): String? {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        return uid?.let { firebaseUidToUUID(it).toString() }
    }

    // JSON parsing helper
    private fun parseProfiles(json: String): List<ProfileDto> {
        return try {
            val parser = kotlinx.serialization.json.Json { ignoreUnknownKeys = true }
            parser.decodeFromString<List<ProfileDto>>(json)
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun parseProfile(json: String): ProfileDto? {
        return try {
            val parser = kotlinx.serialization.json.Json { ignoreUnknownKeys = true }
            parser.decodeFromString<ProfileDto>(json)
        } catch (e: Exception) {
            null
        }
    }
}