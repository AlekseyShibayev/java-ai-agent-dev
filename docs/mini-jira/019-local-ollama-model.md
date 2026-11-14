---
id: "019"
title: "Чат по умолчанию на локальной модели Ollama"
created: 2026-11-14
branch: task/019-local-ollama-model
pr:
---

## Что сделать

Переключить дефолты на локальную Ollama (`qwen3.5:4b-q4_K_M`) и привести `docs/plan.md` и `README.md` к факту.

## Приёмка

- [ ] `opencode.provider` и `opencode.model` по умолчанию — `ollama` и `qwen3.5:4b-q4_K_M`; переопределение через `OPENCODE_PROVIDER`/`OPENCODE_MODEL` сохраняется.
- [ ] Сообщение в `/chat` обслуживает локальная модель: во время прогона `GET 127.0.0.1:11434/api/ps` показывает загруженную `qwen3.5:4b-q4_K_M`, обращений к `opencode-go` за прогон нет.
- [ ] На вопрос «ты олама?» ответ говорит о локальной модели/Qwen, а не о DeepSeek и не о роли агента OpenCode.
- [ ] Ollama остановлена → прогон завершается ошибкой на странице (не 500 и не вечный busy); после запуска Ollama следующий прогон успешен.
- [ ] `docs/plan.md` и `README.md` называют фактическую локальную модель и адрес Ollama; строка `qwen2.5:3b-instruct` убрана.
- [ ] `./gradlew test` без фильтра и `openspec validate --strict --all` зелёные.
