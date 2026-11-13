---
id: "005"
title: "Approval в чате"
status: draft
created: 2026-11-13
branch: task/005-chat-permissions
pr:
---

## Goal

Карточка запроса разрешения в чате: `permission.asked` → allow/reject → ответ в OpenCode.

## Non-goals

Собственные политики и правила permissions, запоминание правил (`save`), audit-лог.

## Acceptance criteria

- [ ] `permission.asked` показывает карточку с `action` и `resources`.
- [ ] allow и reject отправляют `POST /api/session/{id}/permission/{requestId}/reply`.
- [ ] Повторный клик не отправляет второй reply (кнопки гаснут).
- [ ] После ответа карточка закрывается и чат возвращается в актуальное состояние.
- [ ] Устаревший/неизвестный `requestId` → сообщение об ошибке, карточка не висит.
- [ ] Чужой `sessionID` в событии не открывает карточку.

## Tests

Предлагаемые имена (human подтверждает):

- `PermissionControllerTest` — allow/reject идут на верный endpoint, повтор отклоняется.
- `PermissionCardTest` — фикстура `approval.events.json` → карточка с action и resources.
- `PermissionExpiredTest` — 404 от OpenCode → понятное сообщение, карточка закрыта.

## Verification

```bash
GRADLE_USER_HOME=/tmp/opencode/gradle-home ./gradlew test
../java-ai-agent-dev_launcher/run.sh --opencode.spike=true
```

Проверить в браузере: агент с `ask` на `shell`, allow и reject.

## Constraints

- `build.gradle` не менять.
- PR ≤ 300 строк.

## Out of scope

Редактор правил permissions, «запомнить выбор», subagent-approvals.
