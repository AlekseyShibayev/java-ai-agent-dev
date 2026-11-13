---
id: "001"
title: "DB-фундамент"
status: review
created: 2026-11-13
branch: task/001-db-foundation
pr:
---

## Goal

pgvector-образ, роли `owner`/`app`/`read_only`, Liquibase под `owner`, read-only datasource, SQL-guard, Testcontainers.

## Non-goals

Бизнес-таблицы, инструменты, UI.

## Result

- `docker-compose.yml`: `pgvector/pgvector:pg17`, init-скрипт ролей.
- `docker/initdb/01-roles.sh`: `owner` (миграции), `app` (DML), `read_only` (SELECT).
- Liquibase ходит `owner`; приложение — `app`.
- `ReadOnlyDataSourceConfig` — отдельный datasource, только если задан URL.
- `SqlGuard`: только SELECT, один statement, без комментариев и `INTO`.
- `SqlGuardTest` + `DbFoundationTest` (Testcontainers).
- `AGENTS.md` дополнен ключами.

## Acceptance criteria

- [ ] `docker compose up -d` поднимает PostgreSQL с `vector`.
- [ ] Роли созданы; пароли из env, в репозитории их нет.
- [ ] Liquibase — `owner`, приложение — `app`.
- [ ] `read_only` не может писать.
- [x] SQL-guard: только SELECT, timeout, row limit.
- [ ] Testcontainers-тест зелёный.
- [ ] H2 context test проходит.
- [x] `AGENTS.md` дополнен новыми ключами.

## Tests

| Acceptance | Test | Type |
| --- | --- | --- |
| SQL-guard: только SELECT | `SqlGuardTest.allowsSelect`, `SqlGuardTest.rejectsUpdate` | unit |
| read_only не пишет | `DbFoundationTest.rolesAreLeastPrivilege` | Testcontainers |
| H2 context | `JavaAiAgentDevApplicationTests.contextLoads` | context |

## Verification

```bash
GRADLE_USER_HOME=/tmp/opencode/gradle-home ./gradlew test
docker compose down -v
docker compose up -d
```

## Notes

- Gradle-тесты не запускались: Maven Central недоступен (DNS) в этом окружении.
- `SqlGuard` проверен вручную.
- Существующий volume `pgdata` нужно пересоздать: `docker compose down -v`.
- Launcher-конфиг: добавить `DB_OWNER_USERNAME/PASSWORD`, `DB_READONLY_URL/USERNAME/PASSWORD`.

## Constraints

- Scope: `docker-compose.yml`, `docker/initdb/*.sh`, `application.yaml`, datasource, тесты, `build.gradle`, `AGENTS.md`.
- Protected: `build.gradle`, тесты, `docker-compose.yml` — approval человека.
- Секреты — только в launcher-конфиге.

## Out of scope

Бизнес-схема, `read_only_sql`, миграция существующего volume.
