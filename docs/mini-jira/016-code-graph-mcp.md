---
id: "016"
title: "Граф кода для агента: оценка GitNexus"
created: 2026-11-14
branch: task/016-code-graph-mcp
pr:
---

## Что сделать

Решить, нужен ли агенту граф кода и в каком виде он попадает в рантайм: спайк на GitNexus по этому репозиторию.

## Приёмка

- [ ] `gitnexus analyze --index-only` строит индекс по этому репозиторию (Java + Gradle), `gitnexus status` показывает свежий индекс.
- [ ] `gitnexus context OpenCodeClient` и `gitnexus impact ...` возвращают реальных вызывающих; результат сверен с `grep` по `src/main`.
- [ ] Свежесть проверена: правка файла помечает индекс устаревшим, повторный индекс укладывается в приемлемое время.
- [ ] Зафиксировано, что `analyze` без `--index-only` пишет `AGENTS.md` и ставит хуки — риск для нашего `AGENTS.md`; такие файлы в репозиторий не попадают.
- [ ] Лицензия (PolyForm Noncommercial) и требование Node 22.18+ записаны с выводом: применимо или нет и при каких условиях.
- [ ] Решение записано в файле задачи: MCP-сервер / встроенный инструмент / ничего, с последствием для стадии 2 (`docs/plan.md:24`).
- [ ] `src/` и `build.gradle` не менялись, `./gradlew test` зелёный.

## Спайк

Окружение: система ставит Node v18.19.1, GitNexus требует `^22.18.0 || >=24.11.0` (`engines` пакета). Поставлены v22.23.5 в `/tmp/opencode/node22` и `gitnexus@1.6.12` в `/tmp/opencode/gitnexus-cli` (257 npm-пакетов), всё вне репозитория. Спайк гонялся на клоне `task/016-code-graph-mcp` в `/tmp/opencode/gn/`, чтобы индекс и сгенерированные файлы не попали в рабочее дерево.

### Индекс и свежесть

```
$ gitnexus analyze --index-only
Repository indexed successfully (16.1s)
1,554 nodes | 2,967 edges | 41 clusters | 103 flows
$ gitnexus status
Indexed commit: 282ab2e / Current commit: 282ab2e
Index content: matches all 169 covered file(s)
Status: ✅ up-to-date
```

- Холодный индекс — 20.5 с стены; после правки файла повторный `analyze --index-only` — 16.4 с, снова `up-to-date`.
- Свежесть определяется по содержимому файлов, не по коммиту: дописанная пустая строка в `ChatService.java` дала `Index content: 1 changed` и `⚠️ stale (re-run gitnexus analyze)`.
- Индекс лежит в `<repo>/.gitnexus` — 26 МБ на этот репозиторий; `analyze` сам создаёт `.gitnexus/.gitignore` с `*` и дописывает `.gitnexus/` в `.git/info/exclude`, поэтому `git status` после индексации чистый (проверено).

### Граф: context и impact

`gitnexus context OpenCodeClient` находит класс (`OpenCodeClient.java:24`), 24 метода и 6 свойств, но производственные вызовы в `incoming.calls` не видит: там только тестовые конструкторы, а прод — в `incoming.imports` и `incoming.accesses` (`ChatService`, `OpenCodeEventStream`, `ChatPageController`, `ChatMessageController`, `OpenCodeSpike`). Причина — внедрение через поле: `private final OpenCodeClient client` не даёт ребра вызова.

На уровне метода точность нормальная:

```
$ gitnexus context -u "Method:.../OpenCodeClient.java:OpenCodeClient.prompt#3"
incoming.calls: ChatService.start#3, OpenCodeClient.prompt#2
method_overrides: StubOpenCodeClient (src/test/.../e2e/StubOpenCodeClient.java)
epistemic: exact
```

`ChatService.start#3` совпадает с `grep -rn "client.prompt(" src/main`: `ChatService.java:25`.

```
$ gitnexus impact -u "...OpenCodeClient.prompt#3" --direction upstream
risk: CRITICAL, impactedCount: 7
depth 1: ChatService.start, OpenCodeClient.prompt#2
depth 2: ChatController.start, ChatMessageController.start, OpenCodeSpike.run
depth 3: ChatMessageController.send, ChatMessageController.sendFromPage
affected modules: Web, Opencode
```

Сверка с `grep` по `src/main`: `ChatController.java:35 service.start(...)`, `ChatMessageController.java:72 service.start(...)`, `OpenCodeSpike.java:27 client.prompt(sessionId, ...)` — все три вызывающих на месте, лишних нет. Точность зависит от уровня: класс — только импорты/доступы, метод — реальные вызовы.

MCP-сервер работает: `gitnexus mcp` по stdio отдаёт протокол `2025-06-18` и 17 инструментов (`context`, `impact`, `query`, `detect_changes`, `rename`, `explain`, `cypher`, `trace`, `check`, …).

### Что пишет полный `analyze` и `setup`

`gitnexus analyze` без `--index-only` в чистом клоне:

- `AGENTS.md` += 45 строк — блок `<!-- gitnexus:start -->` с директивами MUST/NEVER («MUST run impact before editing», «NEVER commit before MCP/CLI graph change analysis»), таблицами ресурсов и ссылками на скиллы;
- новый `CLAUDE.md`;
- шесть каталогов `.agents/skills/gitnexus-*` и шесть `.claude/skills/gitnexus-*`.

Риск для нашего `AGENTS.md` подтверждён: блок вклинивается в конец файла и вводит свои обязательные правила поверх наших. `--index-only` не трогает ни один файл — после него `git status` пустой.

Хуки пишет не `analyze`, а `gitnexus setup` (`dist/cli/editor-targets.js:110-135`): MCP-запись `mcp.gitnexus` в `~/.config/opencode/opencode.json`, скиллы в `~/.config/opencode/skills`, и хуки `PreToolUse`/`PostToolUse` в `~/.claude/settings.json`, `~/.codex/hooks.json`, `~/.gemini/settings.json` (для OpenCode хуков нет). То есть `setup` правит глобальные конфиги — у нас это запрещено без разрешения человека.

### Лицензия и рантайм

- Лицензия `PolyForm-Noncommercial-1.0.0`: некоммерческое использование разрешено, коммерческое — нет; форк/распространение под своей лицензией нельзя. Для учебного проекта годится, для продукта — нет.
- Нужен Node `^22.18.0 || >=24.11.0`. На машине 18.19.1, то есть GitNexus тянет за собой отдельный рантайм; в репозиторий ничего не кладём, только ссылка на тул.
- Нативная часть: LadybugDB 0.18.3 + ONNX 1.31.1, 26 МБ индекса на репозиторий этого размера.

### Ограничение окружения

FTS-расширение LadybugDB не скачивается (`https://extension.ladybugdb.com/v0.18.1/linux_amd64/fts/libfts.lbug_extension`, 2.2 МБ — обрыв по таймауту), VECTOR тоже. Следствие: `gitnexus query "..."` возвращает пустой результат с warning — поиск по концепциям недоступен; `context`, `impact`, `cypher`, MCP работают. Для работы `query` нужно один раз поставить расширения при нормальной сети и сделать `gitnexus analyze --repair-fts`.

## Решение

**MCP-сервер, не встроенный инструмент, и только для агента-разработчика (dev-time); в приложение GitNexus не встраивается.**

- Подключение — MCP-сервер `gitnexus mcp` в конфиге OpenCode, без `gitnexus setup` (он правит глобальные конфиги и ставит хуки). Индекс строится только `gitnexus analyze --index-only`; `.gitnexus/` добавлен в `.gitignore`, чтобы 26 МБ индекса не попали в коммит.
- Полезность: `impact`/`context` по символу дают вызывающих и blast radius, чего `grep` не даёт; на Java+Spring работает на уровне метода, на уровне класса вызовы через DI теряются — адресоваться надо к методу (`-u`, `-f`), а не к классу.
- Ограничения, принятые сознательно: Node 22 отдельным рантаймом, FTS/`query` в этом окружении недоступны, лицензия разрешает только некоммерческое использование.
- Стадия 2 (`docs/plan.md:24`): MCP-слой остаётся нашим — `wiki_search` и собственное исполнение инструментов не меняются, GitNexus не становится зависимостью приложения. Если понадобится граф в рантайме, он подключается вторым MCP-сервером к OpenCode, а не библиотекой в Spring.

## Проверка

| Что | Результат |
| --- | --- |
| `gitnexus analyze --index-only` по клону репозитория | индекс построен, 20.5 с |
| `gitnexus status` | `up-to-date`, indexed commit = current |
| Правка файла → `status` | `stale`, `1 changed`; повторный индекс 16.4 с |
| `context`/`impact` по `OpenCodeClient.prompt` | вызывающие совпали с `grep` по `src/main` |
| Полный `analyze` | `AGENTS.md` +45 строк, `CLAUDE.md`, 12 каталогов скиллов |
| `src/`, `build.gradle` | без изменений |
