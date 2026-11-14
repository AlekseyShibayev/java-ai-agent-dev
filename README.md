# AI Lab (java-ai-agent-dev)

## Цель проекта

Научиться AI-разработке на локальном AI Lab: чат с агентом, trace, инструменты, RAG и evals — один Spring Boot-проект без внешних каналов.

Чему учит проект:

- **Приёмы LLM-приложений** — интеграция с LLM, tool calling, MCP, structured output, RAG, память, evals, guardrails, observability, локальные модели.
- **Spec-driven development** — OpenSpec: спека как контракт, `openspec/changes/` и `archive/`, трассировка «требование → сценарий → тест».
- **Процесс с агентом** — задача `NNN`, TDD red → green → refactor, ревью, проверка на чистом состоянии.

## Что делает приложение

AI Lab — локальное приложение для практики по всему стеку LLM-приложений:

- **Chat** — диалог с агентом, стриминг, шаги инструментов, стоимость, cancel.
- **Wiki** — краткие конспекты по технологиям; одновременно корпус для RAG.
- **Evals** — датасеты, прогоны, отчёты, регрессии.
- **Runs / Trace** — полный след каждого запуска.

Сейчас реализован каркас: Spring Boot-сервис работает с локальным OpenCode API (сессии, промпты, ожидание, чтение ответа). Модель — DeepSeek V4.1 Flash через провайдер `opencode-go`. Дальше — по плану.

Каждая фича добавляется по циклу «спека → план → реализация агентом → ревью → проверка»: спека — `openspec/changes/<NNN>-<slug>/`, задача и приёмка — `docs/tasks/NNN-<slug>.md`.

## Стек

- **Java** — 21
- **Spring Boot** — 4.1.1
- **Gradle** — сборка проекта (wrapper в репозитории)
- **Персистентность** — PostgreSQL, `jsonb`, Spring Data
- **AI** — локальный OpenCode HTTP API
- **GitHub** — работа с репозиторием и изменениями

## Статус

Каркас и DB-фундамент: база данных, Liquibase, контекстный тест, клиент OpenCode API (`src/main/java/com/example/javaaiagentdev/opencode`), pgvector/роли/read-only datasource. План — `docs/plan.md`; 000/001 закрыты, в работе — `003-runtime-chat-api`. Процесс spec-driven: `openspec/`, пилот — change `003-runtime-chat-api`.

## Документация

- `docs/plan.md` — цель, решения, 5 этапов.
- `docs/tasks/` — текущие задачи и приёмка.
- `docs/task-statuses.md` — статусы задач, claim, закрытие.
- `openspec/` — спеки (`specs/`), активные change (`changes/`) и архив (`changes/archive/`).

## Задания

Задачи — в `docs/tasks/`. Поведение фичи описано в `openspec/changes/<NNN>-<slug>/`; файл задачи ссылается на change и держит только приёмку. После выполнения change и файл задачи уходят в `openspec/changes/archive/YYYY-MM-DD-<id>/` (`task.md` рядом со спекой), история остаётся в git.

## Инструменты

- OpenSpec — CLI `@fission-ai/openspec@2.0.0`, нужен Node ≥ 22.12; команды в `AGENTS.md`.
- Skills OpenSpec — `.agents/skills/openspec-*`, в репозитории (единственное исключение из правила «skills не вендорим»).
- `skill-creator` — глобальный навык, `~/.config/opencode/skills/skill-creator/`; в репозитории не хранится.
- MCP для IntelliJ — в `opencode.json` (нужно включить MCP Server в настройках IDE).

## Wiki

- Корпус `/wiki` и RAG появится вместе с этапом RAG; источник — отдельный каталог приложения, не личные заметки.
