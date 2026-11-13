---
id: "011"
title: "OpenSpec: практики, цель проекта, обучение"
status: review
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

## Решения

- **D1. Роли артефактов.** `docs/tasks/NNN-slug.md` — единица работы и приёмки: id, статус, AC, ссылка на change. `openspec/changes/<id>/` — поведение (Requirement/Scenario) и план (design, tasks). Текст не дублируется: AC отвечает «что принять», спека — «как система ведёт себя». При расхождении спека главнее, AC правит человек.
- **D2. После done.** Файл задачи удаляется, как сейчас; change архивируется через `openspec archive` в `openspec/changes/archive/YYYY-MM-DD-<id>/`.
- **D3. Язык.** Текст спек русский, служебные заголовки и SHALL/MUST — английские (`openspec init --language ru`).
- **D4. Гранулярность.** Спека — на capability (долговечное поведение, `chat-runtime`), не на этап `plan.md`.
- **D5. Идентификатор change.** `<NNN>-<slug>` совпадает с файлом задачи — трассируемость важнее стиля имён.
- **D6. Skills вендорятся.** `.agents/skills/openspec-*` в git, решение человека 2026-11-14; лимит PR ≤300 строк на этот PR снят (разрешено >5000 строк).

## Подзадачи

- [x] 011.1 Решения, статус, ветка (этот файл).
- [x] 011.2 Тулчейн: Node ≥22.12 и `@fission-ai/openspec@2.0.0`; телеметрия выключена.
- [x] 011.3 `openspec init --tools opencode --language ru`; `openspec/config.yaml`, `.agents/skills/openspec-*` в git.
- [x] 011.4 Цель: одна формулировка в `README.md` и `docs/plan.md`; OpenSpec в списке практик.
- [x] 011.5 `AGENTS.md` и `docs/tasks/README.md`: ссылки на OpenSpec, правила D1–D5, команды.
- [x] 011.6 Пилот: change `003-runtime-chat-api` (proposal, specs, design, tasks) + связь с NNN-файлом 003.
- [x] 011.7 Проверка: `openspec validate --strict`, `./gradlew test` без фильтра.
- [x] 011.8 Обновить README «Статус» и «Документация»; удаление файла задачи — на шаге `done`.

## Acceptance criteria

- [ ] Цель в `README.md` и `docs/plan.md` сформулирована одинаково и совпадает по смыслу.
- [ ] В списке того, чему учит проект (README, раздел «Цель проекта»), есть spec-driven development / OpenSpec.
- [ ] `openspec init --tools opencode` выполнен; `openspec/` и `.agents/skills/openspec-*` в git; `openspec validate --strict` зелёный на пилотном change.
- [ ] `AGENTS.md` и `docs/tasks/README.md` ссылаются на OpenSpec и не противоречат друг другу; правило про вендоринг skills заменено на D6.
- [ ] Правило «после done файл задачи удаляется» согласовано с `archive/`: change уходит в `openspec/changes/archive/YYYY-MM-DD-<id>/` (D2).
- [ ] Пилот: 003 оформлена как change (proposal + specs с Requirement/Scenario + tasks), связь change ↔ `NNN` описана в обоих файлах.
- [ ] Черновики 004, 009, 010 не переписаны; backlog не тронут.
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
export PATH=/tmp/opencode/node22/bin:/tmp/opencode/openspec-cli/bin:$PATH
openspec validate --strict
openspec list
openspec show 003-runtime-chat-api
GRADLE_USER_HOME=/tmp/opencode/gradle-home ./gradlew test
```

Ручная: прочитать цель в `README.md` и `docs/plan.md`, сверить с пилотом 003.

## Constraints

- Один PR: лимит ≤300 строк снят человеком (2026-11-14) — сгенерированные skills и пилот в этом PR.
- `.ai/wiki/`, `build.gradle`, существующие тесты, `opencode.json` не менять.
- Телеметрия OpenSpec выключена: `openspec config set telemetry.enabled false` (глобальная настройка машины).
- Секретов в новых файлах нет.

## Depends on

Нет. 010 (статусы) не блокирует: D1–D2 не меняют схему статусов.

## Out of scope

Миграция всех задач в `openspec/specs`, автоматическая архивация через хуки, CI-проверка спек, Spec Kit.
