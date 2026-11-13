# План

Статус: draft.

## Цель

Локальное AI Lab: чат с агентом, trace, инструменты, RAG, evals. Один Spring Boot-проект, без внешних каналов.

## Решения

| Что | Как |
| --- | --- |
| Рантайм | OpenCode первым, native loop потом |
| UI | Thymeleaf + HTMX + SSE |
| БД | PostgreSQL, read-only роль для агента |
| Поиск | Postgres FTS; `bge-m3` + pgvector только при разрыве |
| Локальная модель | Ollama `qwen2.5:3b-instruct` |
| Секреты | только launcher-конфиг |

## Этапы

1. **Чат + trace.** 000 (контракт OpenCode) → 001 (БД, написать после 000) → runtime → SSE/UI.
2. **Инструменты.** MCP, `wiki_search`, approval.
3. **Native loop.** Ollama, свой цикл tool calling.
4. **RAG + evals.** FTS → при разрыве `bge-m3`; JSONL-набор.
5. **Guardrails + observability.** Политики, redaction, метрики, бюджет.

Следующая задача пишется после завершения текущей; в `docs/tasks/` только незавершённые.

## Сейчас

`000-opencode-contract`: проверить API, записать фикстуры. Код приложения не менять.

## Правила

- `build.gradle`, тесты, CI — protected paths.
- Один PR ≤ 300 строк.
- После `done` файл задачи удаляется; история — в git.
- Wiki — `.ai/wiki/`, не трогать без разрешения.
