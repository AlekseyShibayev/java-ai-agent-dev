## 1. Разбор событий

- [ ] 1.1 `OpenCodeEventParser` — разбор `text`, `tool`, `approval`, `abort`; `: heartbeat` пропускается.
- [ ] 1.2 Тест `OpenCodeEventParserTest` на фикстурах из `src/test/resources/opencode/`.

## 2. Реестр прогонов

- [ ] 2.1 `RunRegistry` — создание прогона, подписка, закрытие по `succeeded`/`interrupted`.
- [ ] 2.2 Дедуп по `(sessionID, durable.seq)`.
- [ ] 2.3 Тест `RunRegistryTest`: дедуп, закрытие, ошибка инструмента не закрывает прогон.

## 3. Контроллер

- [ ] 3.1 `POST /api/chat/{sessionId}/messages` → `runId`.
- [ ] 3.2 `POST /api/chat/{sessionId}/abort`.
- [ ] 3.3 `POST /api/chat/{sessionId}/permission/{requestId}` с `allow`/`reject`.
- [ ] 3.4 502 при недоступном OpenCode.
- [ ] 3.5 Тест `ChatControllerTest` (MockMvc).

## 4. Клиент OpenCode и DTO

- [ ] 4.1 `abort` и `permission reply` в клиенте; тест `OpenCodeClientAbortTest`.
- [ ] 4.2 DTO для text, reasoning, tool (вход + результат), usage, execution, permission.

## 5. Проверка

- [ ] 5.1 `GRADLE_USER_HOME=/tmp/opencode/gradle-home ./gradlew test` зелёный.
- [ ] 5.2 Ручной прогон через `../java-ai-agent-dev_launcher/run.sh`.
