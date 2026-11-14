---
id: "006"
title: "Approval в чате"
created: 2026-11-13
branch: task/006-chat-permissions
pr:
---

## Что сделать

Карточка запроса разрешения в чате: `permission.asked` → allow/reject → ответ в OpenCode.

## Приёмка

- [ ] `permission.asked` показывает карточку с `action` и `resources`.
- [ ] allow и reject отправляют `POST /api/session/{id}/permission/{requestId}/reply`.
- [ ] Повторный клик не отправляет второй reply (кнопки гаснут).
- [ ] После ответа карточка закрывается и чат возвращается в актуальное состояние.
- [ ] Устаревший/неизвестный `requestId` → сообщение об ошибке, карточка не висит.
- [ ] Чужой `sessionID` в событии не открывает карточку.
