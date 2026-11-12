# Java AI Agent Dev

## Цель проекта

Научиться AI-разработке и эффективной работе с агентами: интеграция с LLM, tool calling, MCP, evals, guardrails.

## Что делает приложение

Spring Boot-сервис работает с локальным OpenCode API: создаёт сессии, отправляет промпты, ждёт завершения и читает ответы. Модель — DeepSeek V4.1 Flash через провайдер `opencode-go`.

Это учебный стенд: каждая фича добавляется по циклу «спека → план → реализация агентом → ревью → проверка».

## Стек

- **Java** — 21
- **Spring Boot** — 4.1.1
- **Gradle** — сборка проекта (wrapper в репозитории)
- **Персистентность** — PostgreSQL, `jsonb`, Spring Data
- **AI** — локальный OpenCode HTTP API
- **GitHub** — работа с репозиторием и изменениями

## Статус

Каркас: база данных, Liquibase, контекстный тест и клиент OpenCode API (`src/main/java/com/example/javaaiagentdev/opencode`).

## Задания

Задачи и их статусы — в `docs/tasks/README.md`. Спека задачи пишется человеком, агент заполняет отчёт и открывает PR.

## Инструменты

- `skill-creator` — в `.opencode/skills/`.
- MCP для IntelliJ — в `opencode.json` (нужно включить MCP Server в настройках IDE).

## Wiki

Документация для людей: стартовый чек-лист — `.ai/wiki/agent-setup.md`, рекомендации — `.ai/wiki/recommendations.md`.
