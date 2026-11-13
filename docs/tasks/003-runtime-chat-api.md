---
id: "003"
title: "Рантайм чата: API"
status: in-progress
created: 2026-11-13
branch: task/003-runtime-chat-api
pr:
openspec: 003-runtime-chat-api
---

## Change

Поведение и план — `openspec/changes/003-runtime-chat-api/` (`proposal`, `specs/chat-runtime`, `design`, `tasks`). Этот файл держит приёмку: AC — что принимаем, спека — как система ведёт себя.

## Goal

Серверный рантайм чата над OpenCode: свой `runId`, чтение SSE `/api/event`, дедуп, fan-out подписчикам, abort, ответ на permission, нормализованные DTO. Без HTML.

## Non-goals

Thymeleaf, HTMX, JS, вёрстка. Сохранение истории в БД.

## Acceptance criteria

- [ ] `POST /api/chat/{sessionId}/messages` отправляет `{text, agent}` и возвращает `runId`.
- [ ] События из общего `/api/event` фильтруются по `data.sessionID`; чужой `sessionID` не публикуется.
- [ ] Дедуп по `(sessionID, durable.seq)`: повтор не публикуется дважды.
- [ ] `: heartbeat` не парсится как событие.
- [ ] `POST /api/chat/{sessionId}/abort` → `session.execution.interrupted`, run закрывается как `interrupted`.
- [ ] `session.tool.failed` не помечает прогон как проваленный.
- [ ] `POST /api/chat/{sessionId}/permission/{requestId}` принимает `allow`/`reject`.
- [ ] DTO покрывают: text, reasoning, tool (input + результат из `content[type=tool]`), usage, execution, permission.
- [ ] Недоступный OpenCode → 502 с понятным телом; URL резолвится заново, а не кэшируется навсегда.

## Tests

Предлагаемые имена (human подтверждает):

- `OpenCodeEventParserTest` — разбор `text`, `tool`, `approval`, `abort` фикстур.
- `RunRegistryTest` — дедуп по `durable.seq`, закрытие run по `succeeded`/`interrupted`.
- `ChatControllerTest` — MockMvc: prompt → `runId`, abort, permission, 502 при недоступном сервисе.
- `OpenCodeClientAbortTest` — abort и permission reply уходят в правильные endpoint'ы.

## Verification

```bash
GRADLE_USER_HOME=/tmp/opencode/gradle-home ./gradlew test
../java-ai-agent-dev_launcher/run.sh
```

## Constraints

- `build.gradle` не менять.
- Чтение SSE — отдельный поток; при необходимости `spring.threads.virtual.enabled: true` в `application.yaml`.
- PR ≤ 300 строк.

## Out of scope

UI, БД-схема и история, переподключение после рестарта приложения с восстановлением частичного ответа, MCP.
