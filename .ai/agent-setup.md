# AI Agent Setup

Чек-лист задач, которые нужно выполнить, когда начинаешь работать с AI-агентом.
Дополнять по мере появления новых задач.

## 1. Глобальный навык skill-creator

- [x] Скачан из `anthropics/skills`: <https://github.com/anthropics/skills/tree/main/skills/skill-creator>
- [x] Установлен глобально: `~/.config/opencode/skills/skill-creator/`
- [x] Проектная копия версионируется в `.opencode/skills/skill-creator/`
- [ ] Проверить в новой сессии: навык виден и загружается

Установка на новой машине:

```bash
mkdir -p ~/.config/opencode/skills
git clone --depth 1 https://github.com/anthropics/skills /tmp/anthropic-skills
cp -r /tmp/anthropic-skills/skills/skill-creator ~/.config/opencode/skills/
rm -rf /tmp/anthropic-skills
```

Проверка:

```bash
ls ~/.config/opencode/skills/skill-creator/SKILL.md
```

## 2. MCP с IntelliJ

- [x] В `opencode.json` добавлен сервер `intellij`
- [x] В IDE включён MCP Server: `Ctrl+Alt+S` → **AI → MCP Server** → **Enable MCP Server** → Apply
- [x] `opencode mcp list` показывает `intellij connected`
- [x] В новой сессии доступны инструменты IDE (подключено 40 tools)
- [ ] В IDE открыт проект по актуальному пути `java-ai-agent-dev` (после переименования IDE всё ещё держит старый путь)

Конфигурация в `opencode.json`:

```json
{
  "mcp": {
    "servers": {
      "intellij": {
        "type": "remote",
        "url": "http://127.0.0.1:64342/stream"
      }
    }
  }
}
```

Важно: OpenCode не поддерживает SSE-транспорт напрямую — на `http://127.0.0.1:64342/sse` он получает `HTTP 405`.
Нужен **HTTP Stream** endpoint (`/stream`), его же показывает кнопка «Copy HTTP Stream Config» в настройках IDE.

Проверка:

```bash
/home/user/.opencode/bin/opencode mcp list
```

Если запущено несколько IDE, порт и URL берутся из настроек MCP Server в конкретной IDE.

## Как добавлять задачи

- Новый пункт — отдельный раздел с чек-листом.
- Для каждой задачи: зачем, команды, проверка, статус.
- Секреты и токены в файл не записывать: репозиторий публичный.
