---
id: "014"
title: "Правила статусов задач в отдельном файле"
created: 2026-11-14
branch: task/014-status-rules
pr:
---

## Goal

Вынести правила жизненного цикла задачи из `docs/tasks/README.md` в отдельный файл `docs/task-statuses.md` рядом с папкой задач, чтобы правило читалось целиком и не дублировалось в трёх местах.

## Non-goals

Изменение самих статусов, автоматизация claim, БД/трекер, правка кода.

## Acceptance criteria

- [ ] `docs/task-statuses.md` — единственное место с полным описанием статусов `создано` → `в работе` → `выполнено` → `закрыто`, переходов, владельцев и правил claim.
- [ ] `docs/tasks/README.md` не дублирует переходы, а ссылается на новый файл.
- [ ] `AGENTS.md` ссылается на `docs/task-statuses.md`, не противоречит.
- [ ] `docs/tasks/BOARD.md` ссылается на правило.
- [ ] `./gradlew test` зелёный без фильтра.

## Tests

Код-тестов нет: задача про документацию.

| Acceptance | Test | Type |
| --- | --- | --- |
| Регрессия кода | `./gradlew test` | JUnit |

## Verification

```bash
sed -n 1,60p docs/task-statuses.md
grep -rn "создано" docs/tasks/README.md AGENTS.md
GRADLE_USER_HOME=/tmp/opencode/gradle-home ./gradlew test
```

## Constraints

- `личные заметки`, `build.gradle`, тесты, `opencode.json` не менять.
- PR ≤ 300 строк.

## Out of scope

CI-проверка статусов, синхронизация с GitHub Issues, локи через refs.
