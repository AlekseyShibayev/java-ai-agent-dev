# Tasks

## 1. Тесты (red)

- [ ] 1.1 Red `OpenCodeEventParserTest`: `session.execution.failed` → `ExecutionFailedEvent` с типом и текстом ошибки; `session.step.failed` событие прогона не даёт.
- [ ] 1.2 Red `RunRegistryTest`: `ExecutionFailedEvent` закрывает прогон `FAILED` с текстом ошибки.
- [ ] 1.3 Red `SseBridgeTest`: `state: failed` содержит `error` и завершает поток.
- [ ] 1.4 Red `ChatMarkupTest`: блок ответа содержит `.run-error` с `role="alert"`.
- [ ] 1.5 Red `OllamaDefaultsTest`: `application.yaml` — дефолты `ollama` / `qwen3.5:4b-q4_K_M` с переопределением `OPENCODE_PROVIDER` / `OPENCODE_MODEL`.

## 2. Код (green)

- [ ] 2.1 Green: `ExecutionFailedEvent`, `RunEvent`, `RunEventNames`, `OpenCodeEventParser`.
- [ ] 2.2 Green: `RunRegistry` — статус `FAILED` и текст ошибки.
- [ ] 2.3 Green: `SseBridge` — `state: failed` + `error`, завершение эмиттера.
- [ ] 2.4 Green: `answer.html`, `chat.js`, `chat.css` — показ ошибки и разблокировка ввода.
- [ ] 2.5 Green: дефолты в `application.yaml` и `OpenCodeClient`.

## 3. Документация

- [ ] 3.1 `docs/plan.md`: локальная модель `qwen3.5:4b-q4_K_M`, адрес Ollama, `qwen2.5:3b-instruct` удалена.
- [ ] 3.2 `README.md`: фактическая модель и этап.
- [ ] 3.3 `AGENTS.md`: дефолты `opencode.provider` / `opencode.model` в таблице.

## 4. Проверка

- [ ] 4.1 Прогон на локальной Ollama: `/api/ps` показывает загруженную модель, внешних обращений нет; ответ на «ты олама?» говорит о Qwen.
- [ ] 4.2 Ollama остановлена → «ошибка» в ленте, ввод доступен; Ollama запущена → следующий прогон успешен.
- [ ] 4.3 `./gradlew test` без фильтра зелёный; `openspec validate --strict --all` зелёный.
- [ ] 4.4 Merge ветки `task/019-local-ollama-model` в `main`, статус `на ревью`.

## Workflow follow-up

- Ревью отдельным агентом, при успехе — архив change и перенос файла задачи в `openspec/changes/archive/`.
