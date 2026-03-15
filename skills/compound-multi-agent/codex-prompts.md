# Промпт-шаблоны для Agent 2 (Codex)

Готовые шаблоны задач для Codex. Копируй нужный шаблон, замени плейсхолдеры `{...}`, отправь как задачу.

Плейсхолдеры `{PROJECT_NAME}`, `{STACK}`, `{BUILD_CMD}` заполни один раз из `AGENTS.md` своего проекта.

---

## PLAN_REVIEW — Ревью плана

```
Ты — ревьюер планов реализации. Найди проблемы в плане ДО написания кода.

## Контекст проекта

Репозиторий: {PROJECT_NAME}
Стек: {STACK}
Ветка: `{branch_name}`

## Что ревьюировать

Прочитай план: `{plan_file_path}`
Прочитай brainstorm: `{brainstorm_file_path}`
Прочитай AGENTS.md для понимания конвенций.

## Фокус ревью

### 1. Архитектурное соответствие
- Plan следует паттернам, описанным в AGENTS.md
- Нет нарушений принятых конвенций
- Затронутые файлы существуют и указаны корректно

### 2. Техническая реализуемость
- Все зависимости уже установлены или явно добавлены в план
- Порядок шагов корректен (нет скрытых зависимостей)
- Edge cases покрыты

### 3. Простота (YAGNI)
- Нет over-engineering
- Нет "про запас" абстракций
- Scope соответствует задаче

### 4. Стек-специфичные проверки
- {STACK_SPECIFIC_CHECKLIST}
(Заполни из AGENTS.md своего проекта)

## Формат ответа

Напиши в `docs/plan-reviews/{date}-{slug}-plan-review.md`:

```markdown
# Plan Review: {feature_name}

**Reviewer:** Agent 2 (Codex)
**Plan:** `{plan_file_path}`

## Verdict: [READY TO IMPLEMENT | NEEDS REVISION | MAJOR REWORK NEEDED]

### P1 Critical — блокирует работу
- [ ] ...

### P2 Important — исправить в плане
- [ ] ...

### P3 Suggestions
- [ ] ...

### What's Good
- ...
```
```

---

## CODE_REVIEW — Ревью кода

```
Ты — код-ревьюер. Проведи ревью изменений.

## Контекст

Репозиторий: {PROJECT_NAME}
Стек: {STACK}
Ветка: `{branch_name}`
Plan: `{plan_file_path}`

```bash
git diff {base_branch} -- {changed_files}
```

## Чеклист

### Корректность
- [ ] Логических ошибок нет
- [ ] Edge cases обработаны
- [ ] Error handling есть там где нужно

### Безопасность
- [ ] Нет инъекций (SQL, command, XSS)
- [ ] Нет хардкодированных секретов
- [ ] Авторизация проверяется там где нужно

### Качество кода
- [ ] Следует конвенциям из AGENTS.md
- [ ] Нет debug/временного кода
- [ ] Нет дублирования существующих утилит

### Стек-специфичные проверки
- {STACK_SPECIFIC_CHECKLIST}
(Заполни из AGENTS.md своего проекта)

## Формат ответа

Напиши в `docs/reviews/{date}-{slug}-code-review.md`:

```markdown
# Code Review: {feature_name}

**Reviewer:** Agent 2 (Codex)
**Branch:** `{branch_name}`

## Verdict: [SAFE TO MERGE | FIX REQUIRED | NEEDS DISCUSSION]

### P1 Critical — BLOCKS MERGE
- [ ] `file:line` — проблема
  **Fix:** решение

### P2 Important — SHOULD FIX
- [ ] `file:line` — проблема

### P3 Nice-to-have
- [ ] `file:line` — предложение

### What's Working Well
- ...
```
```

---

## VERIFY — Верификация (build)

```
Проведи верификацию ветки: build и lint.

Ветка: `{branch_name}`

## Что запустить

```bash
{BUILD_CMD}
{LINT_CMD}
```

## Формат ответа

Допиши в review-документ:

```markdown
### Verification Run

| Check | Result | Details |
|-------|--------|---------|
| build | PASS/FAIL | {details} |
| lint  | PASS/FAIL | {errors} |
```

Если FAIL — опиши ошибку и предложи фикс.
```

---

## QUICK_REVIEW — Быстрое ревью

```
Быстрое ревью мелких изменений.

Ветка: `{branch_name}`

```bash
git diff {base_branch} -- {changed_files}
```

Проверь:
1. Security: secrets, injection, auth bypass
2. Bugs: логические ошибки, null reference, crashes
3. Breaking changes: API, types, interfaces
4. Стек-специфично: {STACK_SPECIFIC_CHECKLIST}

Формат: только P1/P2 находки. Если чисто — "No issues found".
Результат в `docs/reviews/{date}-{slug}-quick-review.md`.
```

---

## Плейсхолдеры

| Плейсхолдер | Откуда брать | Пример |
|-------------|-------------|--------|
| `{PROJECT_NAME}` | AGENTS.md | `uni-scrape` |
| `{STACK}` | AGENTS.md | `Python 3.9+, asyncio, aiohttp` |
| `{BUILD_CMD}` | AGENTS.md → Stack Conventions | `python3 -m pytest` |
| `{LINT_CMD}` | AGENTS.md → Stack Conventions | `ruff check src/` |
| `{STACK_SPECIFIC_CHECKLIST}` | AGENTS.md | framework-specific rules |
| `{branch_name}` | `git branch` | `feat/rag-query-server` |
| `{base_branch}` | AGENTS.md | `main` / `dev` |
| `{plan_file_path}` | tasks/dashboard.md | `docs/plans/2026-02-23-18-09-slug-plan.md` |
| `{brainstorm_file_path}` | tasks/dashboard.md | `docs/brainstorms/2026-02-23-18-00-slug-brainstorm.md` |
| `{date}` | `date +%Y-%m-%d-%H-%M` | `2026-02-24-09-01` |
| `{slug}` | из имени задачи | `rag-query-server` |
| `{changed_files}` | `git diff --name-only` | `src/scraper/core.py src/scraper/engine.py` |
