package com.codecraft.backend.controller;

import com.codecraft.backend.dto.ProfileDto;
import com.codecraft.backend.service.FirebaseService;
import com.codecraft.backend.service.SupabaseService;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private final FirebaseService firebaseService;
    private final SupabaseService supabaseService;

    public ProfileController(FirebaseService firebaseService, SupabaseService supabaseService) {
        this.firebaseService = firebaseService;
        this.supabaseService = supabaseService;
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(@RequestHeader("Authorization") String authorization) {
        try {
            String token = extractToken(authorization);
            FirebaseToken decoded = firebaseService.verifyIdToken(token);
            String supabaseId = SupabaseService.firebaseUidToSupabaseId(decoded.getUid());
            ProfileDto profile = supabaseService.fetchProfileById(supabaseId);
            if (profile == null) return ResponseEntity.notFound().build();
            return ResponseEntity.ok(profile);
        } catch (FirebaseAuthException e) {
            return ResponseEntity.status(401).body(Map.of("error", "invalid_token"));
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", ex.getMessage()));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerOrCreateProfile(@RequestHeader("Authorization") String authorization,
                                                      @RequestBody(required = false) Map<String, Object> body) {
        try {
            String token = extractToken(authorization);
            FirebaseToken decoded = firebaseService.verifyIdToken(token);
            String firebaseUid = decoded.getUid();
            String supabaseId = SupabaseService.firebaseUidToSupabaseId(firebaseUid);

            String displayName = body != null ? (String) body.getOrDefault("display_name", decoded.getName()) : decoded.getName();
            if (displayName == null || displayName.isEmpty()) {
                displayName = "Coder";
            }

            // Create profile and preferences
            supabaseService.createProfileIfNotExists(supabaseId, displayName, decoded.getEmail());
            supabaseService.createDefaultPreferencesIfNotExists(supabaseId);

            ProfileDto profile = supabaseService.fetchProfileById(supabaseId);
            return ResponseEntity.ok(Map.of(
                    "uid", firebaseUid,
                    "supabaseId", supabaseId,
                    "profile", profile,
                    "created", true
            ));
        } catch (FirebaseAuthException e) {
            return ResponseEntity.status(401).body(Map.of("error", "invalid_token"));
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", ex.getMessage()));
        }
    }

    @PostMapping("/me/xp")
    public ResponseEntity<?> addXp(@RequestHeader("Authorization") String authorization,
                                   @RequestBody Map<String, Object> body) {
        try {
            String token = extractToken(authorization);
            FirebaseToken decoded = firebaseService.verifyIdToken(token);
            String supabaseId = SupabaseService.firebaseUidToSupabaseId(decoded.getUid());
            int amount = ((Number) body.getOrDefault("amount", 0)).intValue();
            String reason = (String) body.getOrDefault("reason", "app_reward");
            supabaseService.addXpForUser(supabaseId, amount, reason);
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
