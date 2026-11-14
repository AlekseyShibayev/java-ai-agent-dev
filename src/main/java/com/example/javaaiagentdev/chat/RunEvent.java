package com.example.javaaiagentdev.chat;

/** Normalized event of a chat run. */
public sealed interface RunEvent permits
        TextDeltaEvent,
        TextEndedEvent,
        ReasoningDeltaEvent,
        ReasoningEndedEvent,
        ToolStartedEvent,
        ToolCalledEvent,
        ToolResultEvent,
        ToolFailedEvent,
        UsageUpdatedEvent,
        StepEndedEvent,
        ExecutionStartedEvent,
        ExecutionSucceededEvent,
        ExecutionInterruptedEvent,
        PermissionAskedEvent,
        PermissionRepliedEvent {

    String sessionId();
}
