package com.collabsync.backend.controller;

import com.collabsync.backend.dto.ai.AiDescriptionRequest;
import com.collabsync.backend.dto.ai.AiPriorityRequest;
import com.collabsync.backend.service.AiService;
import com.collabsync.backend.util.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiService aiService;
    private final JwtUtil jwtUtil;

    @PostMapping("/generate-description")
    public ResponseEntity<Map<String, String>> generateDescription(
            @Valid @RequestBody AiDescriptionRequest request,
            @RequestHeader("Authorization") String auth) {
        String result = aiService.generateDescription(request.getTitle(), request.getProjectName());
        return ResponseEntity.ok(Map.of("description", result));
    }

    @PostMapping("/suggest-priority")
    public ResponseEntity<Map<String, String>> suggestPriority(
            @Valid @RequestBody AiPriorityRequest request,
            @RequestHeader("Authorization") String auth) {
        String result = aiService.suggestPriority(request.getTitle(), request.getDescription());
        return ResponseEntity.ok(Map.of("suggestion", result));
    }

    @PostMapping("/weekly-summary/{projectId}")
    public ResponseEntity<Map<String, String>> weeklySummary(
            @PathVariable Long projectId,
            @RequestHeader("Authorization") String auth) {
        String result = aiService.weeklySummary(projectId);
        return ResponseEntity.ok(Map.of("summary", result));
    }
}