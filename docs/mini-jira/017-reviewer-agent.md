---
id: "017"
title: "Ревьюер как исполняемый агент, а не абзац в правилах"
created: 2026-11-14
branch: task/017-reviewer-agent
pr:
---

## Что сделать

Сделать ревьюера отдельным агентом: `/review NNN` проверяет задачу и при вердикте `OK` сам завершает её — архив change, коммит и push в `main`.

## Приёмка

- [ ] `.opencode/agents/reviewer.md` существует, `mode: subagent`, модель та же, что у Джуна; тело описывает роль, чек-лист и две формы вердикта без пересказа `docs/task-statuses.md` (ссылка, не копия).
- [ ] Права ревьюера: `edit` на `src/main/**`, `src/test/**`, `build.gradle`, личные заметки — deny; `git push --force*`, `git commit --amend`, `git reset --hard`, `git clean`, `git rebase` — deny; разрешены `git status/diff/log/show/mv/commit`, `git push *`, `git merge *`, `git switch *`, `GRADLE_USER_HOME=/tmp/opencode/gradle-home ./gradlew test`, `openspec *`.
- [ ] `.opencode/commands/review.md` запускает `agent: reviewer` с `$ARGUMENTS`; агент отказывается работать, если в `BOARD.md` у задачи не `на ревью`, и говорит, что именно не так.
- [ ] Вердикт `OK`: агент архивирует change (`openspec archive NNN-slug`), переносит файл задачи в `openspec/changes/archive/YYYY-MM-DD-NNN-slug/task.md` через `git mv`, удаляет строку из `BOARD.md`, делает один коммит в `main` и пушит его; `git status` чистый, `git log origin/main -1` совпадает с локальным.
- [ ] Вердикт `НЕ OK`: строка возвращается в `в работе`, под таблицей `docs/mini-jira/BOARD.md` в разделе `## Ревью` появляется подраздел `### NNN — Задача` с находками вида `путь/файл:строка — что не так и что ожидалось`, файл задачи не правится, архив не трогается, коммита и push нет.
- [ ] Каждый пункт AC проверяется фактически: агент в отчёте приводит команду и её вывод (файл, строка, exit code), а не «проверено».
- [ ] Прогон на 004 идёт по-настоящему: агент выдаёт одну из двух форм вердикта и при `OK` доводит финиш до `origin/main`; чужие задачи не тронуты, `git status` чистый.
- [ ] `./gradlew test` зелёный без фильтра; `openspec validate --strict --all` зелёный.
