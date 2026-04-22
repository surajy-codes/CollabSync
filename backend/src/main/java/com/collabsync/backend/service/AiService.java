package com.collabsync.backend.service;

import com.collabsync.backend.entity.Task;
import com.collabsync.backend.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AiService {

    private final WebClient.Builder webClientBuilder;
    private final TaskRepository taskRepository;

    @Value("${app.gemini.api-key}")
    private String apiKey;

    @Value("${app.gemini.url}")
    private String apiUrl;

    public String generateDescription(String title, String projectName) {
        String prompt = String.format("""
                You are a project management assistant.
                Generate a clear task description for the following task:
                Project: %s
                Task Title: %s
                
                Include:
                - Objective (1-2 sentences)
                - Acceptance Criteria (3-4 bullet points)
                - Notes (any technical considerations)
                
                Keep it concise and professional.
                """, projectName, title);

        return callGemini(prompt);
    }

    public String suggestPriority(String title, String description) {
        String prompt = String.format("""
                You are a project management assistant.
                Based on the following task, suggest a priority level.
                
                Task Title: %s
                Task Description: %s
                
                Reply with ONLY this format:
                Priority: <LOW|MEDIUM|HIGH|CRITICAL>
                Reason: <one sentence explanation>
                """, title, description);

        return callGemini(prompt);
    }

    public String weeklySummary(Long projectId) {
        LocalDateTime oneWeekAgo = LocalDateTime.now().minusDays(7);

        List<Task> recentTasks = taskRepository.findAll().stream()
                .filter(t -> t.getProject().getId().equals(projectId))
                .filter(t -> t.getUpdatedAt().isAfter(oneWeekAgo))
                .collect(Collectors.toList());

        if (recentTasks.isEmpty()) {
            return "No tasks were updated in the past 7 days for this project.";
        }

        String taskList = recentTasks.stream()
                .map(t -> String.format("- [%s] %s (Priority: %s)",
                        t.getStatus(), t.getTitle(), t.getPriority()))
                .collect(Collectors.joining("\n"));

        String prompt = String.format("""
                You are a project management assistant.
                Write a brief weekly progress summary (2-3 paragraphs) for a software project
                based on the following tasks updated in the past 7 days:
                
                %s
                
                Keep it professional and highlight key progress and any blockers.
                """, taskList);

        return callGemini(prompt);
    }

    private String callGemini(String prompt) {
        String url = apiUrl + "?key=" + apiKey;

        Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                        Map.of("parts", List.of(
                                Map.of("text", prompt)
                        ))
                )
        );

        Map response = webClientBuilder.build()
                .post()
                .uri(url)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        try {
            var candidates = (List<?>) response.get("candidates");
            var first = (Map<?, ?>) candidates.get(0);
            var content = (Map<?, ?>) first.get("content");
            var parts = (List<?>) content.get("parts");
            var part = (Map<?, ?>) parts.get(0);
            return part.get("text").toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse Gemini response: " + e.getMessage());
        }
    }
}