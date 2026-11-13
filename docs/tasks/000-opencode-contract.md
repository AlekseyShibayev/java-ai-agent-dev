---
id: "000"
title: "Контракт OpenCode"
status: draft
created: 2026-11-13
branch: task/000-opencode-contract
pr:
---

## Goal

Проверить API OpenCode: endpoints, body prompt, события, permissions, approval, usage, SSE.

## Non-goals

UI, MCP, RAG, production-код.

## Acceptance criteria

- [ ] Endpoint/body: `/prompt` + `parts` или `/message` + `parts`.
- [ ] `messageID` работает как идемпотентный ключ.
- [ ] Событие завершения run определено: `session.idle`, `turn.idle` или `step.ended`.
- [ ] Permission-имена подтверждены; `edit` покрывает write/patch.
- [ ] Формат `permission.asked` и endpoint ответа.
- [ ] usage/cost: `step-finish`, `AssistantMessage` или оба.
- [ ] SSE: `id`, `event`, `data`, heartbeat, обрыв.
- [ ] `session.create`: `location` или `directory`.
- [ ] Фикстуры: text, tool call, usage, permission ask; без секретов.

## Verification

```bash
~/.opencode/bin/opencode api get /api/info
../java-ai-agent-dev_launcher/run.sh --opencode.spike=true
```

## Constraints

- Scope: `src/test/resources/opencode/*`, `opencode/OpenCodeSpike.java`. Выводы — в PR/коммите.
- Не менять: `build.gradle`, существующие тесты, CI, `docker-compose.yml`.
- Секреты — только из launcher-конфига.

## Out of scope

Runtime, UI, MCP, RAG, reconnect.
