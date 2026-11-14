# Proposal

## Why

Сервер уже разбирает `permission.asked` и `permission.replied` и умеет отвечать в OpenCode, но страница эти события игнорирует: браузер получает общий `permission`, а `chat.js` его не обрабатывает. Прогон с инструментом, которому нужно разрешение, встаёт на ожидании человека, а пользователь видит только зависший ответ: ни действия, ни ресурсов, ни кнопок ответа.

## What Changes

- `permission.asked` показывает в ленте карточку с `action` и `resources`.
- Кнопки allow и reject отправляют решение в OpenCode; после первого клика кнопки гаснут, повторный клик не отправляет второй reply.
- После ответа — своего или полученного событием `permission.replied` — карточка закрывается, а состояние чата определяется событиями прогона.
- Устаревший или неизвестный `requestId` показывает сообщение об ошибке, карточка перестаёт ждать ввода.
- Событие с чужим `sessionID` карточку не открывает.
- Браузерный поток различает `permission.asked` и `permission.replied`.

## Capabilities

### New Capabilities

<!-- Нет: поведение относится к существующей странице чата и рантайму. -->

### Modified Capabilities

- `chat-ui`: в ленте появляется карточка запроса разрешения и ответ на неё.
- `chat-runtime`: неизвестный запрос разрешения отличается от недоступности OpenCode.

## Impact

- Задача: `docs/mini-jira/006-chat-permissions.md` — приёмка и статус.
- Код: `chat/RunEvent.java`, `chat/RunEventNames.java`, `web/SseBridge.java`, `opencode/OpenCodeClient.java`, `web/ChatMessageController.java`, `templates/chat.html`, `templates/fragments/answer.html`, `templates/fragments/run.html`, `static/js/chat.js`, `static/css/chat.css`.
- Тесты: `web/SseBridgeTest`, `web/ChatMarkupTest`, новый `opencode/OpenCodeClientPermissionTest`, новый E2E `e2e/ChatPermissionsE2ETest` с фикстурой `src/test/resources/e2e/chat-permissions.html`.
- Формат API 003 не меняется; уточняется код ответа на неизвестный `requestId`.
