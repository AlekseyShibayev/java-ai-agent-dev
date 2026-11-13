---
id: "009"
title: "E2E страницы чата"
status: draft
created: 2026-11-14
branch: task/009-chat-e2e
pr:
---

## Goal

Закрыть отложенные в 007 сценарии чата: реальный браузер проходит `/chat` от открытия страницы до ответа ассистента. Инфраструктура Playwright уже есть, здесь добавляются только сценарии.

## Non-goals

Повторная настройка Playwright (`playwrightInstall`, тег `e2e`, trace/screenshot уже в 007). Кросс-браузерная матрица, визуальные регрессии, нагрузка, мобильные вьюпорты, параллельные шарды.

## Depends on

- 004 (страница `/chat` и стриминг) в `main`.
- 007 (инфраструктура Playwright) в `main`.

## Acceptance criteria

- [ ] E2E поднимает приложение на случайном порту и открывает `/chat`.
- [ ] Сценарий: отправка сообщения → сообщение появляется в ленте.
- [ ] Сценарий: ответ ассистента появляется по мере генерации, не одним куском в конце.
- [ ] Stop возвращает ввод в idle.
- [ ] Секреты OpenCode не нужны: ответ модели подменяется стабом, а не живым провайдером.
- [ ] `./gradlew test` остаётся зелёным; сценарии идут только в `./gradlew e2e`.

## Tests

Предлагаемые имена (human подтверждает):

| Acceptance | Test | Type |
| --- | --- | --- |
| `/chat` открывается | `ChatPageE2ETest.shouldOpenChatPage` | Playwright |
| сообщение появляется в ленте | `ChatPageE2ETest.shouldSendMessage` | Playwright |
| стриминг, а не один блок в конце | `ChatPageE2ETest.shouldStreamAssistantText` | Playwright |
| stop возвращает idle | `ChatPageE2ETest.shouldStopRun` | Playwright |
| без живого OpenCode | `ChatPageE2ETest` на профиле `e2e` со стабом | Playwright |

## Verification

```bash
GRADLE_USER_HOME=/tmp/opencode/gradle-home ./gradlew test
GRADLE_USER_HOME=/tmp/opencode/gradle-home ./gradlew e2e
```

## Constraints

- `build.gradle` не меняется: инфраструктура приходит из 007.
- Стаб OpenCode — тестовый бин или профиль `e2e`, без новых прод-ключей конфигурации.
- `build/` и артефакты не попадают в индекс.

## Out of scope

E2E для инструментов и permissions (это уровень API в 005/006), проверка дизайна, доступность (a11y).
