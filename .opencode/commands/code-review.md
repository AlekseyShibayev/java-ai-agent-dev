---
description: Code review задачи по номеру — взять из «на ревью», выгрузить ветку, вынести вердикт
agent: general
subagent: true
---

Ты — ревьюер этого репозитория. Код писал другой агент, твоя сессия чистая.
Аргумент — номер задачи: `$1` (например, `021`).

## Взятие

1. `NNN` — первые три цифры из `$1`: `021`, `021-code-review-command` и `task/021-code-review-command` дают `021`. Не три цифры — остановись: «нужен номер задачи, например `/code-review 021`».
2. Файл задачи — ровно один `docs/mini-jira/NNN-*.md`. Нет файла — остановись и назови шаблон поиска.
3. `docs/mini-jira/BOARD.md`: строка задачи должна быть в статусе `на ревью`. Нет строки или другой статус — остановись, назови фактический статус и ничего не меняй.
4. Ветка: `branch:` из frontmatter файла задачи; пусто — `task/NNN-<slug>` из имени файла. `git rev-parse --verify <branch>`; ветки нет — остановись. `git fetch origin`, затем `git merge-base --is-ancestor <branch> origin/main`; не предок — остановись: ветка не влита.
5. `git symbolic-ref --short HEAD` = `main`; иначе остановись. `git status --porcelain -- docs/mini-jira/BOARD.md` пуст; иначе остановись и назови файлы.
6. `git pull --rebase`, перечитай `BOARD.md`. Всё ещё `на ревью` — поставь `в ревью` в строке задачи, закоммить только `BOARD.md` (`git commit docs/mini-jira/BOARD.md`) сообщением `Task NNN: review in progress`, `git push`. Push отклонён — `git pull --rebase`, перечитай строку и повтори; статус уже не `на ревью` — остановись.
7. Основной checkout не переключай: его ветку держит человек. Выгрузи ветку отдельно:

   ```sh
   wt=$(mktemp -d /tmp/opencode/review-NNN.XXXXXX)
   git worktree add --detach "$wt" <branch>
   ```

   Не вышло — верни статус `на ревью` отдельным коммитом в `main`, затем остановись.

## Проверка

Ревьюй выгруженную ветку, а не `main`:

- `git -C "$wt" diff origin/main...HEAD --stat` и `git -C "$wt" diff origin/main...HEAD` — весь диф.
- Чек-лист — `docs/task-statuses.md`, раздел «Ревью», целиком. Для команд: `cd "$wt" && GRADLE_USER_HOME=/tmp/opencode/gradle-home ./gradlew test` без фильтра; `export PATH=/tmp/opencode/node22/bin:/tmp/opencode/openspec-cli/node_modules/.bin:$PATH` и `openspec validate --strict --all` из `"$wt"`, если у задачи есть change.
- Код не правишь: `src/main/**`, `src/test/**`, `build.gradle`, существующие тесты — только читаешь. Находка — строка отчёта, а не правка.

## Вердикт

Только `OK` или `НЕ OK` со списком находок `путь/файл:строка — что не так и что ожидалось`.

Убери временный worktree (`git worktree remove --force "$wt"`) и проверь, что строка всё ещё `в ревью`; иначе остановись и назови фактический статус.

`OK`:

1. `openspec archive NNN-slug --yes`, если `openspec/changes/NNN-slug/` есть; иначе пропусти.
2. `git mv <файл задачи> openspec/changes/archive/YYYY-MM-DD-NNN-slug/task.md`; дата — сегодняшняя; папки нет — создай.
3. Удали строку задачи из `BOARD.md` и подраздел `### NNN` из `## Ревью`, если он есть.
4. Один коммит `Task NNN: closed after review`, `git push`.

`НЕ OK`:

1. Верни строку в `в работе`. Файл задачи не правь, архив не трогай.
2. В `## Ревью` добавь или замени подраздел:

   ````markdown
   ### NNN — <заголовок задачи>

   - `путь/файл:строка` — что не так и что ожидалось.
   ````

3. Один коммит `Task NNN: review findings`, `git push`.

После любого вердикта: рабочее дерево чистое, ветка основного checkout не менялась, `git log origin/main -1` совпадает с локальным.

## Отчёт

Верни: коммит взятия, вердикт, по каждому пункту чек-листа — команду и фактический вывод (файл/строка/exit code), для `НЕ OK` — находки. Слово «проверено» без вывода не считается.
