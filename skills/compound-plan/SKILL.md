---
name: compound-plan
description: Creates detailed implementation plans with parallel research. Use when ready to plan a feature, after brainstorming, or when the user says "plan", "create a plan", or "how should we implement this". Triggers on Russian: "составь план", "сделай план", "распланируй", "как реализовать", "напиши спецификацию", "составь чеклист".
---

# Compound Plan

Transform feature ideas into actionable implementation plans through parallel research.

**Note: The current year is 2026.** Use this when dating plan documents and searching for documentation.

## When to Use

- After completing a brainstorm (check `docs/brainstorms/` first)
- User has clear requirements and wants a plan
- Before starting significant implementation work
- User says "plan", "create a plan", "how should we implement this"

## Workflow

### Phase 0: Idea Refinement

**Check for brainstorm output first:**

Read `tasks/dashboard.md` to find the active task row. Follow the `B` link to the brainstorm file in `docs/brainstorms/`.

**If a brainstorm file exists:**
1. Read it — extract key decisions, chosen approach, and open questions
2. Skip idea refinement questions below -- the brainstorm already answered WHAT to build
3. Use brainstorm decisions as input to the research phase

**If no brainstorm found, run idea refinement:**

Use AskQuestion tool to refine the idea through dialogue:
- Ask questions one at a time to understand the idea fully
- Prefer multiple choice questions when natural options exist
- Focus on: purpose, constraints, and success criteria
- Continue until the idea is clear OR user says "proceed"

**Gather signals for research decision.** During refinement, note:
- User's familiarity: Do they know the codebase patterns? Pointing to examples?
- Topic risk: Security, payments, external APIs warrant more caution
- Uncertainty level: Is the approach clear or open-ended?

**Skip option:** If the feature description is already detailed, offer:
"Your description is clear. Should I proceed with research, or would you like to refine it further?"

### Phase 0.5: Implementation Preferences

**Run when a brainstorm exists** (Phase 0 found a brainstorm file). If no brainstorm, ask at most 2 preference questions from the applicable category during Phase 0 dialogue instead.

Determine the feature type and ask 2-4 targeted AskQuestion questions to capture implementation preferences before research begins:

**Visual feature** (UI component, page, layout):
- Layout density: compact / spacious / adaptive?
- Empty states: placeholder text / illustration / CTA?
- Mobile vs desktop: same layout / responsive breakpoints / separate views?
- Animation: none / subtle transitions / rich motion?

**API / CLI feature** (endpoint, command, integration):
- Response format: JSON / JSON:API / GraphQL?
- Error handling style: HTTP status codes only / error objects / detailed messages?
- Pagination: offset / cursor / none?

**Content feature** (blog, docs, copy):
- Tone: formal / casual / technical?
- Structure: flat / hierarchical / tagged?
- Length constraints: short-form / long-form / configurable?

**Infrastructure feature** (deploy, CI/CD, config):
- Rollback strategy: manual / automated / blue-green?
- Monitoring: logs only / metrics / alerts?
- Failure mode: fail-fast / graceful degradation?

Record answers in the plan under `## Implementation Preferences`. These preferences guide research agents and plan construction.

### Phase 1: Local Research (Always Runs -- Parallel)

Run these research agents in parallel to gather local context:

**Task 1: Repo Research Analyst**

```
Task (subagent_type="explore", readonly=true):
description: "Analyze codebase for feature"
prompt: "You are a Repo Research Analyst. Analyze this codebase to understand how to implement: [feature description].

Your mission:
1. Find similar patterns and implementations in the codebase
2. Identify files that will need changes (list exact paths)
3. Map dependencies and data flow for the affected area
4. Note existing conventions from AGENTS.md
5. Check package.json for relevant dependencies already installed
6. Look at the project structure (src/app/, src/components/, src/lib/) for patterns

Return a structured report:
- RELEVANT FILES: List of files with brief description of relevance
- PATTERNS FOUND: Existing patterns to follow (with file references)
- CONVENTIONS: Any rules or conventions from AGENTS.md
- DEPENDENCIES: Relevant installed packages
- SUGGESTED APPROACH: How to implement based on existing patterns
- GOTCHAS: Potential conflicts or issues to watch for"
```

**Task 2: Learnings Researcher**

```
Task (subagent_type="explore", readonly=true):
description: "Search past solutions and lessons"
prompt: "You are a Learnings Researcher. Search the tasks/lessons.md file and tasks/history/ directory for any documented solutions, patterns, or learnings that relate to: [feature description].

Your mission:
1. Search tasks/lessons.md for immediate project rules and constraints
2. Search tasks/history/ for past projects that solved similar problems
3. Look for documented gotchas, patterns to follow, mistakes to avoid
4. Look for prevention strategies that apply

Return:
- RELEVANT LESSONS: List of rules from tasks/lessons.md that apply
- PAST IMPLEMENTATIONS: Relevant past tasks from tasks/history/
- PATTERNS TO FOLLOW: Documented patterns that should be applied
- MISTAKES TO AVOID: Past gotchas that are relevant

If nothing is found, state that and move on."
```

### Phase 1.5: Research Decision

Based on signals from Phase 0 and findings from Phase 1, decide on external research.

**High-risk topics -- always research:** Security, payments, external APIs, data privacy, new libraries. The cost of missing something is too high.

**Strong local context -- skip external research:** Codebase has good patterns, user knows what they want. External research adds little value.

**Uncertainty or unfamiliar territory -- research:** User is exploring, codebase has no examples, new technology.

Announce the decision briefly and proceed.

### Phase 1.5b: External Research (Conditional)

**Only run if Phase 1.5 indicates external research is valuable.**

Run up to 2 research agents in parallel:

**Task 3: Best Practices Researcher**

```
Task (subagent_type="general-purpose", readonly=true):
description: "Research best practices"
prompt: "You are a Best Practices Researcher. Research current best practices for: [feature description].

Use WebSearch to find:
1. Current best practices and recommended approaches (search for 2026 content)
2. Common pitfalls and how to avoid them
3. Performance considerations
4. Security considerations

Return:
- BEST PRACTICES: Top 3-5 recommended practices with sources
- PITFALLS: Common mistakes to avoid
- PERFORMANCE: Key performance considerations
- SECURITY: Security aspects to consider
- REFERENCES: URLs to useful documentation"
```

**Task 4: Framework Docs Researcher**

```
Read AGENTS.md to identify the project stack (language, framework, key libraries).
Use WebSearch/WebFetch to find current documentation for the relevant framework/library.

Return:
- API REFERENCE: Relevant API methods/components
- IMPLEMENTATION PATTERNS: How the framework recommends doing this
- VERSION NOTES: Any version-specific considerations
- CODE EXAMPLES: Relevant examples from the docs
```

### Phase 2: Consolidate Research

After all research steps complete, consolidate findings:

- Document relevant file paths from repo research
- Include relevant institutional learnings from `tasks/lessons.md` AND `tasks/history/`
- Note external documentation URLs and best practices (if external research was done)
- Capture conventions from `AGENTS.md`
- List relevant installed dependencies

**Check for Elegance:** Before planning, explicitly ask yourself: "Is there a more elegant, simpler way to achieve this? Are we over-engineering?" Make sure the plan reflects the simplest possible viable change.

**Optional validation:** Briefly summarize findings and ask if anything looks off or missing before proceeding to planning.

### Phase 3: Choose Detail Level

Use AskQuestion to let user choose:

**Question:** "How detailed should the plan be?"

**Options:**
1. **MINIMAL** -- High-level steps, key decisions only. Best for simple bugs, small improvements.
2. **STANDARD** -- Clear steps with implementation notes, file paths, and acceptance criteria. Best for most features.
3. **DETAILED** -- Step-by-step with code snippets, edge cases, and phased implementation. Best for major features, architectural changes.

### Phase 4: Construct the Plan

Create a new file `docs/plans/YYYY-MM-DD-HH-MM-slug-plan.md` (current timestamp, same slug as brainstorm).

```markdown
# Plan: {Feature Name}

**Date:** YYYY-MM-DD-HH-MM
**Brainstorm:** docs/brainstorms/YYYY-MM-DD-HH-MM-slug-brainstorm.md
**Status:** Draft

## Implementation Preferences
_(Only if Phase 0.5 was run. Record user's answers here.)_

## Acceptance Criteria
- [ ] Criterion 1
- [ ] Criterion 2

## Plan

### T1: Task name
**Files:** `path/to/file.ts`, `path/to/other.ts`
**Verify:** command to run or manual check description
**Done when:** acceptance statement for this task

- [ ] Step 1
- [ ] Step 2

### T2: Another task → depends: T1
**Files:** `path/to/another.ts`
**Verify:** description of how to verify
**Done when:** acceptance statement

- [ ] Step 1

## Execution Order
- **Wave 1:** T1 (independent)
- **Wave 2:** T2 (depends on T1)
```

**Structured task format rules:**
- Every task MUST have `**Files:**`, `**Verify:**`, `**Done when:**` fields
- `**Files:**` — exact paths from repo research that this task will create or modify
- `**Verify:**` — a shell command wrapped in backticks (`` `grep ...` ``) OR a plain-text manual check description. Backtick-wrapped = compound-work runs it via Bash; plain text = compound-work evaluates manually. Verify commands should be read-only checks, never destructive operations
- `**Done when:**` — plain-language acceptance statement
- Add `→ depends: TN, TM` to the task header when a task requires another task's output. Tasks without dependencies can run in any order
- End the plan with `## Execution Order` grouping tasks into waves based on dependency graph

Include "Context" or "Implementation Preferences" sections as needed. Reference specific file paths from research.

Then update `tasks/dashboard.md`:
1. Set `P` column for this task to a markdown link to the plan file
2. Mark Queue `plan` item as `[x]`
3. Add Queue item: `- [ ] plan-review | docs/plans/YYYY-MM-DD-HH-MM-slug-plan.md | created: YYYY-MM-DD-HH-MM`

### Phase 5: Post-Generation Options

Use AskQuestion to present next steps:

**Question:** "Plan written to `docs/plans/YYYY-MM-DD-HH-MM-slug-plan.md`. What would you like to do next?"

**Options:**
1. **Review plan** -- Run compound-plan-review for feedback from multiple reviewers
2. **Start work** -- Use compound-work to begin implementing
3. **Refine** -- Continue editing the plan
4. **Simplify** -- Reduce detail level

Loop back to options after Simplify or Refine until user selects work or review.

## Tips

- Plans should be living documents -- update as you learn during implementation
- Link to brainstorm document if one exists
- Include "Why" not just "What" for key decisions
- If plan gets too long (>500 lines), split into phases
- All checkboxes in the plan can be tracked by compound-work
- Use mermaid diagrams for complex data flows or architecture
- Reference specific file paths from the codebase research
- NEVER CODE in this phase! Just research and write the plan.
