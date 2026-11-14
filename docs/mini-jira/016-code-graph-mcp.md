---
id: "016"
title: "Граф кода для агента: оценка GitNexus"
created: 2026-11-14
branch: task/016-code-graph-mcp
pr:
---

## Goal

Решить, нужен ли агенту граф кода (code-graph RAG) и в каком виде он попадает в рантайм, и проверить GitNexus на этом репозитории. Спайк: вывод пишется в файл задачи, кода в `src/` не добавляет.

## Non-goals

MCP-клиент в рантайме, замена `wiki_search` и RAG-стадии, индексация в CI, обзор альтернатив (Sourcegraph, Potpie, code-graph-rag), веб-UI GitNexus.

## Depends on

Ничего из незакрытых задач: спайк независим, нужен только локальный Node ≥ 22.18.

## Acceptance criteria

- [ ] `gitnexus analyze --index-only` строит индекс по этому репозиторию (Java + Gradle), `gitnexus status` показывает свежий индекс.
- [ ] `gitnexus context OpenCodeClient` и `gitnexus impact ...` возвращают реальных вызывающих; результат сверен с `grep` по `src/main`.
- [ ] Свежесть проверена: правка файла помечает индекс устаревшим, повторный индекс укладывается в приемлемое время.
- [ ] Зафиксировано, что `analyze` без `--index-only` пишет `AGENTS.md` и ставит хуки — риск для нашего `AGENTS.md`; такие файлы в репозиторий не попадают.
- [ ] Лицензия (PolyForm Noncommercial) и требование Node 22.18+ записаны с выводом: применимо или нет и при каких условиях.
- [ ] Решение записано в файле задачи: MCP-сервер / встроенный инструмент / ничего, с последствием для стадии 2 (`docs/plan.md:24`).
- [ ] `src/` и `build.gradle` не менялись, `./gradlew test` зелёный.

## Tests

Спайк, кода не добавляет. Проверки ручные:

| Acceptance | Проверка | Type |
| --- | --- | --- |
| индекс строится | `npx gitnexus analyze --index-only && npx gitnexus status` | ручная |
| `context`/`impact` верны | сверить вывод с `grep -rn OpenCodeClient src/main` | ручная |
| свежесть | правка файла → `gitnexus status` → повторный `analyze` | ручная |
| риск для `AGENTS.md` | `git status` после `analyze` без `--index-only` | ручная |
| лицензия и Node | `LICENSE`, `package.json engines`, README | ревью |

## Verification

```bash
GRADLE_USER_HOME=/tmp/opencode/gradle-home ./gradlew test
git status --short
```

Ожидается: `git status` пуст, индекс и `node_modules` вне репозитория.

## Constraints

- Глобальные конфиги (`~/.gradle`, `~/.config`) не меняются, `AGENTS.md` не перезаписывается.
- Индекс живёт вне отслеживаемых путей: либо в `.gitignore`, либо в `~/.local`, `build/` в индекс не попадает.
- Индексация локальная, исходники никуда не отправляются; секреты OpenCode и БД не нужны.
- Лицензионный вывод — не юридическое заключение: коммерческое использование не заявляется.

## Out of scope

Реализация MCP-клиента и инструментов (стадия 2), оценка других инструментов класса, сравнение качества на других языках, интеграция с `/chat`.
