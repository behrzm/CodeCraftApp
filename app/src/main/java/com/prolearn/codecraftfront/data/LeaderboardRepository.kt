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
    private val baseUrl = "https://diuvzzrbwxdufagcdbuz.supabase.co/rest/v1"
    private val anonKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImRpdXZ6enJid3hkdWZhZ2NkYnV6Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3Nzg4NTMwODIsImV4cCI6MjA5NDQyOTA4Mn0.Feg92AbV0UE3H3lFNnrOKqMVKNlhsO6hOhL1bVyQzk4"

    private fun HttpRequestBuilder.supabaseHeaders() {
        header("apikey", anonKey)
        header("Authorization", "Bearer $anonKey")
        contentType(ContentType.Application.Json)
    }

    private fun firebaseUidToUUID(firebaseUid: String): UUID {
        val namespace = UUID.fromString("6ba7b810-9dad-11d1-80b4-00c04fd430c8")
        return UUID.nameUUIDFromBytes((namespace.toString() + firebaseUid).toByteArray())
    }

    private fun currentUserSupabaseId(): String? {
        val uid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
        return uid?.let { firebaseUidToUUID(it).toString() }
    }

    suspend fun fetchTop(limit: Int = 25): List<LeaderboardEntry> {
        val response: HttpResponse = httpClient.get("$baseUrl/profiles") {
            supabaseHeaders()
            parameter("select", "*")
            parameter("order", "xp.desc")
            parameter("limit", limit)
        }
        val profiles: List<ProfileDto> = response.body()
        return profiles.map {
            LeaderboardEntry(
                userId = it.id,
                displayName = it.display_name ?: "Coder",
                xp = it.xp,
                level = it.level
            )
        }
    }

    suspend fun fetchCurrentUser(): LeaderboardEntry? {
        val supabaseUserId = currentUserSupabaseId() ?: return null
        val response: HttpResponse = httpClient.get("$baseUrl/profiles") {
            supabaseHeaders()
            parameter("select", "*")
            parameter("id", "eq.$supabaseUserId")
        }
        val profiles: List<ProfileDto> = response.body()
        val profile = profiles.firstOrNull() ?: return null
        return LeaderboardEntry(
            userId = profile.id,
            displayName = profile.display_name ?: "You",
            xp = profile.xp,
            level = profile.level
        )
    }

    suspend fun incrementCurrentUserXp(displayName: String, amount: Int) {
        val supabaseUserId = currentUserSupabaseId() ?: return
        try {
            val getResponse: HttpResponse = httpClient.get("$baseUrl/profiles") {
                supabaseHeaders()
                parameter("select", "id,xp,level")
                parameter("id", "eq.$supabaseUserId")
            }
            val existing: List<ProfileDto> = getResponse.body()

            if (existing.isEmpty()) {
                httpClient.post("$baseUrl/profiles") {
                    supabaseHeaders()
                    setBody(listOf(ProfileDto(id = supabaseUserId, display_name = displayName, xp = amount, level = 1)))
                }
            } else {
                val current = existing.first()
                val newXp = current.xp + amount
                val newLevel = (newXp / 400) + 1
                httpClient.patch("$baseUrl/profiles") {
                    supabaseHeaders()
                    header("Prefer", "return=minimal")
                    parameter("id", "eq.$supabaseUserId")
                    setBody(mapOf("xp" to newXp, "level" to newLevel))
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("SupabaseError", "incrementCurrentUserXp failed", e)
        }
    }
}