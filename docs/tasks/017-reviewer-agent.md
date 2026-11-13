---
id: "017"
title: "Ревьюер как исполняемый агент, а не абзац в правилах"
created: 2026-11-14
branch: task/017-reviewer-agent
pr:
---

## Goal

Ревью-цикл из `docs/task-statuses.md` сейчас существует только текстом: в `.opencode/agents/` есть `junior.md`, файла ревьюера нет, поэтому вердикт, `закрыто` и архив выполняет кто угодно по памяти. Сделать ревьюера отдельным агентом с собственным набором прав, который по `/review NNN` сам проверяет задачу и заканчивает её только доказательствами: коммитит финиш в `main` и пушит, но только при вердикте `OK`.

## Non-goals

CI-гейт и GitHub Actions, автоматическое слияние ветки в `main`, второй параллельный агент в одном worktree, изменение прав Джуна, авто-ревью каждого push.

## Depends on

Ничего из незакрытых задач. Материал для прогона уже есть: 004 в статусе `выполнено`.

## Acceptance criteria

- [ ] `.opencode/agents/reviewer.md` существует, `mode: subagent`, модель та же, что у Джуна; тело описывает роль, чек-лист и две формы вердикта без пересказа `docs/task-statuses.md` (ссылка, не копия).
- [ ] Права ревьюера: `edit` на `src/main/**`, `src/test/**`, `build.gradle`, `.ai/wiki/**` — deny; `git push --force*`, `git commit --amend`, `git reset --hard`, `git clean`, `git rebase` — deny; разрешены `git status/diff/log/show/mv/commit`, `git push *`, `git merge *`, `git switch *`, `GRADLE_USER_HOME=/tmp/opencode/gradle-home ./gradlew test`, `openspec *`.
- [ ] `.opencode/commands/review.md` запускает `agent: reviewer` с `$ARGUMENTS`; агент отказывается работать, если в `BOARD.md` у задачи не `выполнено`, и говорит, что именно не так.
- [ ] Вердикт `OK`: агент архивирует change (`openspec archive NNN-slug`), переносит файл задачи в `openspec/changes/archive/YYYY-MM-DD-NNN-slug/task.md` через `git mv`, удаляет строку из `BOARD.md`, делает один коммит в `main` и пушит его; `git status` чистый, `git log origin/main -1` совпадает с локальным.
- [ ] Вердикт `НЕ OK`: строка возвращается в `в работе`, в файл задачи добавляется блок `## Ревью` с находками вида `путь/файл:строка — что не так и что ожидалось`, архив не трогается, коммита и push нет.
- [ ] Каждый пункт AC проверяется фактически: агент в отчёте приводит команду и её вывод (файл, строка, exit code), а не «проверено».
- [ ] Прогон на 004 идёт по-настоящему: агент выдаёт одну из двух форм вердикта и при `OK` доводит финиш до `origin/main`; чужие задачи не тронуты, `git status` чистый.
- [ ] `./gradlew test` зелёный без фильтра; `openspec validate --strict --all` зелёный.

## Tests

Код-тестов нет: задача про конфигурацию агента. Проверки — ручные и повторяемые.

| Acceptance | Проверка | Type |
| --- | --- | --- |
| файл агента и команда | `ls .opencode/agents/reviewer.md .opencode/commands/review.md` | ручная |
| права на запись | `grep -n -A2 "effect: deny" .opencode/agents/reviewer.md`; попытка `edit src/main/**` в прогоне — отказ | ручная |
| отказ вне `выполнено` | `/review 005` (статус `создано`) — отказ с указанием статуса | ручная |
| вердикт и финиш | `/review 004`: вердикт + `git log --oneline -1` + `git log origin/main -1` + `git status --short` | ручная |
| регрессия кода | `./gradlew test` | JUnit |

## Verification

```bash
ls .opencode/agents/reviewer.md .opencode/commands/review.md
sed -n 1,60p .opencode/agents/reviewer.md
GRADLE_USER_HOME=/tmp/opencode/gradle-home ./gradlew test
openspec validate --strict --all
git status --short
git log origin/main -3 --oneline
```

Ожидается: оба файла на месте, прогон 004 даёт вердикт в одной из двух форм и при `OK` — коммит плюс push; `git status` чистый.

## Constraints

- `src/`, `build.gradle`, существующие тесты, `opencode.json`, `.agents/skills/` не меняются.
- Секреты не читаются и не логируются: ревьюер работает без лаунчер-конфига, только `./gradlew test` на H2.
- `mode: subagent`: ревьюер не запускается сам и не порождает других агентов.
- Push только в `main` и только fast-forward; `--force`, `--no-verify`, правка `.githooks/pre-commit` запрещены, хук на секреты остаётся включённым.
- Прогон на 004 идёт в `main`: при вердикте `OK` 004 закрывается и push уходит в `main` — это ожидаемый результат прогона, а не побочный эффект; откат — `git revert`.
- PR ≤ 300 строк.

## Out of scope

Изменение прав Джуна, автозапуск ревью после merge, метрики «сколько задач прошло цикл без человека», CI, ревью внешних PR.
