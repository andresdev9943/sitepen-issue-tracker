import { Injectable, NgZone } from '@angular/core';
import { Observable, Subject } from 'rxjs';
import { Issue } from '../models/issue.model';
import { Project } from '../models/project.model';

export interface SseEvent<T = any> {
  type: string;
  data: T;
}

@Injectable({
  providedIn: 'root',
})
export class SseService {
  private readonly API_URL = 'http://localhost:8080/api/sse';
  private eventSources: Map<string, EventSource> = new Map();

  constructor(private ngZone: NgZone) {}

  /**
   * Subscribe to issue updates for a specific project
   */
  subscribeToProjectIssues(projectId: string): Observable<SseEvent<Issue>> {
    const key = `project-${projectId}`;
    const url = `${this.API_URL}/issues?projectId=${projectId}`;
    return this.createEventSource(key, url);
  }

  /**
   * Subscribe to all issue updates (across all projects)
   */
  subscribeToAllIssues(): Observable<SseEvent<Issue>> {
    const key = 'all-issues';
    const url = `${this.API_URL}/issues`;
    return this.createEventSource(key, url);
  }

  /**
   * Subscribe to user-specific events (project membership changes, etc.)
   */
  subscribeToUserEvents(): Observable<SseEvent<Project>> {
    const key = 'user-events';
    const url = `${this.API_URL}/user`;
    return this.createEventSource(key, url);
  }

  /**
   * Create an EventSource and return an Observable
   */
  private createEventSource<T>(key: string, url: string): Observable<SseEvent<T>> {
    // Close existing connection if any
    this.closeConnection(key);

    const subject = new Subject<SseEvent<T>>();

    // Get auth token
    const token = localStorage.getItem('auth_token');
    if (!token) {
      subject.error(new Error('No authentication token found'));
      return subject.asObservable();
    }

    // Note: EventSource doesn't support custom headers directly
    // We need to pass the token as a query parameter or use a different approach
    const urlWithToken = `${url}${url.includes('?') ? '&' : '?'}token=${encodeURIComponent(token)}`;

    this.ngZone.runOutsideAngular(() => {
      const eventSource = new EventSource(urlWithToken);

      eventSource.onopen = () => {
        this.ngZone.run(() => {
          console.log(`SSE connection opened: ${key}`);
        });
      };

      // Listen for all event types
      const eventTypes = [
        'connected',
        'issue.created',
        'issue.updated',
        'issue.deleted',
        'issue.status.changed',
        'issue.priority.changed',
        'issue.assigned',
        'project.member.added',
        'project.member.removed',
        'project.updated',
        'project.deleted',
      ];

      eventTypes.forEach((eventType) => {
        eventSource.addEventListener(eventType, (event: MessageEvent) => {
          this.ngZone.run(() => {
            try {
              const data = eventType === 'connected' ? event.data : JSON.parse(event.data);
              subject.next({ type: eventType, data });
            } catch (error) {
              console.error(`Error parsing SSE event (${eventType}):`, error);
            }
          });
        });
      });

      eventSource.onerror = (error) => {
        this.ngZone.run(() => {
          console.error(`SSE connection error: ${key}`, error);
          subject.error(error);
          this.closeConnection(key);
        });
      };

      this.eventSources.set(key, eventSource);
    });

    return subject.asObservable();
  }

  /**
   * Close a specific SSE connection
   */
  closeConnection(key: string): void {
    const eventSource = this.eventSources.get(key);
    if (eventSource) {
      eventSource.close();
      this.eventSources.delete(key);
      console.log(`SSE connection closed: ${key}`);
    }
  }

  /**
   * Close all SSE connections
   */
  closeAllConnections(): void {
    this.eventSources.forEach((eventSource, key) => {
      eventSource.close();
      console.log(`SSE connection closed: ${key}`);
    });
    this.eventSources.clear();
  }

  /**
   * Get the number of active connections
   */
  getActiveConnectionCount(): number {
    return this.eventSources.size;
  }
}
