---
id: "019"
title: "Чат по умолчанию на локальной модели Ollama"
created: 2026-11-14
branch: task/019-local-ollama-model
pr:
---

## Goal

`docs/plan.md:17` требует локальную модель, а приложение по умолчанию ходит в провайдер `opencode-go` (`deepseek-v4.1-flash`): на вопрос «ты олама?» модель отвечает про OpenCode и DeepSeek. Переключить дефолты на уже установленную Ollama и привести план и README к факту.

## Non-goals

Установка и скачивание моделей, сравнение качества моделей, переключатель провайдера в UI, эмбеддинги и RAG, собственный OpenAI-клиент вместо OpenCode API.

## Depends on

Локальная инфраструктура уже есть: Ollama на `127.0.0.1:11434` с моделью `qwen3.5:4b-q4_K_M`; OpenCode v2.0.31 видит её как `ollama/qwen3.5:4b-q4_K_M`. Кода в задаче нет — только конфигурация и проверка.

## Acceptance criteria

- [ ] `opencode.provider` и `opencode.model` по умолчанию — `ollama` и `qwen3.5:4b-q4_K_M`; переопределение через `OPENCODE_PROVIDER`/`OPENCODE_MODEL` сохраняется.
- [ ] Сообщение в `/chat` обслуживает локальная модель: во время прогона `GET 127.0.0.1:11434/api/ps` показывает загруженную `qwen3.5:4b-q4_K_M`, обращений к `opencode-go` за прогон нет.
- [ ] На вопрос «ты олама?» ответ говорит о локальной модели/Qwen, а не о DeepSeek и не о роли агента OpenCode.
- [ ] Ollama остановлена → прогон завершается ошибкой на странице (не 500 и не вечный busy); после запуска Ollama следующий прогон успешен.
- [ ] `docs/plan.md` и `README.md` называют фактическую локальную модель и адрес Ollama; строка `qwen2.5:3b-instruct` убрана.
- [ ] `./gradlew test` без фильтра и `openspec validate --strict --all` зелёные.

## Tests

| Acceptance | Test | Type |
| --- | --- | --- |
| дефолты — локальная модели | LocalModelDefaultsTest.shouldDefaultToOllamaModel | JUnit (@SpringBootTest, `OpenCodeProperties`) |
| env-переопределение работает | LocalModelDefaultsTest.shouldAllowModelOverride | JUnit (@SpringBootTest, `properties`) |
| ответ идёт из Ollama | ручная: `/chat` + `curl 127.0.0.1:11434/api/ps` | ручная |
| Ollama выключена | ручная: остановить ollama, отправить сообщение, проверить страницу | ручная |

## Verification

```bash
GRADLE_USER_HOME=/tmp/opencode/gradle-home ./gradlew test
openspec validate --strict --all
../java-ai-agent-dev_launcher/run.sh
curl -s 127.0.0.1:11434/api/ps
```

Браузер: спросить «ты олама?», сравнить ответ; повтор при остановленной Ollama.

## Constraints

- `build.gradle` не меняется; общение с моделью остаётся через локальный OpenCode API, зависимость `spring.ai`/Ollama-клиент не добавляется.
- Адрес Ollama — только `http://127.0.0.1:11434`, без ключей и секретов; пароль OpenCode по-прежнему только в лаунчер-конфиге.
- Правки существующих тестов запрещены; новый тест — отдельный файл.
- Существующий `opencode.json` с MCP IntelliJ сохраняется; провайдер не дублируется вручную, если OpenCode уже его видит.
- PR ≤ 300 строк.

## Out of scope

Загрузка моделей, выбор модели на странице, замеры токенов/скорости, RAG, замена OpenCode на прямой клиент Ollama, изменение прав агентов.
