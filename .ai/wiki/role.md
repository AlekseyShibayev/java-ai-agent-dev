# Роли

**Из коробки:**
- `build` (primary) — код, правки, команды.
- `plan` (primary) — исследование и план без правок.
- `general` (subagent) — многошаговые задачи; не запускает субагентов.
- `explore` (subagent) — поиск и чтение без правок.
- Скрытые: `compaction`, `title`, `summary`.

**Свои роли:** файл `.opencode/agents/<path>.md` (проект) или `~/.config/opencode/agents/<path>.md` (глобально), либо `agents.<id>` в `opencode.json`.

ID — путь без `.md` (`team/reviewer.md` → `team/reviewer`). Кириллица работает, латиница удобнее. Встроенные ID не переименовать. Каталог `agent/` тоже подхватывается; документированный — `agents/`.
