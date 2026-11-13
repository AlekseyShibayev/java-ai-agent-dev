---
id: "012"
title: "OpenAPI: springdoc и Swagger UI"
status: in-progress
created: 2026-11-14
branch: task/012-openapi-docs
pr:
---

## Goal

Отдавать машиночитаемое описание HTTP API: `GET /v3/api-docs` (OpenAPI 3) и Swagger UI. Документ строится из кода, отдельного контракт-файла нет.

## Non-goals

Аннотации на контроллерах чата (появятся вместе с 003), contract-first (`openapi.yaml`), генерация клиента, авторизация в Swagger UI, проверка документа в CI, настройка `paths-to-match` (нечего фильтровать, пока нет HTML-контроллеров).

## Acceptance criteria

- [ ] В `build.gradle` добавлен `org.springdoc:springdoc-openapi-starter-webmvc-ui:3.2.0` (собран под Boot 4.1.2).
- [ ] `GET /v3/api-docs` → 200, валидный JSON, поле `openapi` начинается с `3.`.
- [ ] `GET /swagger-ui.html` доступен (200 или редирект).
- [ ] Приложение стартует в профиле `test` (H2), существующие тесты не сломаны.
- [ ] `./gradlew test` без фильтра зелёный.

## Tests

| Acceptance | Test | Type |
| --- | --- | --- |
| `/v3/api-docs` → 200 + JSON | `OpenApiDocsTest.shouldExposeApiDocs` | MockMvc |
| Swagger UI доступен | `OpenApiDocsTest.shouldServeSwaggerUi` | MockMvc |

## Verification

```bash
GRADLE_USER_HOME=/tmp/opencode/gradle-home ./gradlew test
```

## Constraints

- Правка `build.gradle` разрешена этой задачей (явное разрешение человека).
- PR ≤ 300 строк.

## Out of scope

Описание конкретных эндпоинтов, код на Java-клиентах, UI.
