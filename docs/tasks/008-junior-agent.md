---
id: "008"
title: "Джун: primary-агент для атомарных задач"
status: draft
created: 2026-11-13
branch: task/008-junior-agent
pr:
---

## Goal

Сделать так, чтобы фраза «Джун, делай» запускала полный цикл над задачами из `docs/tasks/`: оценить очередь, найти атомарную задачу, выяснить у человека недостающее, довести задачу до `ready`, реализовать по TDD и проверить результат. Один входной агент, без слэш-команд в роли оркестратора.

## Non-goals

Плагины, хуки, автоматическое продвижение задач, автопуш и автоудаление файла задачи, правка `.ai/wiki/`, изменения в `AGENTS.md`.

## Контекст и принятые решения

- Оркестратор — сам Джун: `mode: primary`, свободный текст. Отдельный «менеджер» не нужен: писательский ребёнок гоняется с родителем по рабочему дереву и не может задать вопрос посреди работы, поэтому пишет тот, кто говорит с человеком.
- Субагенты только read-only: `junior-analyst` (триаж, вопросы) и `junior-checker` (проверка реализации).
- Человек не правит файлы: его вклад — ответы в чате и слово «да». Задачу дописывает и переводит в `ready` аналитик после подтверждения.
- Процедура живёт в `docs/junior.md`, промпт агента на неё ссылается. Иначе порядок фаз разъедется по конфигам.

## Что создать

| Файл | Содержание |
| --- | --- |
| `.opencode/agents/junior.md` | primary; модель `opencode-go/deepseek-v4.1-flash`; промпт: следовать `docs/junior.md`, фазы и границы; никаких слэш-команд не требовать |
| `.opencode/agents/junior-analyst.md` | `mode: subagent`; только чтение; возвращает таблицу `NNN \| status \| атомарна? \| чего не хватает`; меняет статус только по слову человека |
| `.opencode/agents/junior-checker.md` | `mode: subagent`; только чтение; сводка AC ↔ Tests, `git diff src/test`, полный прогон |
| `docs/junior.md` | процедура: триггеры, фазы, что запрещено, формат отчёта |
| `docs/tasks/README.md` | переписать шаг 1: человек утверждает, агент пишет файл и ставит `ready` |

## Права

Правила упорядочены: последнее совпадение выигрывает; без совпадения — `ask`.

`junior`:
- `shell *` → deny, затем allow: `git status *`, `git diff *`, `git log *`, `git show *`, `git add *`, `git commit *`, `git switch *`, `git checkout *`, `git branch *`, `GRADLE_USER_HOME=/tmp/opencode/gradle-home ./gradlew *`;
- после allow — deny: `git push *`, `git reset --hard *`, `git clean *`, `git commit --amend *`, `git rebase *`;
- `edit` deny `.ai/wiki/**`; `external_directory *` deny (конфиг лаунчера лежит вне workspace);
- `read` deny `*.env*` кроме `*.env.example`.

`junior-analyst`: `edit *` deny; `shell *` deny + allow только `git status/diff/log/show` и чтение файлов.
`junior-checker`: то же плюс `GRADLE_USER_HOME=/tmp/opencode/gradle-home ./gradlew *`.

Проверить отказом на практике: агент не должен мочь `git push` и не должен читать `../java-ai-agent-dev_launcher/application.yaml`.

## Acceptance criteria

- [ ] `opencode --agent junior` поднимает сессию, где «Джун, делай» запускает триаж без слэш-команд.
- [ ] На текущей очереди триаж отвечает «0 ready», перечисляет `draft`-задачи, игнорирует `backlog/` и ничего не пишет.
- [ ] После ответов человека аналитик сам дописывает пробелы в файл задачи, ставит `ready` и коммитит только этот файл.
- [ ] Реализация идёт по `docs/tasks/README.md`: `in-progress`, коммит только файла задачи, ветка `task/NNN-slug`, red → green → refactor, полный `./gradlew test`.
- [ ] «Джун, проверь» отдаёт сводку: AC ↔ Tests без дыр, `git diff src/test` без ослабленных ассертов, результат прогона без фильтра.
- [ ] `junior` не может `git push`, не пишет в `.ai/wiki/**`, не читает конфиг лаунчера, не запускает `./gradlew` без `GRADLE_USER_HOME`.
- [ ] `docs/junior.md` описывает процедуру; `docs/tasks/README.md` шаг 1 соответствует новой схеме.
- [ ] `./gradlew test` зелёный без фильтра.

## Tests

| Acceptance | Test | Type |
| --- | --- | --- |
| каждая `agent:` в командах и ссылки на агентов резолвятся | `AgentConfigTest.everyAgentReferenceResolves` | JUnit |
| `junior-analyst` и `junior-checker` не могут править файлы | `AgentConfigTest.readOnlySubagentsDenyEdit` | JUnit |
| ни один агент не разрешает `git push` | `AgentConfigTest.noAgentAllowsPush` | JUnit |
| `docs/junior.md` и цитируемые файлы существуют | `AgentConfigTest.referencedDocsExist` | JUnit |

Остальные AC проверяются руками по `Verification`. Имена тестов утверждает человек.

## Verification

```bash
GRADLE_USER_HOME=/tmp/opencode/gradle-home ./gradlew test
opencode --agent junior
```

В сессии: «Джун, делай» → ожидаемо «0 ready»; «Джун, проверь» → сводка без правок.

## Constraints

- `build.gradle`, существующие тесты, `.ai/wiki/`, `application.yaml` — не менять.
- Новые тесты добавлять, существующие не ослаблять.
- Секреты только из лаунчер-конфига; ничего не логировать.
- PR ≤ 300 строк.

## Out of scope

Плагины и хуки, автоматизация push/PR/удаления задачи, второй сильный менеджер на другой модели, глобальная установка агентов в `~/.config/opencode/`.

## Открытые вопросы

- `mode: primary` добавляет Джуна в переключение Tab во всех сессиях. Альтернатива — вход только через `opencode --agent junior`.
- `AgentConfigTest` — единственный механически проверяемый тест здесь. Если считаешь его избыточным, AC 2, 5, 6 остаются ручными, и блок Tests сокращается до него одного.
