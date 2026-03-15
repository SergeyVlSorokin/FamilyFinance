---
name: compound-commit
description: Commit and push current changes with a semantic message based on active task phase. Use when the user says "commit", "push", "коммит", "пуш", "закоммить", "сохрани изменения", "commit and push", "save changes". Triggers on Russian: "коммит", "пуш", "закоммить", "сохрани", "запушь", "скоммить".
---

# Compound Commit

Lightweight commit + push with a context-aware message derived from the active task. Minimal token footprint — reads only `git status` and the Active Tasks table.

## Token Budget

**Read ONLY:**
1. `git status --short` + `git log --oneline -5` (Bash)
2. First 20 lines of `tasks/dashboard.md` (Active Tasks table only)

**Do NOT open** any task docs (brainstorms, plans, reviews, etc.).

## Workflow

### Step 1: Read context (2 tool calls max)

```bash
git status --short
git log --oneline -5
```

Read `tasks/dashboard.md` with `limit: 20` to see active task slug + phase.

### Step 2: Stage files

Stage only relevant files — never `git add .` blindly. Use `git status` output to decide what to stage.

Prefer specific files:
```bash
git add path/to/file1 path/to/file2
```

If all changes are clearly related to the active task:
```bash
git add -A
```

### Step 3: Commit

Generate message from active task context:

**Format:** `type(scope): description`

**Type from Phase:**
| Phase | Type |
|-------|------|
| Brainstorm / Plan / Plan-Review | `docs` |
| Work (in progress) | `feat` / `fix` / `refactor` (pick from changes) |
| Review / Compound | `chore` |
| Result-Log / Done | `chore` |

**Scope:** kebab slug of active task (shortened if long)

**Examples:**
```
feat(memory-installable): implement housekeeping script
chore(memory-installable): close compound phase, advance to result-log
docs(auth-system): capture brainstorm decisions
```

Commit with:
```bash
git commit -m "$(cat <<'EOF'
type(scope): description

Co-Authored-By: Claude Sonnet 4.6 <noreply@anthropic.com>
EOF
)"
```

### Step 4: Push

If user said "commit and push" or "коммит и пуш" — push immediately.

Otherwise ask:

```
Committed. Push to remote?
[p] push now   [s] skip
```

```bash
git push
```

If no upstream set:
```bash
git push -u origin HEAD
```

### Step 5: Report

One-line summary:
```
✓ Committed: type(scope): description
✓ Pushed to origin/branch-name
```

## Key Principles

- **Minimal context** — never read task docs, only git status + dashboard header
- **Specific staging** — prefer named files over `git add .`
- **Honest message** — if unsure of scope, use the slug from dashboard as-is
- **No force push** — if push fails, report the error and stop
