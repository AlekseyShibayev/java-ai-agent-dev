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

1. **Чат + trace.** 000/001/003/004 готовы, 005 на ревью; 002 закрыта ретроспективой, 006 — следующая, 007 готова.
2. **Инструменты.** MCP, `wiki_search`, approval.
3. **Native loop.** Ollama, свой цикл tool calling.
4. **RAG + evals.** FTS → при разрыве `bge-m3`; JSONL-набор.
5. **Guardrails + observability.** Политики, redaction, метрики, бюджет.

Следующая задача пишется после завершения текущей; в `docs/mini-jira/` только незавершённые.

## Сейчас

000/001/002/003/004/007/011 закрыты; 005 на ревью; 006 в `docs/mini-jira/`.

## Правила

- `build.gradle`, тесты, CI — protected paths.
- Один NNN — один PR ≤ 300 строк; один NNN — один change, change может покрывать несколько NNN.
- Требования — в `openspec/changes/<NNN>-<slug>/specs/`, приёмка — в файле задачи; текст не дублируется.
- Статусы и жизненный цикл — `docs/task-statuses.md`, текущие статусы — `docs/mini-jira/BOARD.md`.
- `openspec validate --strict --all` зелёный перед PR.
