---
name: compound-multi-agent
description: Координация работы нескольких AI-агентов в compound-workflow. Определяет роли, handoff-протокол и правила перекрёстного ревью между Agent 1 (Claude Code) и Agent 2 (Codex).
---

# Compound Multi-Agent

Координация мультиагентной работы в compound-pipeline.

## Агенты

| ID | Платформа | Режим | Роль |
|----|-----------|-------|------|
| **Agent 1** | Claude Code (CLI / VSCode) | Интерактивный, real-time | Основной имплементатор: brainstorm, plan, work, synthesis |
| **Agent 2** | Codex (OpenAI) | Async, sandbox | Ревьюер и верификатор: plan-review, code-review, build/test |

### Почему два агента

- **Complementary blind spots**: разные модели ловят разные классы багов.
- **Async pipeline**: Agent 2 не блокирует Agent 1.
- **Sandbox verification**: Agent 2 может запустить build в изолированном окружении.
- **Перекрёстное ревью**: двойная страховка, особенно для v0-сгенерированного кода.

## Распределение ролей

| Фаза | Agent 1 (Claude Code) | Agent 2 (Codex) |
|------|----------------------|-----------------|
| **compound-brainstorm** | Primary (интерактив с юзером) | — |
| **compound-plan** | Пишет план | — |
| **Plan Review** | Внутренние агенты (3 шт) | Ревью плана по шаблону `PLAN_REVIEW` |
| **compound-work** | Primary имплементатор | — |
| **Code Review** | Внутренние агенты (до 8 шт) | Ревью кода по шаблону `CODE_REVIEW` |
| **Verification** | — | Build по шаблону `VERIFY` |
| **compound-result-log** | Синтезирует результаты обоих агентов | — |

## Протокол handoff через `docs/agent-tasks/`

### Формат task-файла

```markdown
---
type: plan-review | code-review | verify | quick-review
status: pending | in-progress | done | skipped
agent: codex
created: YYYY-MM-DD
branch: feat/...
phase: N
phase_title: "Phase N — Description"
plan_file: docs/plans/YYYY-MM-DD-slug-plan.md
result: null
---

[Полный промпт — передаётся в Codex как есть]
```

### Naming convention

```
docs/agent-tasks/YYYY-MM-DD-<slug>-phase<N>-<type>.md
```

### Жизненный цикл task-файла

```
1. Agent 1 создаёт файл (status: pending)
2. Юзер копирует промпт → скармливает Agent 2
3. Agent 2 пишет результат → docs/reviews/
4. Agent 2 обновляет task: status: done, result: путь
5. Agent 1 читает результат → синтезирует → status: processed
```

### Автодетекция результатов Agent 2

При старте сессии Agent 1 сканирует `docs/agent-tasks/` на `status: done`.

**Статусы:**
- `pending` — ждёт Agent 2
- `in-progress` — Agent 2 работает
- `done` — Agent 2 закончил, Agent 1 не обработал
- `processed` — Agent 1 прочитал и применил
- `skipped` — отменено

## Когда запускать Agent 2

### Обязательно (MUST)

- **Code review** после каждой завершённой фазы
- **Verification** (build) перед мёрджем в dev/main

### Рекомендуется (SHOULD)

- **Plan review** для фаз с >3 файлами или новыми паттернами
- **Code review** для v0-сгенерированного кода >100 строк (ловит v0-артефакты)

### Не нужно (SKIP)

- Документационные изменения
- Однострочные хотфиксы
- Изменения только в конфигурации

## Синтез двойного ревью

```markdown
# Code Review: [feature] — Phase N

## Verdict: [SAFE TO MERGE | FIX REQUIRED | NEEDS DISCUSSION]

## Agent 1 (Claude Code) — Internal Review
[Результаты 4-8 внутренних review-агентов]

## Agent 2 (Codex) — External Review
[Результаты ревью Codex]

## Synthesis
- Совпадающие находки: [список]
- Уникальные Agent 1: [список]
- Уникальные Agent 2: [список]
- Разногласия: [если есть]

## Resolution
[Что пофикшено, что отложено, что эскалировано]
```

### Правила разрешения конфликтов

1. Оба нашли → подтверждённый баг, фиксить.
2. Один нашёл P1, другой пропустил → фиксить.
3. Расхождение P2/P3 → брать P2.
4. Разные фиксы → юзер решает.

## Шаблоны промптов для Agent 2

См. [codex-prompts.md](codex-prompts.md)
