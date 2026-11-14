# Tasks

## 1. Тесты (red)

- [x] 1.1 Red `ChatMarkupTest`: фрагмент ответа содержит крючки для reasoning, карточек инструментов и usage.
- [x] 1.2 Red `SseBridgeTest`: вызов, результат и ошибка инструмента приходят как `tool.called`/`tool.result`/`tool.failed`, расход — как `usage.step` и `usage.session`.
- [x] 1.3 Red E2E `ChatToolsE2ETest`: карточка инструмента (имя, вход, результат), ошибка без падения прогона.
- [x] 1.4 Red E2E: reasoning свёрнут, дельты дописываются без замены узла.
- [x] 1.5 Red E2E: стоимость `$0.000058` и токены после завершения; abort без ложной ошибки.

## 2. Реализация (green)

- [x] 2.1 `RunEventNames`: раздельные имена `tool.started`, `tool.called`, `tool.result`, `tool.failed`, `usage.step`, `usage.session`.
- [x] 2.2 `answer.html`: контейнер шагов, свёрнутый reasoning, карточки инструментов, блок стоимости.
- [x] 2.3 `chat.js`: обработчики `tool.*`, `reasoning`, `usage.*`; текст через `textContent`, фильтр по `callId`, формат стоимости.
- [x] 2.4 `chat.css`: карточки, reasoning, ошибка, usage; компактность на мобильном.

## 3. Проверка

- [x] 3.1 `./gradlew test` без фильтра зелёный; `./gradlew e2e` зелёный.
- [x] 3.2 `openspec validate --strict --all` зелёный.
- [x] 3.3 Merge ветки `task/005-chat-tools` в `main`, статус `на ревью`.

## Workflow follow-up

- Ревью отдельным агентом; при успехе — архив change и перенос файла задачи в `openspec/changes/archive/`.
