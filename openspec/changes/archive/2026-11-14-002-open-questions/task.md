---
id: "002"
title: "Открытые вопросы рантайма чата: фактические решения"
created: 2026-11-13
archived: 2026-11-14
branch: task/002-open-questions
pr:
---

## Goal

Зафиксировать, какие решения по вопросам первой редакции этой задачи приняты в реализации, со ссылками на код и спеку. Изначально задача закрывала «решения, которые 003–007 принимают молча»; пока она лежала в backlog, 003 (рантайм, закрыт) и 004 (страница и стриминг, влит) реализовались. Поэтому файл переписан в ретроспективу: у каждого вопроса есть `Ответ:` по фактическому коду, неосуществлённое помечено `не реализовано`.

## Non-goals

Изменения кода и `build.gradle`, пересмотр закрытых 000/001/003, принятие новых решений по открытым пунктам.

## Acceptance criteria

- [ ] У каждого из 16 вопросов есть `Ответ:` со ссылкой на `файл:строка` или спеку.
- [ ] Расхождения кода со спекой и нереализованные пункты помечены `не реализовано`.
- [ ] `src/` и `build.gradle` не менялись.

## A. Контракт fan-out (P1, реализовано в 003/004)

1. **Endpoint для браузера.** Ответ: два эндпоинта, оба на конкретный прогон — `GET /api/chat/{sessionId}/runs/{runId}/events` (`chat/ChatController.java:54`, `sessionId` сверяется с прогоном, :56-58) и `GET /chat/runs/{runId}/events` для страницы (`web/SseBridge.java:44`). Общего `/api/chat/events` нет.
2. **Конверт события.** Ответ: конверта `{runId, sessionId, seq, type, payload}` нет. Имя `event:` — нормализованный тип `text|reasoning|tool|usage|state|permission` (`chat/RunEventNames.java:10-26`), `data` — JSON DTO события, `id` — порядковый номер кадра в прогоне (`web/SseBridge.java:54-71`). `seq` в конверт не выносится: `durable.seq` используется только как ключ дедупа.
3. **Переподключение.** Ответ: на странице работает `Last-Event-ID` + буфер прогона (`RunRegistry.BUFFER_LIMIT = 1000`, `chat/RunRegistry.java:18`; кадры с `id <= after` пропускаются, `web/SseBridge.java:46-62`). Вариант «старое не доигрываем» не выбран: буфер доигрывается. API-эндпоинт заголовок не читает и отдаёт буфер с начала. Переполнение буфера закрывает прогон как `failed` (`chat/RunRegistry.java:119-121`).
4. **Дельты или склейка.** Ответ: сервер отдаёт дельты как есть, склейка на клиенте. Страница получает готовый HTML-фрагмент `<span>` на каждую дельту (`web/SseBridge.java:74-76`), `text.ended` отдаётся только если дельт не было (:77-79).

## B. Жизненный цикл прогона (P1)

5. **Что закрывает run.** Ответ: только `session.execution.succeeded|interrupted` (`chat/RunRegistry.java:124-127`). `session.step.failed` и `session.tool.failed` прогон не закрывают — норма в спеке `chat-runtime`, «Ошибка инструмента не завершает прогон».
6. **Тишина в SSE.** Ответ: `не реализовано`. Таймаута «stale run» нет, статус остаётся `ACTIVE`, страница остаётся busy.
7. **Один прогон на сессию.** Ответ: на странице второй `POST /chat/messages` → `409 {"error":"Run already active"}` (`web/ChatMessageController.java:36-37`, :49-50); признак — `runId` в HTTP-сессии + статус `ACTIVE` в in-memory `RunRegistry` (`chat/RunRegistry.java:20`). В `POST /api/chat/{sessionId}/messages` проверки нет (`chat/ChatController.java:32-36`) — второй прогон стартует; для API `не реализовано`.
8. **Рестарт приложения.** Ответ: прогон теряется. Реестр в памяти; после рестарта `runId` неизвестен и отдаётся `404` (`chat/ChatErrorHandler.java:22-24`). Восстановления нет, отдельного состояния «прогон потерян» в UI нет.
9. **Какой `agent` шлём.** Ответ: из тела запроса, поле необязательное; в OpenCode уходит только непустым (`opencode/OpenCodeClient.java:85-91`). Ни конфига `opencode.agent`, ни хардкода `build` нет.

## C. Дедуп и порядок (P2)

10. **Дедуп без `durable`.** Ответ: дедуп только по `durable.seq` (`chat/RunRegistry.java:115`). События без `durable` (`usage.updated`, `text.delta`, `reasoning.delta`) не дедупятся — дубль дельты допускается и не обрабатывается.
11. **Порядок.** Ответ: порядок как в SSE, сортировки по `durable.seq` нет, гэп `seq` не детектируется.

## D. Ошибки и доступность (P2)

12. **Тело 502.** Ответ: `{"error": "<текст>"}`, а не `{code, message}` (`chat/ChatErrorHandler.java:16-20`). Все `OpenCodeUnavailableException` → 502, включая таймаут и 401; 504 и отдельного текста «проверь пароль» нет.
13. **Инвалидация URL.** Ответ: `resolvedBaseUrl` сбрасывается на любую ошибку запроса (`opencode/OpenCodeClient.java:211-215`), адрес резолвится заново через `opencode service status` при следующем вызове (:230-260). Повтора запроса в том же вызове нет: спека `chat-runtime`, «Переразрешение адреса OpenCode», обещает повтор — код его не делает.

## E. Permissions (P2)

14. **Значения `decision`.** Ответ: принимаются только `allow` и `reject` (`chat/ChatController.java:47-48`), в OpenCode уходит `{"decision": ...}` (`opencode/OpenCodeClient.java:110-113`). Реальный набор значений OpenCode и соответствие `allow` значению `once|always` в фикстурах не проверялись.
15. **Permission без ответа.** Ответ: `не реализовано`. Таймаута и авто-reject нет, карточка висит, `pending` не чистится.

## F. Объём (P1)

16. **`PR ≤ 300`.** Ответ: лимит не менялся, 003 влит одним PR (`fbda608`).

## Открытые пункты

Отдельной задачи не требуют: 6 (stale run), 7 (409 в API), 12 (401/504), 13 (повтор запроса), 15 (permission без ответа). Номер 14 — проверка контракта OpenCode, а не код.

## Tests

Задача решений, своих тестов не добавляет. Факты выше подтверждены существующими:

| Пункт | Test | Type |
| --- | --- | --- |
| 1-4 | `SseBridgeTest`, `ChatControllerTest` | MockMvc/SSE |
| 5, 10 | `RunRegistryTest` | unit |
| 7 | `ChatMessageControllerTest` | MockMvc |
| 12 | `ChatControllerTest` | MockMvc |

## Verification

```bash
git diff --stat
```

Ожидается: `docs/`, `openspec/changes/archive/`.

## Out of scope

Реализация открытых пунктов, правки 003-007, пересмотр 000/001.
