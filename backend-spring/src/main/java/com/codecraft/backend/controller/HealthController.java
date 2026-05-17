package com.codecraft.backend.controller;

import com.codecraft.backend.service.SupabaseService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class HealthController {

    private final SupabaseService supabaseService;

    public HealthController(SupabaseService supabaseService) {
        this.supabaseService = supabaseService;
    }

    @GetMapping("/health")
    public ResponseEntity<?> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "ok");
        
        boolean supabaseHealthy = supabaseService.isHealthy();
        response.put("supabase", supabaseHealthy ? "connected" : "disconnected");
        
        if (!supabaseHealthy) {
            return ResponseEntity.status(503).body(response);
        }
        
        return ResponseEntity.ok(response);
    }
}

