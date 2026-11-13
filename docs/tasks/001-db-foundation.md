---
id: "001"
title: "DB-фундамент"
status: draft
created: 2026-11-13
branch: task/001-db-foundation
pr:
---

## Goal

pgvector-образ, роли `owner`/`app`/`read_only`, Liquibase под `owner`, read-only datasource, SQL-guard, Testcontainers.

## Non-goals

Бизнес-таблицы, инструменты, UI.

## Acceptance criteria

- [ ] `docker compose up -d` поднимает PostgreSQL с `vector`.
- [ ] Роли созданы; пароли из env, в репозитории их нет.
- [ ] Liquibase — `owner`, приложение — `app`.
- [ ] `read_only` не может писать.
- [ ] SQL-guard: только SELECT, timeout, row limit.
- [ ] Testcontainers-тест зелёный.
- [ ] H2 context test проходит.
- [ ] `AGENTS.md` дополнен новыми ключами.

## Verification

```bash
GRADLE_USER_HOME=/tmp/opencode/gradle-home ./gradlew test
docker compose up -d
```

## Constraints

- Scope: `docker-compose.yml`, `docker/initdb/*.sql`, `application.yaml`, datasource, тест, `build.gradle`, `AGENTS.md`.
- Protected: `build.gradle`, тесты, `docker-compose.yml` — approval человека.
- Секреты — только в launcher-конфиге.

## Out of scope

Бизнес-схема, `read_only_sql`, миграция существующего volume.
