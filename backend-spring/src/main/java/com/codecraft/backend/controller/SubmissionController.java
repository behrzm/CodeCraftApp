package com.codecraft.backend.controller;

import com.codecraft.backend.dto.LevelDto;
import com.codecraft.backend.dto.ProfileDto;
import com.codecraft.backend.service.FirebaseService;
import com.codecraft.backend.service.SupabaseService;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/levels")
public class SubmissionController {

    private final FirebaseService firebaseService;
    private final SupabaseService supabaseService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public SubmissionController(FirebaseService firebaseService, SupabaseService supabaseService) {
        this.firebaseService = firebaseService;
        this.supabaseService = supabaseService;
    }

    @PostMapping("/{id}/submit")
    public ResponseEntity<?> submitSolution(
            @PathVariable long id,
            @RequestHeader("Authorization") String authorization,
            @RequestBody Map<String, Object> body) {
        try {
            String token = extractToken(authorization);
            FirebaseToken decoded = firebaseService.verifyIdToken(token);
            String supabaseId = SupabaseService.firebaseUidToSupabaseId(decoded.getUid());

            // Get level details
            LevelDto level = supabaseService.fetchLevelById(id);
            if (level == null) {
                return ResponseEntity.notFound().build();
            }

            // Get user profile for HP tracking
            ProfileDto profile = supabaseService.fetchProfileById(supabaseId);
            if (profile == null) {
                return ResponseEntity.status(404).body(Map.of("error", "Profile not found"));
            }

            // Extract code from request
            String userCode = (String) body.getOrDefault("code", "");

            // SIMPLIFIED SOLUTION CHECK: Compare with expected solution
            // In production, you'd run actual test cases
            boolean isCorrect = checkSolution(userCode, level);

            int xpEarned = 0;
            int hpUsed = 0;

            if (isCorrect) {
                // Correct solution: +50 XP
                xpEarned = level.xpReward > 0 ? level.xpReward : 50;
                hpUsed = 0;
                supabaseService.addXpForUser(supabaseId, xpEarned, "level_completed");
                supabaseService.createOrUpdateLevelProgress(supabaseId, id, level.language, level.track,
                        3, true); // 3 stars, completed
            } else {
                // Wrong solution: 0 XP, -1 HP (max 3)
                xpEarned = 0;
                hpUsed = 1;
                // Update profile: decrease HP equivalent (we'll use a simple counter)
                // For now, just record in xp_history as negative
                supabaseService.addXpForUser(supabaseId, 0, "wrong_submission");
                supabaseService.createOrUpdateLevelProgress(supabaseId, id, level.language, level.track,
                        0, false); // 0 stars, not completed
            }

            Map<String, Object> response = new HashMap<>();
            response.put("correct", isCorrect);
            response.put("xp_earned", xpEarned);
            response.put("hp_used", hpUsed);
            response.put("message", isCorrect ? "Correct! Well done!" : "Incorrect. Try again!");

            return ResponseEntity.ok(response);
        } catch (FirebaseAuthException e) {
            return ResponseEntity.status(401).body(Map.of("error", "invalid_token"));
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", ex.getMessage()));
        }
    }

    // Simple solution checker - can be made more sophisticated
    private boolean checkSolution(String userCode, LevelDto level) {
        // Normalize code (remove whitespace, comments for basic check)
        String normalized = userCode.replaceAll("\\s+", "").toLowerCase();
        String solutionNormalized = level.solution.replaceAll("\\s+", "").toLowerCase();

        // Exact match check
        if (normalized.equals(solutionNormalized)) {
            return true;
        }

        // If test_cases exist, could validate against them
        // For now, we'll accept if user code is close enough (contains key parts)
        return normalized.contains(solutionNormalized.substring(0, Math.min(20, solutionNormalized.length())));
    }

    private String extractToken(String authHeader) {
        if (authHeader == null) return null;
        if (authHeader.startsWith("Bearer ")) return authHeader.substring(7);
        return authHeader;
    }
}

