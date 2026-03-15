# AGENTS.md snippet — вставь в свой AGENTS.md

Скопируй секции ниже и адаптируй под свой стек.

---

## Skill Trigger Keywords

When you hear these words/phrases — invoke the corresponding skill IMMEDIATELY, before doing anything else:

| Heard | Invoke |
|-------|--------|
| "новая фича", "новый функционал", "есть идея", "хочу добавить", "брейнсторм", "мозговой штурм", "давай обсудим", "brainstorm" | `compound-brainstorm` |
| "составь план", "сделай план", "план", "распланируй", "как реализовать", "plan" | `compound-plan` |
| "ревью плана", "проверь план", "plan review" | `compound-plan-review` |
| "начинай работу", "погнали", "реализуй", "кодируй", "пиши код", "compound-work", "начни имплементацию" | `compound-work` |
| "сделай ревью", "проверь код", "ревью", "review", "можно мержить" | `compound-review` |
| "зафикси урок", "запомни", "задокументируй", "compound compound", "добавь в lessons" | `compound-compound` |
| "заархивируй", "закрой таск", "таск готов", "result-log" | `compound-result-log` |
| "отчёт", "что сделали", "итоги спринта", "report", "compound-report" | `compound-report` |
| "просто сделай", "быстро", "lfg", "мелкий фикс", "quick fix" | `compound-lfg` |

## Skills

| Skill | When to use | Output |
|-------|-------------|--------|
| `compound-brainstorm` | Старт фичи, обсуждение идей | `docs/brainstorms/YYYY-MM-DD-HH-MM-slug-brainstorm.md` |
| `compound-plan` | Планирование по браузерму | `docs/plans/YYYY-MM-DD-HH-MM-slug-plan.md` |
| `compound-plan-review` | Ревью плана перед реализацией | `docs/plan-reviews/YYYY-MM-DD-HH-MM-slug-plan-review.md` |
| `compound-work` | Реализация по плану | коммиты в ветку |
| `compound-review` | Ревью кода после реализации | `docs/reviews/YYYY-MM-DD-HH-MM-slug-code-review.md` |
| `compound-compound` | Фиксация паттернов (Quick mode дефолт) | `tasks/lessons.md` |
| `compound-result-log` | Архивирование завершённого таска | `tasks/history/YYYY-MM-DD-HH-MM-slug.md` |
| `compound-report` | Отчёт для заказчика / Sprint summary | markdown в диалоге |
| `compound-lfg` | Быстрая задача без церемоний | коммиты в ветку |

**Default workflow:** brainstorm → plan → plan-review → work → review → compound → result-log

**Dashboard:** `tasks/dashboard.md` — реестр всех активных задач + Queue триггеры
**Slug format:** `YYYY-MM-DD-HH-MM-kebab-name`

## Stack Conventions (for compound-work)

<!-- Заполни под свой стек: -->
- **BUILD_CMD:** `./gradlew assembleDebug`
- **LINT_CMD:** `./gradlew lint`
- **BASE_BRANCH:** `main` <!-- или dev -->

## Workflow Orchestration

- Always enter Planning Mode (`compound-plan`) for ANY non-trivial task (3+ steps).
- Read `tasks/lessons.md` before starting work.
- After resolving an issue, append a rule to `tasks/lessons.md` via `compound-compound`.
