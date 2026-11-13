# Tasks

## 1. Каркас страницы

- [x] 1.1 Вендорить HTMX `2.0.11` и `htmx-ext-sse` `2.2.4` в `src/main/resources/static/js/`; проверка: файлы на месте, в шаблонах нет ссылок на внешние URL.
- [x] 1.2 Red `ChatPageControllerTest`: `/chat` отдаёт 200 с пустой лентой; при недоступном OpenCode — 200 и сообщение об ошибке, а не 500.
- [x] 1.3 Green: `spring-boot-starter-thymeleaf` в `build.gradle`, контроллер страницы, `chat.html` с композером и пустым состоянием; тест `ChatPageControllerTest` зелёный.

## 2. Отправка сообщения

- [x] 2.1 Red `ChatMessageControllerTest`: `POST /chat/messages` → `202 {runId, sessionId}`; первое сообщение создаёт сессию, следующее переиспользует её; при активном прогоне — `409`, промпт в OpenCode не уходит.
- [x] 2.2 Green: хранение `opencodeSessionId` и `runId` в HTTP-сессии, `ChatMessageController`, запрет второго прогона по `RunRegistry`; тест `ChatMessageControllerTest` зелёный.
- [x] 2.3 Ответ на HTMX-запрос (`HX-Request`) — фрагмент с сообщением пользователя, контейнером ответа с `sse-connect` и busy-композером; проверка — тест рендера фрагмента с `text` и `<script>` в тексте пользователя.

## 3. Поток событий

- [x] 3.1 Red `SseBridgeTest`: `event: text` содержит экранированную дельту и `id`; `event: state` приходит при завершении; `Last-Event-ID` пропускает уже отданные события; завершённый прогон, догнавший буфер, получает `204`.
- [x] 3.2 Green: `SseBridge` и `GET /chat/runs/{runId}/events`; `SseBridgeTest` зелёный.
- [x] 3.3 Открытие `/chat` при активном прогоне восстанавливает контейнер с тем же SSE-endpoint; проверка — тест `ChatPageControllerTest` на модель с активным прогоном.

## 4. Разметка и клиентское поведение

- [x] 4.1 Red `ChatMarkupTest`: HTML из ответа модели экранируется, контейнер сохраняет переносы (`white-space: pre-wrap`).
- [x] 4.2 Green: разметка и стили ленты; `ChatMarkupTest` зелёный.
- [x] 4.3 Red `ChatMessageControllerTest`: `POST /chat/stop` отменяет прогон и возвращает idle-композер.
- [x] 4.4 Green: stop-endpoint через HTMX; тест зелёный.
- [x] 4.5 Скрипт страницы: нативный `EventSource`, баннер «нет связи» только при обрыве, разблокировка ввода по `state`, блокировка на время прогона; проверка — вручную в браузере.

## 5. Проверка

- [x] 5.1 `GRADLE_USER_HOME=/tmp/opencode/gradle-home ./gradlew test` без фильтра — зелёный.
- [ ] 5.2 DOM-проверка в браузере: баннер обрыва, разблокировка ввода — перенесена в 009 (E2E). Серверная часть проверена curl: страница 200 при выключенном OpenCode, отправка, стриминг, stop → `interrupted`, продолжение после `Last-Event-ID`.

## Workflow follow-up

- Merge ветки `task/004-chat-page` в `main`, статус `выполнено`.
- Ревью отдельным агентом, при успехе — архив change и перенос файла задачи в `openspec/changes/archive/`.
