---
id: "000"
title: "Контракт OpenCode"
status: review
created: 2026-11-13
branch: task/000-opencode-contract
pr:
---

## Goal

Проверить API OpenCode: endpoints, body prompt, события, permissions, approval, usage, SSE.

## Non-goals

UI, MCP, RAG, production-код.

## Result

- Сессия: `POST /api/session` с `{agent, model:{id,providerID}, location:{directory}}`.
- Prompt: `POST /api/session/{id}/prompt` с `{text, agent}`. `parts` не принимается — `Missing key ["text"]`.
- `messageID` не дедуплицирует: повтор создаёт новое сообщение. Нужен свой `runId`.
- Сообщения: `GET /api/session/{id}/message?limit=N`; типы `user`, `assistant`, `idle`; tool — часть assistant `{type:"tool", state:{status,input,content}}`.
- События: SSE `/api/event`, только `data:` + `: heartbeat`; без `id:`/`event:`. Поток общий — фильтр по `data.sessionID`.
- Завершение: `session.execution.succeeded`; abort: `session.execution.interrupted` + `session.step.failed` (`aborted`).
- Usage: `session.step.ended.data.cost/tokens`; накопительно — `session.usage.updated`.
- Tools: `session.tool.called` даёт input; результат — в assistant-части из `/message`, отдельного `tool.success` для локального `read` нет.
- Permissions: только через agent config V2. `permission.asked`; ответ — `POST /api/session/{id}/permission/{requestId}/reply` с `{decision:"reject"}`.
- V1 `/session` не смонтирован: 405. Работаем только с `/api/*`.

## Acceptance criteria

- [x] Endpoint/body: `/prompt` + `text` (не `parts`).
- [x] `messageID` проверен: не идемпотентен.
- [x] Событие завершения: `session.execution.succeeded`.
- [x] Permission-имена и approval reply проверены.
- [x] usage/cost: `step.ended` + `usage.updated`.
- [x] SSE: `data:`, `: heartbeat`, фильтр по `sessionID`.
- [x] `session.create`: `location`.
- [x] Фикстуры: text, tool, approval, abort; без секретов.

## Verification

```bash
~/.opencode/bin/opencode api get /api/info
../java-ai-agent-dev_launcher/run.sh --opencode.spike=true
```

## Artifacts

`src/test/resources/opencode/{text,tool,approval,abort}.*.json`

## Constraints

- Не менять: `build.gradle`, существующие тесты, CI, `docker-compose.yml`.
- Секреты — только из launcher-конфига.

## Out of scope

Runtime, UI, MCP, RAG, reconnect.
