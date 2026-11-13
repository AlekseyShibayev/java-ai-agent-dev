# Wiki

Краткий справочник по сущностям AI-агента в OpenCode V2.

| Сущность | Что это | Где задаётся |
| --- | --- | --- |
| Агенты | профили: `build`, `plan` (`primary`), `general`, `explore` (`subagent`); роль определяет `mode` | `.opencode/agents/`, `~/.config/opencode/agents/`, `agents` в конфиге |
| Skills | переиспользуемые инструкции и файлы к ним | `.opencode/skills/<id>/SKILL.md` (или `<id>.md`), `~/.config/opencode/skills/`, `.claude/skills`, `.agents/skills`, массив `skills` |
| Instructions | постоянные правила для агента | `AGENTS.md`: глобальный `~/.config/opencode/AGENTS.md`, проектный и вложенные; массив `instructions` — файлы, glob, URL |
| References | именованный внешний контекст: локальные каталоги и Git-репозитории | `references` в конфиге: `path` или `repository`/`branch`, плюс `description`, `hidden` |
| Commands | промпты-шаблоны, вызываются как `/name` | `.opencode/commands/<name>.md`, `~/.config/opencode/commands/`, `commands` в конфиге |
| MCP | внешние инструменты, ресурсы и промпты | `mcp.servers` в конфиге: `type: local` + `command` или `type: remote` + `url`; отключить — `disabled: true`; промпты — `/server:prompt` |
| Plugins | код-расширения: tools, hooks, integrations, commands, agents | `plugins` в конфиге, `.opencode/plugins/` (`.ts`, `.js`, каталоги-пакеты), `~/.config/opencode/plugins/` |
| Permissions | `allow` / `ask` / `deny` по action и resource; без совпадения — `ask`; действия V2 — `shell`, `subagent` | `permissions` в конфиге и у агента; правила агента дописываются после глобальных |
| Policies | жёсткий `deny` поверх permissions, только ужесточает | `policies` в конфиге; глобальные и системные важнее проектных |
| Models | модель и провайдер; `small_model` (устар.) — для служебных задач | `model` и `providers` в конфиге; формат `provider/model#variant`: корневой `model` без варианта, вариант — у агента и команды |
| Tools | встроенные, MCP и плагинные инструменты | доступны через Code Mode (`execute`), MCP — `<server>_<tool>`, ограничение — `permissions` (ключ `tools` устарел) |

`/etc/opencode/` — системный уровень, работает как глобальный.

Подробнее о ролях — [role.md](role.md).
Стартовый чек-лист — [agent-setup.md](agent-setup.md).
Рекомендации — [recommendations.md](recommendations.md).
