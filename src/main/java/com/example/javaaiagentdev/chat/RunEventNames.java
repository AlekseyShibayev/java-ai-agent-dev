package com.example.javaaiagentdev.chat;

/** Named SSE events exposed to the browser. */
public final class RunEventNames {

    private RunEventNames() {
    }

    public static String of(RunEvent event) {
        return switch (event) {
            case TextDeltaEvent ignored -> "text";
            case TextEndedEvent ignored -> "text";
            case ReasoningDeltaEvent ignored -> "reasoning";
            case ReasoningEndedEvent ignored -> "reasoning";
            case ToolStartedEvent ignored -> "tool";
            case ToolCalledEvent ignored -> "tool";
            case ToolFailedEvent ignored -> "tool";
            case UsageUpdatedEvent ignored -> "usage";
            case StepEndedEvent ignored -> "usage";
            case PermissionAskedEvent ignored -> "permission";
            case PermissionRepliedEvent ignored -> "permission";
            case ExecutionStartedEvent ignored -> "state";
            case ExecutionSucceededEvent ignored -> "state";
            case ExecutionInterruptedEvent ignored -> "state";
        };
    }
}
