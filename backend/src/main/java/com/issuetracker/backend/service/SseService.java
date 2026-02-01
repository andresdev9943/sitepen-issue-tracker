package com.issuetracker.backend.service;

import com.issuetracker.backend.dto.IssueDTO;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class SseService {

    // Store emitters by project ID
    private final Map<Long, CopyOnWriteArrayList<SseEmitter>> projectEmitters = new ConcurrentHashMap<>();

    // Store emitters for all issues (not filtered by project)
    private final CopyOnWriteArrayList<SseEmitter> globalEmitters = new CopyOnWriteArrayList<>();

    /**
     * Create a new SSE emitter for a specific project
     */
    public SseEmitter createEmitter(Long projectId) {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE); // No timeout
        
        if (projectId != null) {
            projectEmitters.computeIfAbsent(projectId, k -> new CopyOnWriteArrayList<>()).add(emitter);
            
            emitter.onCompletion(() -> removeEmitter(projectId, emitter));
            emitter.onTimeout(() -> removeEmitter(projectId, emitter));
            emitter.onError(e -> removeEmitter(projectId, emitter));
        } else {
            globalEmitters.add(emitter);
            
            emitter.onCompletion(() -> globalEmitters.remove(emitter));
            emitter.onTimeout(() -> globalEmitters.remove(emitter));
            emitter.onError(e -> globalEmitters.remove(emitter));
        }

        // Send initial connection message
        try {
            emitter.send(SseEmitter.event()
                .name("connected")
                .data("Connected to issue updates" + (projectId != null ? " for project " + projectId : "")));
        } catch (IOException e) {
            removeEmitter(projectId, emitter);
        }

        return emitter;
    }

    /**
     * Broadcast issue update to all subscribers of a project
     */
    public void broadcastIssueUpdate(IssueDTO issue, String eventType) {
        Long projectId = issue.getProjectId();
        
        // Send to project-specific subscribers
        CopyOnWriteArrayList<SseEmitter> emitters = projectEmitters.get(projectId);
        if (emitters != null) {
            sendToEmitters(emitters, issue, eventType);
        }

        // Send to global subscribers
        sendToEmitters(globalEmitters, issue, eventType);
    }

    /**
     * Send issue update to a list of emitters
     */
    private void sendToEmitters(CopyOnWriteArrayList<SseEmitter> emitters, IssueDTO issue, String eventType) {
        emitters.removeIf(emitter -> {
            try {
                emitter.send(SseEmitter.event()
                    .name(eventType)
                    .data(issue));
                return false;
            } catch (IOException e) {
                return true; // Remove dead emitters
            }
        });
    }

    /**
     * Remove an emitter from a project's subscriber list
     */
    private void removeEmitter(Long projectId, SseEmitter emitter) {
        if (projectId != null) {
            CopyOnWriteArrayList<SseEmitter> emitters = projectEmitters.get(projectId);
            if (emitters != null) {
                emitters.remove(emitter);
                if (emitters.isEmpty()) {
                    projectEmitters.remove(projectId);
                }
            }
        }
    }

    /**
     * Get the number of active connections
     */
    public int getActiveConnectionCount() {
        int count = globalEmitters.size();
        for (CopyOnWriteArrayList<SseEmitter> emitters : projectEmitters.values()) {
            count += emitters.size();
        }
        return count;
    }

    /**
     * Get the number of active connections for a specific project
     */
    public int getProjectConnectionCount(Long projectId) {
        CopyOnWriteArrayList<SseEmitter> emitters = projectEmitters.get(projectId);
        return emitters != null ? emitters.size() : 0;
    }
}
