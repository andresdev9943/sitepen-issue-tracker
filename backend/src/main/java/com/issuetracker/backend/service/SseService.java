package com.issuetracker.backend.service;

import com.issuetracker.backend.dto.IssueDTO;
import com.issuetracker.backend.dto.ProjectDTO;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class SseService {

    // Store emitters by project ID (for issue updates within a project)
    private final Map<UUID, CopyOnWriteArrayList<SseEmitter>> projectEmitters = new ConcurrentHashMap<>();

    // Store emitters by user ID (for user-specific events like membership changes)
    private final Map<UUID, CopyOnWriteArrayList<SseEmitter>> userEmitters = new ConcurrentHashMap<>();

    // Store emitters for all issues (not filtered by project)
    private final CopyOnWriteArrayList<SseEmitter> globalEmitters = new CopyOnWriteArrayList<>();

    /**
     * Create a new SSE emitter for a specific project
     */
    public SseEmitter createEmitter(UUID projectId) {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE); // No timeout
        
        if (projectId != null) {
            projectEmitters.computeIfAbsent(projectId, k -> new CopyOnWriteArrayList<>()).add(emitter);
            
            emitter.onCompletion(() -> removeProjectEmitter(projectId, emitter));
            emitter.onTimeout(() -> removeProjectEmitter(projectId, emitter));
            emitter.onError(e -> removeProjectEmitter(projectId, emitter));
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
            removeProjectEmitter(projectId, emitter);
        }

        return emitter;
    }

    /**
     * Create a new SSE emitter for a specific user (for membership events)
     */
    public SseEmitter createUserEmitter(UUID userId) {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE); // No timeout
        
        userEmitters.computeIfAbsent(userId, k -> new CopyOnWriteArrayList<>()).add(emitter);
        
        emitter.onCompletion(() -> removeUserEmitter(userId, emitter));
        emitter.onTimeout(() -> removeUserEmitter(userId, emitter));
        emitter.onError(e -> removeUserEmitter(userId, emitter));

        // Send initial connection message
        try {
            emitter.send(SseEmitter.event()
                .name("connected")
                .data("Connected to user events for user " + userId));
        } catch (IOException e) {
            removeUserEmitter(userId, emitter);
        }

        return emitter;
    }

    /**
     * Broadcast issue update to all subscribers of a project
     */
    public void broadcastIssueUpdate(IssueDTO issue, String eventType) {
        UUID projectId = issue.getProjectId();
        
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
     * Broadcast project event to a specific user
     */
    public void broadcastProjectEventToUser(UUID userId, ProjectDTO project, String eventType) {
        CopyOnWriteArrayList<SseEmitter> emitters = userEmitters.get(userId);
        if (emitters != null) {
            sendToEmitters(emitters, project, eventType);
        }
    }

    /**
     * Broadcast project event to all members of a project
     */
    public void broadcastProjectEventToMembers(ProjectDTO project, String eventType, List<UUID> memberUserIds) {
        for (UUID userId : memberUserIds) {
            broadcastProjectEventToUser(userId, project, eventType);
        }
    }

    /**
     * Send project update to a list of emitters
     */
    private void sendToEmitters(CopyOnWriteArrayList<SseEmitter> emitters, Object data, String eventType) {
        emitters.removeIf(emitter -> {
            try {
                emitter.send(SseEmitter.event()
                    .name(eventType)
                    .data(data));
                return false;
            } catch (IOException e) {
                return true; // Remove dead emitters
            }
        });
    }

    /**
     * Remove an emitter from a project's subscriber list
     */
    private void removeProjectEmitter(UUID projectId, SseEmitter emitter) {
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
     * Remove an emitter from a user's subscriber list
     */
    private void removeUserEmitter(UUID userId, SseEmitter emitter) {
        if (userId != null) {
            CopyOnWriteArrayList<SseEmitter> emitters = userEmitters.get(userId);
            if (emitters != null) {
                emitters.remove(emitter);
                if (emitters.isEmpty()) {
                    userEmitters.remove(userId);
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
        for (CopyOnWriteArrayList<SseEmitter> emitters : userEmitters.values()) {
            count += emitters.size();
        }
        return count;
    }

    /**
     * Get the number of active connections for a specific project
     */
    public int getProjectConnectionCount(UUID projectId) {
        CopyOnWriteArrayList<SseEmitter> emitters = projectEmitters.get(projectId);
        return emitters != null ? emitters.size() : 0;
    }

    /**
     * Get the number of active connections for a specific user
     */
    public int getUserConnectionCount(UUID userId) {
        CopyOnWriteArrayList<SseEmitter> emitters = userEmitters.get(userId);
        return emitters != null ? emitters.size() : 0;
    }
}
