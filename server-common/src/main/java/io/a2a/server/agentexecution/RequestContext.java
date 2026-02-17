package io.a2a.server.agentexecution;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import io.a2a.server.ServerCallContext;
import io.a2a.server.util.IdGenerator;
import io.a2a.server.util.UUIDIdGenerator;
import io.a2a.spec.InvalidParamsError;
import io.a2a.spec.Message;
import io.a2a.spec.MessageSendConfiguration;
import io.a2a.spec.MessageSendParams;
import io.a2a.spec.Part;
import io.a2a.spec.Task;
import io.a2a.spec.TextPart;
import org.jspecify.annotations.Nullable;

/**
 * Container for request parameters and task state provided to {@link AgentExecutor}.
 * <p>
 * This class encapsulates all the information an agent needs to process a request:
 * the user's message, existing task state (for continuing conversations), configuration,
 * and server call context. It's the primary way agents access request data.
 * </p>
 *
 * <h2>Key Components</h2>
 * <ul>
 *   <li><b>Message:</b> The user's input message with parts (text, images, etc.)</li>
 *   <li><b>Task:</b> Existing task state for continuing conversations (null for new conversations)</li>
 *   <li><b>TaskId/ContextId:</b> Identifiers for the task and conversation (auto-generated if not provided)</li>
 *   <li><b>Configuration:</b> Request settings (blocking mode, push notifications, etc.)</li>
 *   <li><b>Related Tasks:</b> Other tasks in the same conversation context</li>
 * </ul>
 *
 * <h2>Common Usage Patterns</h2>
 * <pre>{@code
 * public void execute(RequestContext context, AgentEmitter emitter) {
 *     // Check if this is a new conversation or continuation
 *     Task existingTask = context.getTask();
 *     if (existingTask == null) {
 *         // New conversation - initialize
 *     } else {
 *         // Continuing conversation - access history
 *         List<Message> history = existingTask.history();
 *     }
 *
 *     // Extract user input
 *     String userMessage = context.getUserInput("\n");
 *
 *     // Access configuration if needed
 *     MessageSendConfiguration config = context.getConfiguration();
 *     boolean isBlocking = config != null && config.blocking();
 *
 *     // Process and respond...
 * }
 * }</pre>
 *
 * <h2>Text Extraction Helper</h2>
 * The {@link #getUserInput(String)} method is a convenient way to extract text from
 * message parts:
 * <pre>{@code
 * // Get all text parts joined with newlines
 * String text = context.getUserInput("\n");
 *
 * // Get all text parts joined with spaces
 * String text = context.getUserInput(" ");
 * }</pre>
 *
 * @see AgentExecutor
 * @see Message
 * @see Task
 * @see MessageSendConfiguration
 */
public class RequestContext {

    private final @Nullable MessageSendParams params;
    private final String taskId;
    private final String contextId;
    private final @Nullable Task task;
    private final List<Task> relatedTasks;
    private final @Nullable ServerCallContext callContext;

    /**
     * Constructor with all fields already validated and initialized.
     * <p>
     * <b>Note:</b> Use {@link Builder} instead of calling this constructor directly.
     * The builder handles ID generation and validation.
     * </p>
     *
     * @param params the message send parameters (can be null for cancel operations)
     * @param taskId the task identifier (must not be null)
     * @param contextId the context identifier (must not be null)
     * @param task the existing task state (null for new conversations)
     * @param relatedTasks other tasks in the same context (must not be null, can be empty)
     * @param callContext the server call context (can be null)
     */
    private RequestContext(
            @Nullable MessageSendParams params,
            String taskId,
            String contextId,
            @Nullable Task task,
            List<Task> relatedTasks,
            @Nullable ServerCallContext callContext) {
        this.params = params;
        this.taskId = taskId;
        this.contextId = contextId;
        this.task = task;
        this.relatedTasks = relatedTasks;
        this.callContext = callContext;
    }

    /**
     * Returns the task identifier.
     * <p>
     * This is auto-generated (UUID) by the builder if not provided by the client
     * in the message parameters. This value is never null.
     * </p>
     *
     * @return the task ID (never null)
     */
    public String getTaskId() {
        return taskId;
    }

    /**
     * Returns the conversation context identifier.
     * <p>
     * Conversation contexts group related tasks together (e.g., multiple tasks
     * in the same user session). This is auto-generated (UUID) by the builder if
     * not provided by the client in the message parameters. This value is never null.
     * </p>
     *
     * @return the context ID (never null)
     */
    public String getContextId() {
        return contextId;
    }

    /**
     * Returns the existing task state, if this is a continuation of a conversation.
     * <p>
     * For new conversations, this is null. For continuing conversations, contains
     * the full task state including history, artifacts, and status.
     * <p>
     * <b>Common Pattern:</b>
     * <pre>{@code
     * if (context.getTask() == null) {
     *     // New conversation - initialize state
     * } else {
     *     // Continuing - access previous messages
     *     List<Message> history = context.getTask().history();
     * }
     * }</pre>
     *
     * @return the existing task, or null if this is a new conversation
     */
    public @Nullable Task getTask() {
        return task;
    }

    /**
     * Returns other tasks in the same conversation context.
     * <p>
     * Useful for multi-task conversations where the agent needs to access
     * state from related tasks.
     * </p>
     *
     * @return unmodifiable list of related tasks (empty if none)
     */
    public List<Task> getRelatedTasks() {
        return Collections.unmodifiableList(relatedTasks);
    }

    /**
     * Returns the user's message.
     * <p>
     * Contains the message parts (text, images, etc.) sent by the client.
     * Use {@link #getUserInput(String)} for convenient text extraction.
     * </p>
     *
     * @return the message, or null if not available
     * @see #getUserInput(String)
     */
    public @Nullable Message getMessage() {
        return params != null ? params.message() : null;
    }

    /**
     * Returns the request configuration.
     * <p>
     * Contains settings like blocking mode, push notification config, etc.
     * </p>
     *
     * @return the configuration, or null if not provided
     */
    public @Nullable MessageSendConfiguration getConfiguration() {
        return params != null ? params.configuration() : null;
    }

    /**
     * Returns the server call context.
     * <p>
     * Contains transport-specific information like authentication, headers, etc.
     * Most agents don't need this.
     * </p>
     *
     * @return the call context, or null if not available
     */
    public @Nullable ServerCallContext getCallContext() {
        return callContext;
    }

    /**
     * Returns the tenant identifier from the request parameters.
     * <p>
     * The tenant is used in multi-tenant environments to identify which
     * customer or organization the request belongs to.
     * </p>
     *
     * @return the tenant identifier, or null if no params or tenant not set
     */
    public @Nullable String getTenant() {
        return params != null ? params.tenant() : null;
    }

    /**
     * Extracts all text content from the message and joins with the specified delimiter.
     * <p>
     * This is a convenience method for getting text input from messages that may contain
     * multiple text parts. Non-text parts (images, etc.) are ignored.
     * <p>
     * <b>Examples:</b>
     * <pre>{@code
     * // Join with newlines (common for multi-paragraph input)
     * String text = context.getUserInput("\n");
     *
     * // Join with spaces (common for single-line input)
     * String text = context.getUserInput(" ");
     *
     * // Default delimiter is newline
     * String text = context.getUserInput(null);  // uses "\n"
     * }</pre>
     *
     * @param delimiter the string to insert between text parts (null defaults to "\n")
     * @return all text parts joined with delimiter, or empty string if no message
     */
    public String getUserInput(@Nullable String delimiter) {
        Message message = getMessage();
        if (message == null) {
            return "";
        }
        
        List<Part> parts = message.parts();
        if (parts == null || parts.isEmpty()) {
            return "";
        }

        String delim = delimiter != null ? delimiter : "\n";
        
        return parts.stream()
                .filter(p -> p instanceof TextPart)
                .map(p -> ((TextPart) p).text())
                .collect(Collectors.joining(delim));
    }

    public static class Builder {
        private @Nullable MessageSendParams params;
        private @Nullable String taskId;
        private @Nullable String contextId;
        private @Nullable Task task;
        private List<Task> relatedTasks = new ArrayList<>();
        private @Nullable ServerCallContext callContext;
        private final IdGenerator idGenerator;

        public Builder() {
            this(new UUIDIdGenerator());
        }

        public Builder(IdGenerator idGenerator) {
            this.idGenerator = idGenerator;
        }

        public Builder withParams(MessageSendParams params) {
            this.params = params;
            return this;
        }

        public Builder withTaskId(String taskId) {
            this.taskId = taskId;
            return this;
        }

        public Builder withContextId(String contextId) {
            this.contextId = contextId;
            return this;
        }

        public Builder withTask(Task task) {
            this.task = task;
            return this;
        }

        public Builder withRelatedTasks(List<Task> relatedTasks) {
            this.relatedTasks = relatedTasks != null ? relatedTasks : new ArrayList<>();
            return this;
        }

        public Builder withCallContext(ServerCallContext callContext) {
            this.callContext = callContext;
            return this;
        }

        public RequestContext build() throws InvalidParamsError {
            String finalTaskId = this.taskId;
            String finalContextId = this.contextId;

            if (params != null) {
                // Validate or extract Task ID
                String paramTaskId = params.message().taskId();
                if (finalTaskId != null) {
                    if (paramTaskId != null && !finalTaskId.equals(paramTaskId)) {
                         throw new InvalidParamsError("bad task id");
                    }
                } else {
                    finalTaskId = paramTaskId;
                }

                // Validate or extract Context ID
                String paramContextId = params.message().contextId();
                if (finalContextId != null) {
                    if (paramContextId != null && !finalContextId.equals(paramContextId)) {
                        throw new InvalidParamsError("bad context id");
                    }
                } else {
                    finalContextId = paramContextId;
                }
            }

            if (finalTaskId == null) {
                finalTaskId = idGenerator.generate();
            }

            if (finalContextId == null) {
                finalContextId = idGenerator.generate();
            }

            return new RequestContext(
                params,
                finalTaskId,
                finalContextId,
                task,
                relatedTasks,
                callContext
            );
        }
    }
}
