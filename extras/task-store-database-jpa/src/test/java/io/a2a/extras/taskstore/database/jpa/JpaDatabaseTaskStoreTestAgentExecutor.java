package io.a2a.extras.taskstore.database.jpa;

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
 * Simple test AgentExecutor that responds to messages and uses AgentEmitter.addArtifact()
 * to trigger TaskUpdateEvents for our integration test.
 */
@IfBuildProfile("test")
@ApplicationScoped
public class JpaDatabaseTaskStoreTestAgentExecutor {

    @Produces
    public AgentExecutor agentExecutor() {
        return new AgentExecutor() {
            @Override
            public void execute(RequestContext context, AgentEmitter agentEmitter) throws A2AError {
                System.out.println("TestAgentExecutor.execute() called for task: " + context.getTaskId());
                System.out.println("Message " + context.getMessage());
                String lastText = getLastTextPart(context.getMessage());
                switch (lastText) {
                    case "create":
                        agentEmitter.submit();
                        break;
                    case "add-artifact":
                        agentEmitter.addArtifact(List.of(new TextPart(lastText)), "art-1", "test", null);
                        break;
                    default:
                        throw new InvalidRequestError(lastText + " is unknown");
                }
            }

            @Override
            public void cancel(RequestContext context, AgentEmitter agentEmitter) throws A2AError {
                agentEmitter.cancel();
            }
        };
    }

    private String getLastTextPart(Message message) throws A2AError {
        Part<?> part = message.parts().get(message.parts().size() - 1);
        if (part instanceof TextPart) {
            return ((TextPart) part).text();
        }
        throw new InvalidRequestError("No parts");
    }
}
