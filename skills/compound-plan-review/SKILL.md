---
name: compound-plan-review
description: Reviews an implementation plan before work begins, using parallel reviewers to catch issues. Use when the user says "review the plan", "check the plan", or after compound-plan creates a plan document.
---

# Compound Plan Review

Get feedback on an implementation plan from multiple perspectives before committing to implementation.

## When to Use

- After compound-plan creates a plan document in `docs/plans/`
- Before starting compound-work on a complex feature
- Queue item: `plan-review` in `tasks/dashboard.md`

## Workflow

### Phase 1: Find the Plan

1. Read `tasks/dashboard.md` — find the active task row and its Plan link (`P` column)
2. Open the linked `docs/plans/YYYY-MM-DD-HH-MM-slug-plan.md`
3. If multiple pending plan-review items in Queue, ask user which one to review
4. Read the plan completely

### Phase 2: Launch Plan Reviewers (3 agents in parallel)

**Task 1: Simplicity Reviewer**

```
Task (subagent_type="general-purpose", readonly=true):
description: "Review plan for simplicity"
prompt: "You are a Simplicity Reviewer. Find unnecessary complexity in this implementation plan.

Identify:
1. OVER-ENGINEERING: Abstractions not needed for the stated goals
2. YAGNI VIOLATIONS: Things planned 'for the future' that should be deferred
3. SIMPLER ALTERNATIVES: Where a simpler approach works just as well
4. UNNECESSARY STEPS: Steps that could be combined or eliminated
5. SCOPE CREEP: Requirements beyond what was asked

For each finding: What | Why it matters | Simpler alternative

Also note what's GOOD about the plan.

PLAN:
[plan content]"
```

**Task 2: Technical Feasibility Reviewer**

```
Task (subagent_type="general-purpose", readonly=true):
description: "Review plan feasibility"
prompt: "You are a Technical Feasibility Reviewer. Find things that won't work or will cause problems.

Identify:
1. MISSING EDGE CASES: Scenarios not covered
2. TECHNICAL RISKS: Things that might not work as described
3. DEPENDENCY ISSUES: Missing deps, version conflicts
4. ORDERING PROBLEMS: Steps in wrong order or with hidden dependencies
5. MISSING ERROR HANDLING: Failure scenarios not addressed
6. ACCEPTANCE GAPS: Acceptance criteria that are vague or untestable

For each finding: What | Risk level (High/Medium/Low) | Suggestion

PLAN:
[plan content]"
```

**Task 3: Codebase Alignment Reviewer**

```
Task (subagent_type="explore", readonly=true):
description: "Check plan alignment with codebase"
prompt: "Review this implementation plan against the actual codebase:

1. Do the file paths mentioned in the plan actually exist?
2. Do the patterns described match what's in the codebase?
3. Are there existing utilities/components the plan should reuse?
4. Does the plan follow the project's established conventions from AGENTS.md?
5. Are there similar implementations to reference?

For each finding: What | Evidence (actual files) | Suggestion

PLAN:
[plan content]"
```

### Phase 3: Synthesize Feedback

```markdown
## Plan Review Summary

**Plan:** `docs/plans/YYYY-MM-DD-HH-MM-slug-plan.md`
**Verdict:** [READY TO IMPLEMENT | NEEDS REVISION | MAJOR REWORK NEEDED]

### Simplicity Feedback
- [findings]
- Positive: [what's good]

### Feasibility Feedback
- [findings with risk level]
- Positive: [what's solid]

### Codebase Alignment
- [findings]
- Positive: [what aligns well]

### Recommended Changes
1. [Most important]
2. [Second]
3. [Third]
```

### Phase 4: Write Result File

Write review to `docs/plan-reviews/YYYY-MM-DD-HH-MM-slug-plan-review.md` (same slug as the plan, current timestamp).

Update `tasks/dashboard.md`:
- Set `PR` column for this task to ✅ (or ❌ if MAJOR REWORK)
- Mark Queue item as `[x]`
- If READY: add `- [ ] work | slug: \`slug\` | created: YYYY-MM-DD-HH-MM` to Queue
- If NEEDS REVISION: add `- [ ] revise-plan | slug: \`slug\` | created: YYYY-MM-DD-HH-MM` to Queue

### Phase 5: Apply Feedback

Use AskQuestion:

**Question:** "How would you like to proceed with the plan review feedback?"

**Options:**
1. **Apply all suggestions** — Update plan with all recommended changes
2. **Apply selected** — Choose which suggestions to incorporate
3. **Discuss** — Talk through before deciding
4. **Ignore and proceed** — Start compound-work as-is

If applying changes: update plan doc, then update Queue item to `work`.

## Tips

- Most valuable for complex features (5+ steps)
- For simple tasks, skip and go straight to compound-work
- If major issues found, consider going back to compound-brainstorm
