package com.codecraft.backend.service;

import com.codecraft.backend.dto.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;

@Service
public class SupabaseService {

    private final WebClient webClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public SupabaseService(@Value("${supabase.url}") String baseUrl,
                           @Value("${supabase.service-role-key}") String serviceRoleKey,
                           @Value("${supabase.apikey}") String anonKey) {
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("apikey", anonKey)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + serviceRoleKey)
                .build();
    }

    // ========== PROFILES ==========
    public List<ProfileDto> fetchTopProfiles(int limit) throws Exception {
        String uri = "/profiles?select=*&order=xp.desc&limit=" + limit;
        String body = webClient.get().uri(uri)
                .retrieve()
                .bodyToMono(String.class)
                .block();
        return objectMapper.readValue(body, new TypeReference<List<ProfileDto>>(){});
    }

    public ProfileDto fetchProfileById(String id) throws Exception {
        String uri = "/profiles?select=*&id=eq." + id;
        String body = webClient.get().uri(uri)
                .retrieve()
                .bodyToMono(String.class)
                .block();
        List<ProfileDto> list = objectMapper.readValue(body, new TypeReference<List<ProfileDto>>(){});
        if (list.isEmpty()) return null;
        return list.get(0);
    }

    public void createProfileIfNotExists(String id, String displayName, String email) {
        try {
            ProfileDto existing = fetchProfileById(id);
            if (existing != null) return;
            Map<String, Object> payload = new HashMap<>();
            payload.put("id", id);
            payload.put("display_name", displayName);
            payload.put("email", email);
            payload.put("xp", 0);
            payload.put("level", 1);
            payload.put("streak", 0);
            webClient.post().uri("/profiles")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(Collections.singletonList(payload))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void addXpForUser(String id, int amount, String reason) throws Exception {
        ProfileDto existing = fetchProfileById(id);
        if (existing == null) {
            Map<String, Object> payload = new HashMap<>();
            payload.put("id", id);
            payload.put("display_name", "Coder");
            payload.put("xp", amount);
            payload.put("level", 1);
            payload.put("streak", 0);
            webClient.post().uri("/profiles")
                    .bodyValue(Collections.singletonList(payload))
                    .retrieve().bodyToMono(String.class).block();
        } else {
            int newXp = existing.xp + amount;
            int newLevel = (newXp / 400) + 1;
            Map<String, Object> update = new HashMap<>();
            update.put("xp", newXp);
            update.put("level", newLevel);
            webClient.patch().uri(uriBuilder -> uriBuilder.path("/profiles").queryParam("id","eq."+id).build())
                    .header("Prefer","return=minimal")
                    .bodyValue(update)
                    .retrieve().bodyToMono(String.class).block();
        }
        // insert xp_history
        Map<String,Object> hist = new HashMap<>();
        hist.put("user_id", id);
        hist.put("amount", amount);
        hist.put("reason", reason);
        webClient.post().uri("/xp_history")
                .bodyValue(Collections.singletonList(hist))
                .retrieve().bodyToMono(String.class).block();
    }

    // ========== LEVELS ==========
    public List<LevelDto> fetchLevels(String language, String track) throws Exception {
        String uri = "/levels?select=*";
        if (language != null) uri += "&language=eq." + language;
        if (track != null) uri += "&track=eq." + track;
        uri += "&order=level_number.asc";
        String body = webClient.get().uri(uri)
                .retrieve()
                .bodyToMono(String.class)
                .block();
        return objectMapper.readValue(body, new TypeReference<List<LevelDto>>(){});
    }

    public LevelDto fetchLevelById(long levelId) throws Exception {
        String uri = "/levels?select=*&id=eq." + levelId;
        String body = webClient.get().uri(uri)
                .retrieve()
                .bodyToMono(String.class)
                .block();
        List<LevelDto> list = objectMapper.readValue(body, new TypeReference<List<LevelDto>>(){});
        if (list.isEmpty()) return null;
        return list.get(0);
    }

    // ========== LEVEL PROGRESS ==========
    public List<LevelProgressDto> fetchProgressForUser(String userId) throws Exception {
        String uri = "/level_progress?select=*&user_id=eq." + userId;
        String body = webClient.get().uri(uri)
                .retrieve()
                .bodyToMono(String.class)
                .block();
        return objectMapper.readValue(body, new TypeReference<List<LevelProgressDto>>(){});
    }

    public LevelProgressDto fetchProgressForLevel(String userId, long levelId) throws Exception {
        String uri = "/level_progress?select=*&user_id=eq." + userId + "&level_id=eq." + levelId;
        String body = webClient.get().uri(uri)
                .retrieve()
                .bodyToMono(String.class)
                .block();
        List<LevelProgressDto> list = objectMapper.readValue(body, new TypeReference<List<LevelProgressDto>>(){});
        if (list.isEmpty()) return null;
        return list.get(0);
    }

    public void createOrUpdateLevelProgress(String userId, long levelId, String language, String track,
                                            int stars, boolean completed) throws Exception {
        Map<String, Object> payload = new HashMap<>();
        payload.put("user_id", userId);
        payload.put("level_id", levelId);
        payload.put("language", language);
        payload.put("track", track);
        payload.put("stars", stars);
        payload.put("completed", completed);
        if (completed) {
            payload.put("completed_at", Instant.now().toString());
        }
        payload.put("attempts", 1);

        // Try update first
        try {
            webClient.patch().uri(uriBuilder -> uriBuilder.path("/level_progress")
                    .queryParam("user_id", "eq." + userId)
                    .queryParam("level_id", "eq." + levelId)
                    .build())
                    .header("Prefer", "return=minimal")
                    .bodyValue(payload)
                    .retrieve().bodyToMono(String.class).block();
        } catch (Exception ex) {
            // Insert if update fails
            webClient.post().uri("/level_progress")
                    .bodyValue(Collections.singletonList(payload))
                    .retrieve().bodyToMono(String.class).block();
        }
    }

    // ========== ACHIEVEMENTS ==========
    public List<AchievementDto> fetchAchievementsForUser(String userId) throws Exception {
        String uri = "/achievements?select=*&user_id=eq." + userId;
        String body = webClient.get().uri(uri)
                .retrieve()
                .bodyToMono(String.class)
                .block();
        return objectMapper.readValue(body, new TypeReference<List<AchievementDto>>(){});
    }

    public void unlockAchievement(String userId, String badgeCode) throws Exception {
        Map<String, Object> payload = new HashMap<>();
        payload.put("user_id", userId);
        payload.put("badge_code", badgeCode);
        payload.put("unlocked_at", Instant.now().toString());
        webClient.post().uri("/achievements")
                .bodyValue(Collections.singletonList(payload))
                .retrieve().bodyToMono(String.class).block();
    }

    // ========== USER PREFERENCES ==========
    public UserPreferencesDto fetchPreferencesForUser(String userId) throws Exception {
        String uri = "/user_preferences?select=*&user_id=eq." + userId;
        String body = webClient.get().uri(uri)
                .retrieve()
                .bodyToMono(String.class)
                .block();
        List<UserPreferencesDto> list = objectMapper.readValue(body, new TypeReference<List<UserPreferencesDto>>(){});
        if (list.isEmpty()) return null;
        return list.get(0);
    }

    public void createDefaultPreferencesIfNotExists(String userId) throws Exception {
        UserPreferencesDto existing = fetchPreferencesForUser(userId);
        if (existing != null) return;
        Map<String, Object> payload = new HashMap<>();
        payload.put("user_id", userId);
        payload.put("dark_theme", true);
        payload.put("sound_enabled", true);
        payload.put("notifications_enabled", true);
        webClient.post().uri("/user_preferences")
                .bodyValue(Collections.singletonList(payload))
                .retrieve().bodyToMono(String.class).block();
    }

    public void updatePreferences(String userId, boolean darkTheme, boolean soundEnabled, boolean notificationsEnabled) throws Exception {
        Map<String, Object> update = new HashMap<>();
        update.put("dark_theme", darkTheme);
        update.put("sound_enabled", soundEnabled);
        update.put("notifications_enabled", notificationsEnabled);
        webClient.patch().uri(uriBuilder -> uriBuilder.path("/user_preferences").queryParam("user_id","eq."+userId).build())
                .header("Prefer","return=minimal")
                .bodyValue(update)
                .retrieve().bodyToMono(String.class).block();
    }

    // ========== HEALTH CHECK ==========
    public boolean isHealthy() {
        try {
            String body = webClient.get().uri("/profiles?select=id&limit=1")
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            return body != null;
        } catch (Exception ex) {
            return false;
        }
    }

    // ========== UTILITY ==========
    public static String firebaseUidToSupabaseId(String firebaseUid) {
        UUID namespace = UUID.fromString("6ba7b810-9dad-11d1-80b4-00c04fd430c8");
        byte[] nameBytes = (namespace.toString() + firebaseUid).getBytes(StandardCharsets.UTF_8);
        UUID uuid = UUID.nameUUIDFromBytes(nameBytes);
        return uuid.toString();
    }
}


