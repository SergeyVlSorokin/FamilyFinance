---
name: compound-report
description: Generates a human-readable progress report for clients or stakeholders. Reads tasks/dashboard.md and tasks/history/ to summarize completed, in-progress, and planned work. Use when the user says "generate report", "what did we do", "sprint summary", "report for client". Triggers on Russian: "сгенери отчёт", "отчёт для заказчика", "что сделали", "итоги спринта", "прогресс", "покажи статус задач", "compound-report".
---

# Compound Report

Generate a progress report from the task registry and history archive.
Reads only lightweight sources — does NOT open full brainstorm/plan documents.

## When to Use

- User asks "generate report", "sprint summary", "what did we do this week"
- End of sprint or milestone
- Client or stakeholder needs a progress update
- Queue item: `report` in `tasks/dashboard.md`

## Workflow

### Phase 1: Gather Data (minimal reads)

1. Read `tasks/dashboard.md` — get Active Tasks table + Sprint info
2. List files in `tasks/history/` — get names only (no content)
3. Optionally: read specific history files if detail is requested

### Phase 2: Choose Report Format

Use AskQuestion:

**Question:** "What type of report do you need?"

**Options:**
1. **Sprint Summary** — completed + in-progress tasks, work volume
2. **Status Update** — current state of active tasks only
3. **Detailed** — sprint summary + key decisions per completed task (reads history files)
4. **Client-ready** — clean prose format, no technical jargon

### Phase 3: Generate Report

#### Sprint Summary (default)

```markdown
# Progress Report: [Sprint dates or "as of YYYY-MM-DD"]

## Completed ([N] tasks)
- ✅ [Task name] — [1-line description] `[archived: YYYY-MM-DD]`
- ✅ ...

## In Progress ([N] tasks)
| Task | Phase | Notes |
|------|-------|-------|
| [slug] | [phase] | [any blocked status] |

## Planned / Backlog
- ⏳ [Task name] — [phase: Brainstorm/Plan]

## Work Volume
- Tasks completed: N
- Plans written: N
- Code reviews passed: N
- Lessons documented: N (lines in tasks/lessons.md)

## Next Steps
[1-3 bullet points based on Queue items in dashboard]
```

#### Detailed Report

Same as Sprint Summary, but for each completed task also include:
- Key decisions (from history file `## Key Decisions` section)
- Commits (from history file `## Commits` section)

Read history files only for this mode.

#### Client-Ready Report

Rewrite Sprint Summary in clean prose, removing technical terms:
- No file paths
- No branch names
- Replace "compound-work" → "implementation"
- Replace "code review" → "quality check"
- Focus on outcomes, not process

### Phase 4: Output

Print the report in the conversation.

Offer to save it:
```
- [ ] Save as `docs/reports/YYYY-MM-DD-HH-MM-sprint-report.md`?
```

## Tips

- Keep report generation token-cheap: read only dashboard + history filenames by default
- For Detailed mode: read at most 5 most recent history files
- Client-ready mode is for external sharing — strip all internal tooling references
