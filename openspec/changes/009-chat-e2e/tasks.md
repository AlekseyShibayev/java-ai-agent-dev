# Tasks

## 1. Тесты (red)

- [ ] 1.1 `StubOpenCodeClient` — стаб клиента OpenCode: доступен, создаёт сессию, отдаёт кадры SSE из блокирующего потока, сообщает об остановке прогона.
- [ ] 1.2 Red `ChatPageE2ETest.shouldOpenChatPage` — приложение поднимается на случайном порту, `/chat` открывается, баннера недоступности нет.
- [ ] 1.3 Red `ChatPageE2ETest.shouldShowSentMessageInThread` — сообщение пользователя в ленте, композер заблокирован.
- [ ] 1.4 Red `ChatPageE2ETest.shouldShowAnswerWhileItIsGenerating` — первая порция видна до второй, полный текст — после неё.
- [ ] 1.5 Red `ChatPageE2ETest.shouldReturnComposerToIdleOnStop` — `Stop` возвращает ввод в idle.

## 2. Проверка

- [ ] 2.1 `GRADLE_USER_HOME=/tmp/opencode/gradle-home ./gradlew e2e` — сценарии зелёные.
- [ ] 2.2 `GRADLE_USER_HOME=/tmp/opencode/gradle-home ./gradlew test` — без фильтра зелёный, сценарии не выполняются.
- [ ] 2.3 `openspec validate --strict --all` зелёный.
- [ ] 2.4 Merge ветки `task/009-chat-e2e` в `main`, статус `на ревью`.

## Test map

| Acceptance | Test | Type |
| --- | --- | --- |
| E2E поднимает приложение на случайном порту и открывает `/chat` | `ChatPageE2ETest.shouldOpenChatPage` | Playwright |
| отправка сообщения → сообщение появляется в ленте | `ChatPageE2ETest.shouldShowSentMessageInThread` | Playwright |
| ответ виден по мере генерации | `ChatPageE2ETest.shouldShowAnswerWhileItIsGenerating` | Playwright |
| Stop возвращает ввод в idle | `ChatPageE2ETest.shouldReturnComposerToIdleOnStop` | Playwright |
| секреты OpenCode не нужны | стаб `StubOpenCodeClient` + `shouldOpenChatPage` | Playwright |
| `./gradlew test` зелёный, сценарии только в `e2e` | `./gradlew test`, `./gradlew e2e` | CLI |
