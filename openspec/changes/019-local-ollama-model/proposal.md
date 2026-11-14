# Proposal

## Why

Чат по умолчанию ходит во внешнюю модель `opencode-go/deepseek-v4.1-flash`, хотя проект — локальный AI Lab, а на машине уже работает Ollama с `qwen3.5:4b-q4_K_M`. Документация (`docs/plan.md`, `README.md`) при этом называет ещё и третью модель, `qwen2.5:3b-instruct`, которой нет. Без локальной модели этап 3 (native loop) не на чем проверять.

Отдельная проблема — ошибка провайдера. Если Ollama остановлена, OpenCode принимает промпт и через `session.execution.failed` сообщает об ошибке, но приложение этот терминальный результат не разбирает: прогон остаётся `ACTIVE`, страница навсегда блокирует ввод, а ответа нет.

## What Changes

- Дефолты `opencode.provider` / `opencode.model` — `ollama` и `qwen3.5:4b-q4_K_M`; переопределение через `OPENCODE_PROVIDER` / `OPENCODE_MODEL` сохраняется.
- `session.execution.failed` разбирается как терминальный результат прогона: прогон закрывается `failed` с сообщением провайдера, поток SSE отправляет `state: failed` с текстом ошибки и завершается.
- Страница чата показывает ошибку прогона над ответом и разблокирует ввод, поэтому после запуска Ollama следующее сообщение проходит в той же сессии.
- `docs/plan.md`, `README.md`, `AGENTS.md` называют фактическую модель и адрес Ollama; строка `qwen2.5:3b-instruct` убрана.

## Capabilities

### New Capabilities

<!-- Нет: обе капабилити уже существуют. -->

### Modified Capabilities

- `chat-runtime`: модель прогона по умолчанию — локальная Ollama; ошибка исполнения закрывает прогон.
- `chat-ui`: страница показывает ошибку прогона и снова принимает ввод.

## Impact

- Задача: `docs/mini-jira/019-local-ollama-model.md` — приёмка и статус.
- Код: `src/main/resources/application.yaml`, `opencode/OpenCodeClient.java`, `chat/OpenCodeEventParser.java`, `chat/ExecutionFailedEvent.java` (новый), `chat/RunEvent.java`, `chat/RunEventNames.java`, `chat/RunRegistry.java`, `web/SseBridge.java`, `static/js/chat.js`, `static/css/chat.css`, `templates/fragments/answer.html`.
- Тесты: `OpenCodeEventParserTest`, `RunRegistryTest`, `SseBridgeTest`, `ChatMarkupTest`, новый `OllamaDefaultsTest`.
- Документация: `docs/plan.md`, `README.md`, `AGENTS.md`.
- Схема БД, API-контракты публичных ручек и `build.gradle` не меняются.
