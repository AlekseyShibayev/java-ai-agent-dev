# План

Статус: draft.

## Цель

Научиться AI-разработке на локальном AI Lab: чат с агентом, trace, инструменты, RAG и evals — один Spring Boot-проект без внешних каналов.

## Решения

| Что | Как |
| --- | --- |
| Рантайм | OpenCode первым, native loop потом |
| UI | Thymeleaf + HTMX + SSE |
| БД | PostgreSQL, read-only роль для агента |
| Поиск | Postgres FTS; `bge-m3` + pgvector только при разрыве |
| Локальная модель | Ollama `qwen2.5:3b-instruct` |
| Спецификации | OpenSpec: change `<NNN>-<slug>`, спека — на capability |
| Секреты | только launcher-конфиг |

## Этапы

1. **Чат + trace.** 000/001 готово → 003 (рантайм чата) → 004 (SSE/UI). 002 (открытые вопросы) закрыта ретроспективой, 005–006 — в backlog, 007 готова.
2. **Инструменты.** MCP, `wiki_search`, approval.
3. **Native loop.** Ollama, свой цикл tool calling.
4. **RAG + evals.** FTS → при разрыве `bge-m3`; JSONL-набор.
5. **Guardrails + observability.** Политики, redaction, метрики, бюджет.

Следующая задача пишется после завершения текущей; в `docs/tasks/` только незавершённые.

## Сейчас

003 закрыта; `004-chat-page` в работе после ревью — исправляются находки; 005–006 в `docs/tasks/`. 000/001/002/007/011 закрыты.

## Правила

- `build.gradle`, тесты, CI — protected paths.
- Один NNN — один PR ≤ 300 строк; один NNN — один change, change может покрывать несколько NNN.
- Требования — в `openspec/changes/<NNN>-<slug>/specs/`, приёмка — в файле задачи; текст не дублируется.
- Статусы и жизненный цикл — `docs/task-statuses.md`, текущие статусы — `docs/tasks/BOARD.md`.
- `openspec validate --strict --all` зелёный перед PR.
- Wiki — `.ai/wiki/`, не трогать без разрешения.
