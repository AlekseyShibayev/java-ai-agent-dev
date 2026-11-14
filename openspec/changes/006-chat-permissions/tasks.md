# Tasks

## 1. Тесты (red)

- [ ] 1.1 Red `SseBridgeTest`: `permission.asked` и `permission.replied` приходят разными именами, событие чужой сессии не попадает в поток.
- [ ] 1.2 Red `OpenCodeClientPermissionTest`: 404 от OpenCode на reply — неизвестный запрос, 5xx — недоступность.
- [ ] 1.3 Red `ChatMarkupTest`: блок ответа содержит `data-session-id`.
- [ ] 1.4 Red E2E `ChatPermissionsE2ETest`: карточка с `action` и `resources`, allow/reject уходят в `/api/chat/{session}/permission/{request}/` с телом `{decision}`.
- [ ] 1.5 Red E2E: повторный клик не отправляет второй reply, успех закрывает карточку, `permission.replied` закрывает карточку, 404 показывает ошибку без активных кнопок, чужой `sessionID` карточку не открывает.

## 2. Реализация (green)

- [ ] 2.1 `RunEvent`: `sessionId()` на интерфейсе; `RunEventNames`: `permission.asked`, `permission.replied`.
- [ ] 2.2 `SseBridge`: событие чужой сессии не отправляется подписчику.
- [ ] 2.3 `OpenCodeClient.replyPermission`: 404 → неизвестный запрос.
- [ ] 2.4 `answer.html`, `run.html`, `chat.html`, `ChatMessageController`: `data-session-id` блока ответа.
- [ ] 2.5 `chat.js`: карточка разрешения, allow/reject, защита от повторного клика, закрытие по ответу и `permission.replied`, сообщение для устаревшего запроса, фильтр чужой сессии.
- [ ] 2.6 `chat.css`: карточка разрешения, кнопки, ошибка, компактность на мобильном.

## 3. Проверка

- [ ] 3.1 `./gradlew test` без фильтра зелёный; `./gradlew e2e` зелёный.
- [ ] 3.2 `openspec validate --strict --all` зелёный.
- [ ] 3.3 Merge ветки `task/006-chat-permissions` в `main`, статус `на ревью`.

## Workflow follow-up

- Ревью отдельным агентом; при успехе — архив change и перенос файла задачи в `openspec/changes/archive/`.
