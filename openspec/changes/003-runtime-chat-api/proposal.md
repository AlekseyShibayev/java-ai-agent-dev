## Why

Каркас умеет отправить промпт и дождаться финального текста, но не умеет вести живой прогон: показывать шаги, отменять, отвечать на permission. Без этого нет ни UI (004), ни инструментов.

## What Changes

- Свой `runId` на прогон и реестр прогонов с дедупом событий.
- Чтение общего SSE `/api/event` и fan-out подписчикам по `sessionID`.
- Abort и ответ на permission.
- Нормализованные DTO: text, reasoning, tool, usage, execution, permission.

## Capabilities

### New Capabilities

- `chat-runtime`: серверный рантайм прогона над OpenCode API — запуск, события, отмена, разрешения.

### Modified Capabilities

## Impact

- Задача: `docs/tasks/003-runtime-chat-api.md` — приёмка и статус.
- `src/main/java/com/example/javaaiagentdev/chat/**` — новый пакет.
- `opencode/` — клиент дополняется abort и reply.
