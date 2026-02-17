package io.a2a.extras.queuemanager.replicated.tests;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

import io.a2a.server.agentexecution.AgentExecutor;
import io.a2a.server.agentexecution.RequestContext;
import io.a2a.server.tasks.AgentEmitter;
import io.a2a.spec.A2AError;
import io.a2a.spec.InvalidRequestError;
import io.a2a.spec.Message;
import io.a2a.spec.Part;
import io.a2a.spec.TextPart;
import io.quarkus.arc.profile.IfBuildProfile;

/**
 * Test AgentExecutor for replicated queue manager integration testing.
 * Handles different message types to trigger various events that should be replicated.
 */
@IfBuildProfile("test")
@ApplicationScoped
public class ReplicationTestAgentExecutor {

    @Produces
    public AgentExecutor agentExecutor() {
        return new AgentExecutor() {
            @Override
            public void execute(RequestContext context, AgentEmitter agentEmitter) throws A2AError {
                String lastText = getLastTextPart(context.getMessage());

                switch (lastText) {
                    case "create":
                        // Submit task - this should trigger TaskStatusUpdateEvent
                        agentEmitter.submit();
                        break;
                    case "working":
                        // Move task to WORKING state without completing - keeps queue alive
                        agentEmitter.submit();
                        agentEmitter.startWork();
                        break;
                    case "complete":
                        // Complete the task - should trigger poison pill generation
                        agentEmitter.submit();
                        agentEmitter.startWork();
                        agentEmitter.addArtifact(List.of(new TextPart("Task completed")));
                        agentEmitter.complete();
                        break;
                    default:
                        throw new InvalidRequestError("Unknown command: " + lastText);
                }
            }

            @Override
            public void cancel(RequestContext context, AgentEmitter agentEmitter) throws A2AError {
                agentEmitter.cancel();
            }
        };
    }

    private String getLastTextPart(Message message) throws A2AError {
        if (message.parts().isEmpty()) {
            throw new InvalidRequestError("No parts in message");
        }
        Part<?> part = message.parts().get(message.parts().size() - 1);
        if (part instanceof TextPart) {
            return ((TextPart) part).text();
        }
        throw new InvalidRequestError("Last part is not text");
    }
}