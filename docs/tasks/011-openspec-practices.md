---
id: "011"
title: "OpenSpec: практики, цель проекта, обучение"
status: draft
created: 2026-11-14
branch: task/011-openspec-practices
pr:
---

## Goal

Внедрить OpenSpec как слой спецификаций над `docs/tasks/`: `openspec/` со спеками, changes и archive, skills для OpenCode, пилот на 003. Одновременно привести цель проекта в `README.md` и `docs/plan.md` к одной формулировке и добавить spec-driven development (OpenSpec) в список практик, которым учит проект.

## Non-goals

Spec Kit, миграция backlog и всех черновиков, CI-гейт на `openspec validate`, плагины и хуки OpenCode, изменение кода приложения и `build.gradle`, правка `.ai/wiki/`, пересмотр статусов (010).

## Контекст

- OpenSpec: change = фича (`proposal`, `specs`, `design`, `tasks`), история — в `archive/`; `NNN` = атомарный PR на ≤300 строк. Граница: один change может ссылаться на несколько `NNN`, один `NNN` — на один change.
- `openspec init --tools opencode` кладёт skills в `.agents/skills/` и не трогает `AGENTS.md`.
- Сейчас цель расходится: `README.md` — «научиться AI-разработке», `docs/plan.md` — «локальное AI Lab».

## Вопросы, которые нужно закрыть

- `NNN`-файл остаётся в `docs/tasks/` или заменяется `changes/<id>/tasks.md`.
- После `done` удаляем файл задачи (как сейчас) или переносим change в `archive/`.
- Язык спек: русский, как `docs/`, или английский.
- Одна спека на фичу или на этап из `plan.md`.

## Acceptance criteria

- [ ] Цель в `README.md` и `docs/plan.md` сформулирована одинаково и совпадает по смыслу.
- [ ] В списке того, чему учит проект (README, раздел «Цель проекта»), есть spec-driven development / OpenSpec.
- [ ] `openspec init --tools opencode` выполнен; `openspec/` и `.agents/skills/openspec-*` в git; `openspec validate --strict` зелёный.
- [ ] `AGENTS.md` и `docs/tasks/README.md` ссылаются на OpenSpec и не противоречат друг другу.
- [ ] Правило «после done файл задачи удаляется» согласовано с `archive/`: куда уходит change.
- [ ] Пилот: 003 оформлена как change (proposal + specs с Requirement/Scenario + tasks), связь change ↔ `NNN` описана.
- [ ] Черновики 004, 008–010 не переписаны; backlog не тронут.
- [ ] `.ai/wiki/` не изменён.
- [ ] `./gradlew test` зелёный без фильтра.

## Tests

Код-тестов нет: задача про документацию, конфигурацию и процесс.

| Acceptance | Test | Type |
| --- | --- | --- |
| Схема и пилотный change валидны | `openspec validate --strict` | CLI |
| Регрессия кода | `./gradlew test` | JUnit |

## Verification

```bash
openspec validate --strict
openspec list
openspec show <change-id>
GRADLE_USER_HOME=/tmp/opencode/gradle-home ./gradlew test
```

Ручная: прочитать цель в `README.md` и `docs/plan.md`, сверить с пилотом 003.

## Constraints

- Два PR: (1) `openspec init` и skills, (2) цель, `AGENTS.md`, пилот. Каждый ≤300 строк; если сгенерированные skills не влезают — согласовать исключение с человеком.
- `.ai/wiki/`, `build.gradle`, существующие тесты, `opencode.json` не менять.
- Телеметрия OpenSpec выключена: `openspec config set telemetry.enabled false`.
- Секретов в новых файлах нет.

## Depends on

- 010 (статусы и процесс): правила удаления и архивации согласуются до пилота.

## Out of scope

Миграция всех задач в `openspec/specs`, автоматическая архивация через хуки, CI-проверка спек, Spec Kit.
