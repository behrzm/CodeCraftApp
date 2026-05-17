package com.codecraft.backend.controller;

import com.codecraft.backend.dto.LevelDto;
import com.codecraft.backend.service.SupabaseService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/levels")
public class LevelsController {

    private final SupabaseService supabaseService;

    public LevelsController(SupabaseService supabaseService) {
        this.supabaseService = supabaseService;
    }

    @GetMapping
    public ResponseEntity<?> getLevels(
            @RequestParam(name = "language", required = false) String language,
            @RequestParam(name = "track", required = false) String track) {
        try {
            List<LevelDto> levels = supabaseService.fetchLevels(language, track);
            return ResponseEntity.ok(levels);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(java.util.Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getLevelById(@PathVariable long id) {
        try {
            LevelDto level = supabaseService.fetchLevelById(id);
            if (level == null) return ResponseEntity.notFound().build();
            return ResponseEntity.ok(level);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(java.util.Map.of("error", e.getMessage()));
        }
    }
}

