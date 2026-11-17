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
| Локальная модель | Ollama `qwen3.5:4b-q4_K_M` на `127.0.0.1:11434` |
| Спецификации | OpenSpec: change `<NNN>-<slug>`, спека — на capability |
| Секреты | только launcher-конфиг |

## Этапы

1. **Чат + trace.** 000/001/003/004/005/006 готовы; 002 закрыта ретроспективой, 007 готова.
2. **Инструменты.** MCP, `wiki_search`, approval.
3. **Native loop.** Ollama, свой цикл tool calling.
4. **RAG + evals.** FTS → при разрыве `bge-m3`; JSONL-набор.
5. **Guardrails + observability.** Политики, redaction, метрики, бюджет.

## Сейчас

Закрытые change — в `openspec/changes/archive/`; активные — `018-chat-styling` и `019-local-ollama-model`.

## Правила

- `build.gradle`, тесты, CI — protected paths.
- Один change — один PR ≤ 300 строк; id change — `NNN-slug`, номер не переиспользуется.
- Требования — в `openspec/changes/<NNN>-<slug>/specs/`, текст не дублируется.
- `openspec validate --strict --all` зелёный перед PR.
