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
            case ToolStartedEvent ignored -> "tool.started";
            case ToolCalledEvent ignored -> "tool.called";
            case ToolResultEvent ignored -> "tool.result";
            case ToolFailedEvent ignored -> "tool.failed";
            case UsageUpdatedEvent ignored -> "usage.session";
            case StepEndedEvent ignored -> "usage.step";
            case PermissionAskedEvent ignored -> "permission.asked";
            case PermissionRepliedEvent ignored -> "permission.replied";
            case ExecutionStartedEvent ignored -> "state";
            case ExecutionSucceededEvent ignored -> "state";
            case ExecutionInterruptedEvent ignored -> "state";
        };
    }
}
