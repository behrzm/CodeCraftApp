package com.codecraft.backend.controller;

import com.codecraft.backend.dto.LevelProgressDto;
import com.codecraft.backend.service.FirebaseService;
import com.codecraft.backend.service.SupabaseService;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/progress")
public class ProgressController {

    private final FirebaseService firebaseService;
    private final SupabaseService supabaseService;

    public ProgressController(FirebaseService firebaseService, SupabaseService supabaseService) {
        this.firebaseService = firebaseService;
        this.supabaseService = supabaseService;
    }

    @GetMapping
    public ResponseEntity<?> getProgress(@RequestHeader("Authorization") String authorization) {
        try {
            String token = extractToken(authorization);
            FirebaseToken decoded = firebaseService.verifyIdToken(token);
            String supabaseId = SupabaseService.firebaseUidToSupabaseId(decoded.getUid());
            List<LevelProgressDto> progress = supabaseService.fetchProgressForUser(supabaseId);
            return ResponseEntity.ok(progress);
        } catch (FirebaseAuthException e) {
            return ResponseEntity.status(401).body(Map.of("error", "invalid_token"));
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", ex.getMessage()));
        }
    }

    @GetMapping("/level/{levelId}")
    public ResponseEntity<?> getLevelProgress(
            @PathVariable long levelId,
            @RequestHeader("Authorization") String authorization) {
        try {
            String token = extractToken(authorization);
            FirebaseToken decoded = firebaseService.verifyIdToken(token);
            String supabaseId = SupabaseService.firebaseUidToSupabaseId(decoded.getUid());
            LevelProgressDto progress = supabaseService.fetchProgressForLevel(supabaseId, levelId);
            if (progress == null) {
                return ResponseEntity.ok(Map.of("completed", false, "stars", 0, "attempts", 0));
            }
            return ResponseEntity.ok(progress);
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

