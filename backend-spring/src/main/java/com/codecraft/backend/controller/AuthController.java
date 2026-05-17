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
@RequestMapping("/api/auth")
public class AuthController {

    private final FirebaseService firebaseService;
    private final SupabaseService supabaseService;

    public AuthController(FirebaseService firebaseService, SupabaseService supabaseService) {
        this.firebaseService = firebaseService;
        this.supabaseService = supabaseService;
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verify(@RequestHeader("Authorization") String authorization) {
        try {
            String token = extractToken(authorization);
            FirebaseToken decoded = firebaseService.verifyIdToken(token);
            String firebaseUid = decoded.getUid();

            String supabaseId = SupabaseService.firebaseUidToSupabaseId(firebaseUid);
            // ensure profile exists
            supabaseService.createProfileIfNotExists(supabaseId, decoded.getName(), decoded.getEmail());
            ProfileDto profile = supabaseService.fetchProfileById(supabaseId);

            Map<String,Object> resp = new HashMap<>();
            resp.put("uid", firebaseUid);
            resp.put("supabaseId", supabaseId);
            resp.put("profile", profile);
            return ResponseEntity.ok(resp);
        } catch (FirebaseAuthException e) {
            return ResponseEntity.status(401).body(Map.of("error","invalid_token","message",e.getMessage()));
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error","server_error","message",ex.getMessage()));
        }
    }

    private String extractToken(String authHeader) {
        if (authHeader == null) return null;
        if (authHeader.startsWith("Bearer ")) return authHeader.substring(7);
        return authHeader;
    }
}
