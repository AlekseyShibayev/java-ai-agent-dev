---
id: "007"
title: "E2E UI: Playwright"
status: in-progress
created: 2026-11-13
branch: task/007-playwright
pr:
---

## Goal

Подключить Playwright (Java) для E2E-проверки UI: реальный браузер проходит чат от открытия страницы до появления ответа, вместо ручных кликов и MockMvc.

## Non-goals

Chrome DevTools MCP / Playwright MCP для агента, кросс-браузерная матрица, визуальные регрессии, нагрузка.

## Acceptance criteria

- [ ] Браузер ставится одной Gradle-задачей, без ручных шагов и без системного Chrome.
- [ ] E2E-тест поднимает приложение на случайном порту и открывает страницу чата.
- [ ] Сценарий: отправка сообщения → сообщение появляется в ленте.
- [ ] Тесты идут headless по умолчанию, локально можно смотреть UI.
- [ ] При падении сохраняются screenshot и trace в `build/`.
- [ ] `./gradlew test` остаётся зелёным без E2E-тегов; E2E запускается отдельной задачей.
- [ ] Секреты OpenCode не нужны для E2E (стаб или профиль без реального провайдера).

## Tests

Тесты пишутся сейчас только для инфраструктуры (AC 1, 4, 5, 6): страница `/chat` появится в 004, сценарии чата — отдельный PR.

| Acceptance | Test | Type |
| --- | --- | --- |
| браузер ставится одной Gradle-задачей | `./gradlew playwrightInstall` | CLI |
| браузер запускается без системного Chrome | `PlaywrightSmokeE2ETest.shouldLaunchBrowser` | Playwright |
| падение сохраняет screenshot и trace в `build/` | `PlaywrightArtifactsE2ETest.shouldSaveArtifactsWhenTestFails` | Playwright |
| headless по умолчанию, `-Pheaded` включает UI | `PlaywrightConfigTest.shouldDefaultToHeadless` | JUnit |
| `./gradlew test` не запускает E2E | `./gradlew test` — тег `e2e` исключён | CLI |

## Verification

```bash
GRADLE_USER_HOME=/tmp/opencode/gradle-home ./gradlew playwrightInstall
GRADLE_USER_HOME=/tmp/opencode/gradle-home ./gradlew e2e
GRADLE_USER_HOME=/tmp/opencode/gradle-home ./gradlew test
```

## Constraints

- `build.gradle` меняется только в рамках этой задачи, минимальная дельта.
- Без внешних сервисов: E2E не должен зависеть от живого OpenCode и БД.
- `build/` и артефакты не попадают в индекс.

## Out of scope

E2E для инструментов и permissions (задачи 005/006 покрывают это на уровне API), мобильные вьюпорты, параллельные шарды.
