---
name: compound-compound
description: Documents solutions and learnings to make future work easier. Use after solving a problem, fixing a bug, completing a feature, or when the user says "document this", "save this learning", or "that worked". Triggers on Russian: "зафикси урок", "запомни это", "сохрани паттерн", "задокументируй", "compound compound", "добавь в lessons".
---

# Compound Compound

Capture solutions and learnings so each piece of work makes future work easier. This is the KEY step that makes knowledge compound.

## When to Use

- After successfully solving a problem
- User says "that worked", "it's fixed", "done", "document this", "save this learning"
- After completing a feature
- When discovering a pattern worth remembering
- After a code review with valuable feedback (compound-review)
- After debugging a non-trivial issue

## Philosophy

**Each unit of engineering work should make subsequent units easier -- not harder.**

Traditional development accumulates technical debt. Every feature adds complexity. Compound engineering inverts this by capturing knowledge:
- What problem we solved
- How we solved it
- Why this approach works
- What to avoid next time
- Rules that should be enforced automatically going forward

The feedback loop:

```
Build -> Test -> Find Issue -> Research -> Improve -> Document -> Validate -> Deploy
  ^                                                                           |
  +------------- knowledge compounds here ------------------------------------+
```

## Preconditions

Before running this workflow, verify:
- Problem has been solved (not in-progress)
- Solution has been verified working
- Non-trivial problem (not simple typo or obvious error)

## Default Mode: Quick Compound

**For 80% of cases** — use Quick Compound directly (see bottom of file).
Use Full Analysis only when explicitly requested ("detailed compound", "full analysis") or when the insight is complex enough to warrant it (e.g. discovered a systemic architectural issue).

## Full Analysis Workflow

### Phase 1: Detect Trigger

Look for signals that something was learned:
- Problem was solved after some debugging
- Non-obvious solution was discovered
- Pattern or anti-pattern was identified
- Code review revealed important insight
- User explicitly requests detailed documentation

### Phase 2: Parallel Analysis (3 Agents)

Launch parallel subagents to extract and structure learnings. Run in 2 batches due to the 4-agent parallel limit.

**3 agents in parallel:**

**Task 1: Solution Extractor**

```
Task (subagent_type="general-purpose", readonly=true):
description: "Extract working solution"
prompt: "Analyze the recent conversation and code changes. Extract:
1. What was the root cause of the problem?
2. What was the final working solution? (step-by-step)
3. What didn't work and why?
4. Any gotchas or edge cases?

Be concise. Include actual file paths and code snippets only where essential."
```

**Task 2: Prevention Strategist**

```
Task (subagent_type="general-purpose", readonly=true):
description: "Develop prevention strategies"
prompt: "Based on the problem just solved, answer:
1. How could this have been prevented?
2. Should a project rule be added to AGENTS.md or tasks/lessons.md?
3. What warning signs to watch for in future code reviews?

Return: prevention rule draft (1-2 sentences, actionable)."
```

**Task 3: Rule Writer**

```
Task (subagent_type="explore", readonly=true):
description: "Draft concise lesson + check for duplicates"
prompt: "Search tasks/lessons.md for existing rules related to: [brief description].

Then draft a concise rule to add:
- **[Pattern Name]**: [What to do or avoid]. [Brief explanation]. (Date: YYYY-MM-DD)

If a closely related rule exists, suggest updating it instead of adding a new one."
```

### Phase 3: Assemble and Write

After all agents complete:

1. **Update `tasks/lessons.md`** — append the generated rule under the correct category header.
2. **Update `tasks/dashboard.md`** — mark Queue `compound` item as `[x]`, add `result-log` to Queue.
3. If critical: update `AGENTS.md` with the new rule.

### Phase 5: Summary and Handoff

Present a summary to the user:

```markdown
## Compound Complete

**Lesson documented in:** tasks/lessons.md

**Key takeaway added:**
- [The new rule]

**What's next?**
1. Continue workflow -- proceed with next task
2. View generated rules -- inspect tasks/lessons.md
```

## Quick Compound (Default)

For most learnings — directly append a one-liner to `tasks/lessons.md`:

```
- **[Pattern Name]**: [What to do or avoid]. (Date: YYYY-MM-DD)
```

Then update `tasks/dashboard.md`: mark Queue `compound` item as `[x]`, add `result-log` to Queue.

## The Compounding Philosophy

This creates a compounding knowledge system:

1. First time you solve "image optimization" -> Research (30 min)
2. Document the solution -> `tasks/lessons.md` (2 min)
3. Next time similar issue occurs -> AI reads `lessons.md` and applies the rule (0 min)
4. Knowledge compounds -> Each session makes the next one smarter

**You give feedback once. The system learns. You never say it again.**

## Tips

- Document immediately while context is fresh
- Keep the lessons concise. TL;DR is key.
- Don't document obvious things -- focus on surprises
