---
id: "008"
title: "Джун: primary-агент и команда входа"
status: ready
created: 2026-11-13
branch: task/008-junior-agent
pr:
---

## Goal

Проектный primary-агент `junior` и проектная команда `/junior`, которая запускает его в текущей сессии TUI. Джун работает с задачами из `docs/tasks/`: оценивает очередь, задаёт человеку недостающие вопросы, реализует атомарную задачу по TDD и отдаёт результат на проверку. Вход — одна строка, внутри цикла слэш-команды не нужны.

## Non-goals

Пересмотр статусов и процедуры (010), правка `AGENTS.md`, `docs/tasks/README.md` и новая `docs/junior.md`, код-тесты конфигурации, субагенты (аналитик, чекер), плагины и хуки, глобальная установка в `~/.config/opencode/`, автоматический push/PR/удаление файла задачи, правка `.ai/wiki/`.

## Контекст и принятые решения

- `opencode --agent` для TUI не существует: флаг есть только у `run` и `mini`, а они неинтерактивны и не могут задать вопрос человеку. Поэтому вход — команда с `agent: junior` и `subagent: false`, она переключает текущую сессию и может вести диалог.
- Команда и агент лежат в репозитории, поэтому `/junior` виден только в этом проекте; `opencode.json` не меняется.
- `hidden: true` не подходит: скрытый primary нельзя ни выбрать дефолтным, ни запустить субагентом. Джун остаётся видимым primary.
- Субагенты не нужны: фоновый ребёнок не может задать вопрос человеку, а весь смысл Джуна — диалог.
- Права задаются в YAML фронтматтере упорядоченным списком `{action, resource, effect}`, последнее совпадение выигрывает. `*` и `*.env*` обязательно в кавычках: без них YAML читает алиас, и агент молча не загружается.

## Что создать

| Файл | Содержание |
| --- | --- |
| `.opencode/agents/junior.md` | primary; модель `opencode-go/deepseek-v4.1-flash`; промпт: роль, границы, ссылка на `docs/tasks/README.md`; слэш-команд не требовать |
| `.opencode/commands/junior.md` | `agent: junior`, `subagent: false`, тело `$ARGUMENTS` |

## Права

Правила упорядочены: последнее совпадение выигрывает; без совпадения — `ask`.

`junior`:

- `shell "*"` → deny, затем allow: `git status *`, `git diff *`, `git log *`, `git show *`, `git add *`, `git commit *`, `git switch *`, `git checkout *`, `git branch *`, `GRADLE_USER_HOME=/tmp/opencode/gradle-home ./gradlew *`;
- после allow — deny: `git push *`, `git reset --hard *`, `git clean *`, `git commit --amend *`, `git rebase *`;
- `edit` deny `.ai/wiki/*`;
- `external_directory "*"` deny: конфиг лаунчера лежит вне репозитория;
- `read` deny `*.env*`, затем allow `*.env.example`;
- `question "*"` allow: диалог с человеком — часть работы.

Проверить отказом на практике: `git push` не проходит, `../java-ai-agent-dev_launcher/application.yaml` не читается, `./gradlew` без `GRADLE_USER_HOME` не запускается.

## Acceptance criteria

- [ ] В TUI `/junior делай` переключает сессию на `junior` и отправляет «делай» как промпт; внутри цикла слэш-команды не требуются.
- [ ] `opencode debug agents` из корня репозитория показывает `junior`: `mode: primary`, модель `opencode-go/deepseek-v4.1-flash`, правила прав загружены без потерь.
- [ ] `git push`, `git reset --hard`, `git clean`, `git commit --amend`, `git rebase` отклоняются правами.
- [ ] `../java-ai-agent-dev_launcher/application.yaml` недоступен Джуну; `*.env*` не читается, `*.env.example` читается.
- [ ] `./gradlew` без `GRADLE_USER_HOME=/tmp/opencode/gradle-home` отклоняется, с ним — разрешён.
- [ ] Джун не создаёт файлы задач, а работает с существующим в `docs/tasks/`.
- [ ] `AGENTS.md`, `docs/tasks/README.md`, `.ai/wiki/`, `build.gradle`, `opencode.json` не изменены.
- [ ] `./gradlew test` зелёный без фильтра.

## Tests

Код-тестов нет по решению человека: конфигурация агента и команды не тестируется JUnit. Проверка — `Verification` и ручные отказы. Отсутствие блока Tests — осознанное отступление от `docs/tasks/README.md`; правило «в задаче обязателен Tests» меняется только в 010.

## Verification

```bash
GRADLE_USER_HOME=/tmp/opencode/gradle-home ./gradlew test
opencode debug agents
```

В TUI: `/junior делай` → триаж очереди. Затем попросить `git push` → отказ; попросить прочитать `../java-ai-agent-dev_launcher/application.yaml` → отказ; `./gradlew test` без `GRADLE_USER_HOME` → отказ.

## Constraints

- PR ≤ 300 строк; только два новых файла.
- `build.gradle`, существующие тесты, `.ai/wiki/`, `application.yaml` — не менять.
- Секреты только из лаунчер-конфига; ничего не логировать.

## Out of scope

Пересмотр статусов и процедуры (010), чекер для PR в `main`, субагенты, автопуш/PR, глобальные агенты в `~/.config/opencode/`.
