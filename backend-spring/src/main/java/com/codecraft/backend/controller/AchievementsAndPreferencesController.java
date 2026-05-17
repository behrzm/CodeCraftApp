package com.codecraft.backend.controller;

import com.codecraft.backend.dto.AchievementDto;
import com.codecraft.backend.dto.UserPreferencesDto;
import com.codecraft.backend.service.FirebaseService;
import com.codecraft.backend.service.SupabaseService;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class AchievementsAndPreferencesController {

    private final FirebaseService firebaseService;
    private final SupabaseService supabaseService;

    public AchievementsAndPreferencesController(FirebaseService firebaseService, SupabaseService supabaseService) {
        this.firebaseService = firebaseService;
        this.supabaseService = supabaseService;
    }

    // ========== ACHIEVEMENTS ==========
    @GetMapping("/achievements")
    public ResponseEntity<?> getAchievements(@RequestHeader("Authorization") String authorization) {
        try {
            String token = extractToken(authorization);
            FirebaseToken decoded = firebaseService.verifyIdToken(token);
            String supabaseId = SupabaseService.firebaseUidToSupabaseId(decoded.getUid());
            List<AchievementDto> achievements = supabaseService.fetchAchievementsForUser(supabaseId);
            return ResponseEntity.ok(achievements);
        } catch (FirebaseAuthException e) {
            return ResponseEntity.status(401).body(Map.of("error", "invalid_token"));
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", ex.getMessage()));
        }
    }

    // ========== PREFERENCES ==========
    @GetMapping("/preferences")
    public ResponseEntity<?> getPreferences(@RequestHeader("Authorization") String authorization) {
        try {
            String token = extractToken(authorization);
            FirebaseToken decoded = firebaseService.verifyIdToken(token);
            String supabaseId = SupabaseService.firebaseUidToSupabaseId(decoded.getUid());
            
            // Create default if not exists
            supabaseService.createDefaultPreferencesIfNotExists(supabaseId);
            
            UserPreferencesDto prefs = supabaseService.fetchPreferencesForUser(supabaseId);
            if (prefs == null) {
                return ResponseEntity.status(500).body(Map.of("error", "Could not create preferences"));
            }
            return ResponseEntity.ok(prefs);
        } catch (FirebaseAuthException e) {
            return ResponseEntity.status(401).body(Map.of("error", "invalid_token"));
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", ex.getMessage()));
        }
    }

    @PutMapping("/preferences")
    public ResponseEntity<?> updatePreferences(
            @RequestHeader("Authorization") String authorization,
            @RequestBody Map<String, Object> body) {
        try {
            String token = extractToken(authorization);
            FirebaseToken decoded = firebaseService.verifyIdToken(token);
            String supabaseId = SupabaseService.firebaseUidToSupabaseId(decoded.getUid());
            
            boolean darkTheme = (boolean) body.getOrDefault("dark_theme", true);
            boolean soundEnabled = (boolean) body.getOrDefault("sound_enabled", true);
            boolean notificationsEnabled = (boolean) body.getOrDefault("notifications_enabled", true);
            
            supabaseService.updatePreferences(supabaseId, darkTheme, soundEnabled, notificationsEnabled);
            return ResponseEntity.ok(Map.of("ok", true));
        } catch (FirebaseAuthException e) {
            return ResponseEntity.status(401).body(Map.of("error", "invalid_token"));
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", ex.getMessage()));
        }
    }

    private String extractToken(String authHeader) {
        if (authHeader == null) return null;
        if (authHeader.startsWith("Bearer ")) return authHeader.substring(7);
        return authHeader;
    }
}

