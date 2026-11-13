---
id: "004"
title: "Карточки инструментов, reasoning, стоимость"
status: draft
created: 2026-11-13
branch: task/004-chat-tools
pr:
---

## Goal

Показать в чате шаги прогона: вызовы инструментов с input и результатом, свёрнутый reasoning, токены и стоимость.

## Non-goals

Approvals (005), RAG, MCP-настройка, отдельная страница trace.

## Acceptance criteria

- [ ] `session.tool.called` → карточка с именем и input, свёрнутая по умолчанию.
- [ ] Результат инструмента подтягивается из assistant-части `{type:"tool", state:{status, content}}`, а не из события.
- [ ] `session.tool.failed` → карточка с ошибкой, прогон при этом не помечается проваленным.
- [ ] Reasoning свёрнут по умолчанию и не перерисовывает весь блок на каждой дельте.
- [ ] Стоимость прогона и токены видны после завершения; формат вида `$0.000058`, не `0.00`.
- [ ] При abort ложная ошибка не показывается.

## Tests

Предлагаемые имена (human подтверждает):

- `ToolPartViewTest` — фикстура `tool.messages.json` → карточка с input и результатом.
- `UsageFormatTest` — `5.8e-05` → `$0.000058`, накопительный итог.
- `AbortViewTest` — `abort.events.json` не даёт статус «ошибка».
- `ReasoningViewTest` — reasoning свёрнут в разметке.

## Verification

```bash
GRADLE_USER_HOME=/tmp/opencode/gradle-home ./gradlew test
../java-ai-agent-dev_launcher/run.sh
```

Проверить в браузере: промпт с вызовом `read`, затем abort.

## Constraints

- `build.gradle` не менять.
- PR ≤ 300 строк.

## Out of scope

Timeline/waterfall, экспорт trace, MCP-инструменты, approvals.
