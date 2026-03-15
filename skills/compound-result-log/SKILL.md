---
name: compound-result-log
description: Archives a completed task and clears it from the dashboard. Use when a task is fully done (code merged, review passed) or when the user says "archive this task", "log result", "close the task". Triggers on Russian: "заархивируй", "закрой таск", "таск готов", "убери в историю", "логируй результат", "result-log", "задача завершена".
---

# Compound Result Log

Archive a completed task from `tasks/dashboard.md` into `tasks/history/`.

## When to Use

- Task is fully done: code merged, review passed, lessons documented
- Queue item: `result-log` in `tasks/dashboard.md`
- User says "archive this task", "log result", "close the task"

## Statuses

| Status | Meaning |
|--------|---------|
| `planned` | Plan exists, work not started |
| `in_progress` | Work underway, partial progress |
| `implemented` | Code done, review/checks pending |
| `reviewed` | Code review passed |
| `done` | Fully closed, ready to archive |

## Workflow

### 1. Read Dashboard

Open `tasks/dashboard.md`. Find the task row to archive.
Follow the links to collect available artifacts:
- `B` link → `docs/brainstorms/...`
- `P` link → `docs/plans/...`
- `PR` link → `docs/plan-reviews/...`
- `R` link → `docs/reviews/...`

Read only the files that exist (some phases may have been skipped).

### 2. Determine Status

If status is not `done`, update `tasks/dashboard.md` to reflect actual current status and stop.
Only archive when status is `done`.

### 3. Archive (status = done)

Create `tasks/history/YYYY-MM-DD-HH-MM-slug.md` (current timestamp):

```markdown
# [Task Name] — Archived YYYY-MM-DD

## Summary
[2-3 sentence description of what was built and why]

## Artifacts
- Brainstorm: `docs/brainstorms/YYYY-MM-DD-HH-MM-slug-brainstorm.md`
- Plan: `docs/plans/YYYY-MM-DD-HH-MM-slug-plan.md`
- Plan Review: `docs/plan-reviews/...` (if exists)
- Code Review: `docs/reviews/...` (if exists)
- Branch / PR: [branch name or PR link]

## Key Decisions
[2-3 bullet points from brainstorm/plan]

## Commits
[short hash + message list, if available via git log]

## Verification
- Build: PASS/FAIL
- Tests: PASS/FAIL
- Manual checks: PASS/FAIL
```

Keep the archive file as a summary + links. Do NOT copy full plan or brainstorm content.

### 3.5 Append to CHANGELOG.md

After creating the archive file, add an entry to `CHANGELOG.md` (project root).

1. **If `CHANGELOG.md` does not exist** — create it with the header:
   ```markdown
   # Changelog

   All notable changes documented automatically by the Compound workflow.
   ```

2. **Determine change type** from the first commit prefix in the archive's `## Commits` section:
   - `feat` → **Added**
   - `fix` → **Fixed**
   - `refactor` → **Changed**
   - `docs` → **Documentation**
   - anything else → **Changed**

3. **Insert a new entry** directly after the header block (newest first). Format:

   ```markdown
   ---

   ### YYYY-MM-DD — [Task Name]

   **Type:** Added | Fixed | Changed | Documentation
   **Summary:** [First 1-2 sentences from ## Summary]
   **Branch:** [branch name or "direct edit"]
   **Slug:** `YYYY-MM-DD-HH-MM-slug`
   ```

4. Write the updated `CHANGELOG.md`.

### 4. Update Dashboard

Remove the archived task row from the Active Tasks table.
Mark Queue item as `[x]`.
Increment "Completed this sprint" counter.

### 5. Sync Lessons

If `compound-compound` hasn't run yet for this task, suggest doing it first.
Lessons may be lost from context after archiving.

## Rules

- Never archive while task is `in_progress` or `implemented`
- Archive file = summary + artifact links only (token-efficient)
- After archive: `docs/brainstorms/`, `docs/plans/`, etc. files stay as-is (don't delete)
