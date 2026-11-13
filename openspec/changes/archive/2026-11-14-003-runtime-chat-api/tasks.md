## 1. Разбор событий

- [x] 1.1 `OpenCodeEventParser` — разбор `text`, `tool`, `approval`, `abort`; `: heartbeat` пропускается.
- [x] 1.2 Тест `OpenCodeEventParserTest` на фикстурах из `src/test/resources/opencode/`.

## 2. Реестр прогонов

- [x] 2.1 `RunRegistry` — создание прогона, подписка, закрытие по `succeeded`/`interrupted`.
- [x] 2.2 Дедуп по `(sessionID, durable.seq)`.
- [x] 2.3 Тест `RunRegistryTest`: дедуп, закрытие, ошибка инструмента не закрывает прогон.

## 3. Контроллер

- [x] 3.1 `POST /api/chat/{sessionId}/messages` → `runId`.
- [x] 3.2 `POST /api/chat/{sessionId}/abort`.
- [x] 3.3 `POST /api/chat/{sessionId}/permission/{requestId}` с `allow`/`reject`.
- [x] 3.4 502 при недоступном OpenCode.
- [x] 3.5 Тест `ChatControllerTest` (MockMvc).

## 4. Клиент OpenCode и DTO

- [x] 4.1 `abort` и `permission reply` в клиенте; тест `OpenCodeClientAbortTest`.
- [x] 4.2 DTO для text, reasoning, tool (вход + результат), usage, execution, permission.

## 5. Проверка

- [x] 5.1 `GRADLE_USER_HOME=/tmp/opencode/gradle-home ./gradlew test` зелёный.
- [ ] 5.2 Ручной прогон через `../java-ai-agent-dev_launcher/run.sh`.
