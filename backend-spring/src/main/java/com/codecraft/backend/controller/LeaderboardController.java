package com.codecraft.backend.controller;

import com.codecraft.backend.dto.ProfileDto;
import com.codecraft.backend.service.SupabaseService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/leaderboard")
public class LeaderboardController {

    private final SupabaseService supabaseService;

    public LeaderboardController(SupabaseService supabaseService) {
        this.supabaseService = supabaseService;
    }

    @GetMapping
    public ResponseEntity<?> top(@RequestParam(name = "limit", defaultValue = "25") int limit) {
        try {
            List<ProfileDto> top = supabaseService.fetchTopProfiles(limit);
            return ResponseEntity.ok(top);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }
}
