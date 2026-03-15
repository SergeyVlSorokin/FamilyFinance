# Настройка Compound 2.2 в новом проекте

## Способ 1 (рекомендуется): через engram install.py

```bash
git clone git@github.com:you/engram.git
python3 engram/install.py --target /path/to/project          # только скиллы
python3 engram/install.py --target /path/to/project --memory # + memory system
```

Затем: добавь `templates/agents-snippet.md` в свой `AGENTS.md` и заполни `BUILD_CMD` / `LINT_CMD`.

---

## Способ 2: вручную

Используй эту инструкцию, если скиллы `skills/compound-*` уже перенесены вручную,
но инфраструктура ещё не создана.

---

## 1. Создать дерево каталогов

```bash
# tasks/ — верстак
mkdir -p tasks/history
touch tasks/dashboard.md tasks/lessons.md

# docs/ — артефакты по фазам
mkdir -p docs/brainstorms docs/plans docs/plan-reviews docs/reviews docs/agent-tasks
```

---

## 2. Наполнить tasks/dashboard.md

Скопируй шаблон из `skills/compound-setup-in-new-project.md` (секция ниже) или
запусти `compound-brainstorm` — он создаст первую строку в таблице сам.

```markdown
# Dashboard

## Active Tasks

| Slug | Phase | B | P | PR | W | R | Priority | Started |
|------|-------|:-:|:-:|:--:|:-:|:-:|----------|---------|
| _no active tasks_ | | | | | | | | |

## Queue (pending agent actions)

## Sprint

**Current:** _not set_
**Completed this sprint:** 0
```

---

## 3. Настроить автотриггер Queue (рекомендуется)

Создай `.claude/settings.json` в корне репозитория:

```json
{
  "hooks": {
    "PostToolUse": [
      {
        "matcher": "Write",
        "hooks": [
          {
            "type": "command",
            "command": "python3 scripts/queue_trigger.py"
          }
        ]
      }
    ]
  }
}
```

Скрипт `scripts/queue_trigger.py` уже в репозитории — копируй вместе со скиллами.

---

## 4. Установить субагентов

```bash
mkdir -p .claude/agents
cp skills/agents/*.md .claude/agents/
```

Субагенты — это тонкие делегаты, вызываемые через Task tool на дешёвой модели (haiku):
- `brainstorm-writer` — пишет brainstorm-doc (вызывается из compound-brainstorm)
- `result-log-agent` — архивирует задачи из dashboard (вызывается из compound-result-log)
- `sprint-reporter` — пишет sprint report из dashboard (вызов: "напиши отчёт")

---

## 5. Адаптировать AGENTS.md

Добавь в секцию Skills:

```markdown
| `compound-brainstorm` | Старт фичи → `docs/brainstorms/YYYY-MM-DD-HH-MM-slug-brainstorm.md` |
| `compound-plan`       | Планирование → `docs/plans/YYYY-MM-DD-HH-MM-slug-plan.md` |
| `compound-work`       | Реализация по плану |
| `compound-review`     | Ревью кода → `docs/reviews/YYYY-MM-DD-HH-MM-slug-code-review.md` |
| `compound-compound`   | Фиксация урока → `tasks/lessons.md` |
| `compound-result-log` | Архив → `tasks/history/YYYY-MM-DD-HH-MM-slug.md` |
| `compound-report`     | Отчёт заказчику из dashboard + history |
```

Добавь стек-специфичные константы для `compound-work`:

```markdown
## Stack Conventions (for compound-work)
- BUILD_CMD: `python3 -m pytest` / `pnpm build` / `go build ./...`
- LINT_CMD: `ruff check .` / `pnpm lint` / `golangci-lint run`
- Framework-specific rules (если есть)
```

---

## 6. Адаптировать ревью-агентов под стек (опционально)

Если нет Next.js/TypeScript — замени агента «TypeScript and Next.js Reviewer»
в `skills/compound-review/agents.md` на нейтрального Language Reviewer под свой стек.

---

## 7. Проверка

- [ ] `tasks/dashboard.md` создан
- [ ] `tasks/lessons.md` создан
- [ ] `docs/brainstorms/`, `docs/plans/`, `docs/plan-reviews/`, `docs/reviews/`, `docs/agent-tasks/` созданы
- [ ] `.claude/agents/` содержит `brainstorm-writer.md`, `result-log-agent.md`, `sprint-reporter.md`
- [ ] `.claude/settings.json` с PostToolUse хуком создан
- [ ] `AGENTS.md` обновлён (Skills + Stack Conventions)

---

## Workflow после настройки

```
compound-brainstorm
  → docs/brainstorms/YYYY-MM-DD-HH-MM-slug-brainstorm.md
  → dashboard: строка + Queue [plan pending]

compound-plan
  → docs/plans/YYYY-MM-DD-HH-MM-slug-plan.md
  → dashboard Queue [plan-review pending]

compound-plan-review  ← подбирает из Queue
  → docs/plan-reviews/YYYY-MM-DD-HH-MM-slug-plan-review.md
  → dashboard Queue [work pending]

compound-work
  → коммиты в ветку
  → dashboard Queue [code-review pending]

compound-review  ← подбирает из Queue
  → docs/reviews/YYYY-MM-DD-HH-MM-slug-code-review.md
  → dashboard Queue [compound pending]

compound-compound  ← подбирает из Queue
  → tasks/lessons.md
  → dashboard Queue [result-log pending]

compound-result-log
  → tasks/history/YYYY-MM-DD-HH-MM-slug.md
  → dashboard: строка помечается ✅, удаляется из Active

compound-report  ← в любой момент
  → Sprint Report для заказчика
```
