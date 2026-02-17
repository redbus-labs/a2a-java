package io.a2a.extras.pushnotificationconfigstore.database.jpa;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Alternative;

import io.a2a.server.tasks.PushNotificationSender;
import io.a2a.spec.StreamingEventKind;
import io.a2a.spec.Task;

/**
 * Mock implementation of PushNotificationSender for integration testing.
 * Captures notifications in a thread-safe queue for test verification.
 */
@ApplicationScoped
@Alternative
@Priority(100)
public class MockPushNotificationSender implements PushNotificationSender {

    private final Queue<StreamingEventKind> capturedEvents = new ConcurrentLinkedQueue<>();

    @Override
    public void sendNotification(StreamingEventKind event) {
        capturedEvents.add(event);
    }

    public Queue<StreamingEventKind> getCapturedEvents() {
        return capturedEvents;
    }

    /**
     * For backward compatibility - provides access to Task events only.
     */
    public Queue<Task> getCapturedTasks() {
        Queue<Task> tasks = new ConcurrentLinkedQueue<>();
        capturedEvents.stream()
            .filter(e -> e instanceof Task)
            .map(e -> (Task) e)
            .forEach(tasks::add);
        return tasks;
    }

    public void clear() {
        capturedEvents.clear();
    }
}
