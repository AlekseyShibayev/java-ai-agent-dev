---
id: "003"
title: "Страница чата и стриминг"
status: draft
created: 2026-11-13
branch: task/003-chat-page
pr:
---

## Goal

Страница `/chat`: отправка промпта, стриминг ответа ассистента, stop, состояния соединения. Thymeleaf + HTMX + SSE.

## Non-goals

Карточки инструментов, reasoning, стоимость, permissions (задачи 004/005). Markdown. Хранение истории.

## Acceptance criteria

- [ ] `/chat` открывается при выключенном OpenCode и показывает ошибку, а не 500.
- [ ] Первое сообщение создаёт сессию OpenCode, последующие переиспользуют её.
- [ ] Текст ассистента появляется по мере генерации, не одним куском в конце.
- [ ] Enter отправляет; повторный Enter во время прогона не создаёт второй промпт.
- [ ] Stop останавливает прогон и возвращает ввод в idle.
- [ ] Ввод заблокирован, пока прогон активен.
- [ ] HTML из ответа модели не исполняется (экранирование, `white-space: pre-wrap`).
- [ ] Разрыв SSE → баннер «нет связи», после восстановления стрим продолжается.
- [ ] Браузерный SSE-endpoint отдаёт именованные события (`event: text|state|...`), а не сырые фреймы OpenCode.

## Tests

Предлагаемые имена (human подтверждает):

- `ChatPageControllerTest` — MockMvc: страница рендерится, пустой state при отсутствии сессии.
- `ChatMessageControllerTest` — POST сообщения → 202 + `runId`, блокировка второго промпта.
- `SseBridgeTest` — `SseEmitter` получает `event: text` с дельтой и `event: state` по завершении.
- `ChatMarkupTest` — HTML в тексте модели экранируется.

## Verification

```bash
GRADLE_USER_HOME=/tmp/opencode/gradle-home ./gradlew test
../java-ai-agent-dev_launcher/run.sh
```

Проверить в браузере: отправка, стриминг, stop, обрыв сервиса.

## Constraints

- `build.gradle` (protected): разрешено добавить только `spring-boot-starter-thymeleaf` и локальные `htmx.min.js` + `htmx-ext-sse.js` с фиксированными версиями.
- Без CDN: приложение запускается офлайн.
- PR ≤ 300 строк.

## Out of scope

Маркдаун, подсветка кода, карточки инструментов, approvals, несколько сессий в списке, мобильная вёрстка.
