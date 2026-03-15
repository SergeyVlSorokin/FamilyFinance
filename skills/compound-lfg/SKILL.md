---
name: compound-lfg
description: Quick plan-and-execute for small, well-defined tasks. Use when the user says "lfg", "just do it", "quick fix", or describes a small clearly-scoped task that doesn't need full brainstorming or planning. Triggers on Russian: "просто сделай", "быстро исправь", "lfg", "мелкая задача", "быстрый фикс", "без церемоний", "сразу делай".
---

# Compound LFG (Let's Go)

Rapid plan-and-execute for small, well-defined tasks. Combines a lightweight plan with immediate execution -- no brainstorm document, no separate plan file. Just get it done.

## When to Use

- Small, clearly-scoped task (< 2 hours of work)
- User says "lfg", "just do it", "quick fix", "just make it work"
- Requirements are obvious and don't need brainstorming
- Single-concern change (one component, one bug, one config change)

## When NOT to Use (escalate to full workflow)

- Task touches 5+ files
- Architectural decisions are needed
- Multiple valid approaches with significant trade-offs
- Security-sensitive changes
- New feature with unclear requirements

## Workflow

### Step 1: Quick Assessment (30 seconds)

Read the task description and quickly determine:
- What files need to change?
- Is there an existing pattern to follow?
- Are there any risks?

If the task is bigger than expected, suggest: "This looks like it needs a proper plan. Want me to use compound-plan instead?"

### Step 2: Rapid Research

Run a single quick exploration:

```
Task (subagent_type="explore", readonly=true):
description: "Quick codebase scan"
prompt: "Quickly find files and patterns related to: [task description].
Return: relevant file paths, existing patterns to follow, and any gotchas.
Be fast and concise."
```

### Step 3: Create Minimal Task List

Use TodoWrite with just the essential steps:

```
TodoWrite:
- Implement the change [pending]
- Verify it works [pending]
```

### Step 4: Execute

1. Mark task as in_progress
2. Implement the change, following existing patterns
3. Run lints on changed files
4. Mark as completed

### Step 5: Commit

```bash
git add [changed files]
git commit -m "type(scope): description"
```

### Step 6: Quick Handoff

Report what was done in 2-3 sentences. Offer:

```
Done! Options:
1. Quick review -- fast single-agent review
2. Done -- move on
3. Document -- use compound-compound if something interesting was learned
```

## Key Principle

**Speed over process.** LFG is for when the task is clear and small. Don't add ceremony where it isn't needed. But if you discover the task is bigger than expected mid-execution, stop and escalate to the full workflow.
