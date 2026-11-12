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
- [ ] В IDE включён MCP Server: `Ctrl+Alt+S` → **AI → MCP Server** → **Enable MCP Server** → Apply
- [ ] `opencode mcp list` показывает `intellij connected`
- [ ] В новой сессии доступны инструменты IDE (build, problems, debug, search)

Конфигурация в `opencode.json`:

```json
{
  "mcp": {
    "servers": {
      "intellij": {
        "type": "local",
        "command": ["npx", "-y", "@jetbrains/mcp-proxy"]
      }
    }
  }
}
```

Проверка:

```bash
/home/user/.opencode/bin/opencode mcp list
```

Если запущено несколько IDE, порт задаётся явно:

```json
"environment": { "IDE_PORT": "63342" }
```

Прокси ищет IDE на портах `63342–63352` и проверяет `/api/mcp/list_tools`.
Пока MCP Server в IDE выключен, эндпоинт отвечает `404`, а сервер остаётся `pending`.

## Как добавлять задачи

- Новый пункт — отдельный раздел с чек-листом.
- Для каждой задачи: зачем, команды, проверка, статус.
- Секреты и токены в файл не записывать: репозиторий публичный.
