---
name: compound-work
description: Executes implementation plans with task tracking and incremental commits. Use when ready to implement, after planning, or when the user says "start working", "implement this", or "let's build it". Triggers on Russian: "начинай работу", "погнали кодить", "реализуй", "пиши код", "выполни план", "начни имплементацию", "кодируй".
---

# Compound Work

Execute implementation plans systematically with progress tracking, incremental commits, and quality checks.

## When to Use

- After creating an implementation plan (compound-plan)
- User is ready to start coding
- Plan exists in `docs/plans/` (linked from `tasks/dashboard.md` P column)
- User says "start working", "implement this", "let's build it"

## Workflow

### Phase 1: Quick Start

#### 1.1 Find and Read the Plan

1. Read `tasks/dashboard.md` — find the active task row and its Plan link (`P` column)
2. Open the linked `docs/plans/YYYY-MM-DD-HH-MM-slug-plan.md` and read it completely
3. Review any referenced files or links in the plan
4. If anything is unclear or ambiguous, ask clarifying questions NOW
5. Get user approval to proceed

**Do not skip clarification.** Better to ask questions now than build the wrong thing.

#### 1.2 Setup Environment

First, check the current branch:

```bash
git branch --show-current
```

**If already on a feature branch** (not main/master/dev):
- Use AskQuestion: "Continue working on `[current_branch]`, or create a new branch?"
- If continuing, proceed to Phase 2

**If on main/master/dev**, create a feature branch:

```bash
git pull origin dev
git checkout -b feat/{feature-slug}
```

Use a meaningful name based on the work:
- `feat/hero-animation`
- `fix/hydration-issue`
- `refactor/i18n-system`

**Important:** Never commit directly to main/dev without explicit user permission.

### Phase 2: Create Task List and Parse Waves

Use TodoWrite to break the plan into actionable tasks. Extract steps from the plan document.

**Wave-aware execution (v2.4+):** If the plan uses structured task format with `→ depends:` annotations:

1. Parse `→ depends: TN, TM` from each task header (e.g., `### T3: Build component → depends: T1, T2`)
2. Group tasks into waves — tasks with no unmet dependencies belong to the same wave
3. Display wave plan before starting:
   ```
   Wave 1: T1, T2 (independent)
   Wave 2: T3, T4 (depend on Wave 1)
   Wave 3: T5 (depends on Wave 2)
   ```
4. Execute tasks wave by wave. Within a wave, execute sequentially but commit once after the wave completes (unless wave has only 1 task — commit after that task).

**Structured plans without `→ depends:` annotations (v2.4+):** Treat tasks as a single wave executed in plan order. Use `**Files:**` for pre-wave research when present and run auto-verify from `**Verify:**` fields. Do NOT fall back to legacy mode just because dependencies are omitted.

**Legacy plan fallback:** If the plan uses flat checklists without `→ depends:`, `**Files:**`, or `**Verify:**` fields:
- Execute tasks sequentially (current behavior)
- Read files as encountered (no pre-wave research)
- Skip auto-verify
- Commit after each logical unit as before

**Pre-wave research (v2.4+ plans only):** Before starting a wave, if tasks have `**Files:**` fields, launch an Explore subagent to pre-read all listed files for the wave's tasks. This keeps the main context focused on writing, not reading. Skip if no `**Files:**` fields present.

```
TodoWrite:
- Step 1 from plan [pending]
- Step 2 from plan [pending]
- Step 3 from plan [pending]
- ...
- Run quality checks [pending]
- Update plan status [pending]
```

### Phase 3: Execute Steps

For each task (in wave order if waves are defined, otherwise in plan order):

#### 3.1 Task Execution Loop

```
while (tasks remain):
  1. Mark task as in_progress via TodoWrite
  2. Read files listed in **Files:** field (or referenced files from plan)
  3. Look for similar patterns in codebase (grep, explore)
  4. Implement following existing conventions
  5. Run linter on changed files
  6. Mark task as completed via TodoWrite
  7. Mark off the corresponding checkbox in the plan file ([ ] -> [x])
  8. Auto-verify (see 3.8) if **Verify:** field exists
  9. Evaluate for incremental commit (see 3.4)
```

**IMPORTANT:** Always check off completed items in the plan file. Use the appropriate tool to change `- [ ]` to `- [x]` for each task you finish.

#### 3.2 Follow Existing Patterns

- The plan should reference similar code -- read those files first
- Match naming conventions from `AGENTS.md` exactly
- Reuse existing utilities and components before creating new ones
- When in doubt, search for similar implementations in the codebase

#### 3.3 Stack Conventions

Read `AGENTS.md` for project-specific conventions (build commands, framework rules, naming patterns).
Apply them here. Do not invent conventions that aren't documented.

#### 3.4 Incremental Commit Strategy

**Wave-aware commits (v2.4+ plans):** Commit after each wave completes (not after each individual task), unless the wave has only 1 task. This keeps commits meaningful and aligned with dependency boundaries.

**Legacy plans:** After completing each task, evaluate whether to commit:

| Commit when... | Don't commit when... |
|----------------|---------------------|
| Logical unit complete (component, page, section) | Small part of a larger unit |
| Lints pass and meaningful progress made | Lints failing |
| About to switch contexts (layout to animation) | Purely scaffolding with no behavior |
| About to attempt risky/uncertain changes | Would need a "WIP" commit message |
| Before making potentially breaking changes | Debug code or temporary hacks |

#### 3.5 Commit Workflow

```bash
# 1. Stage only files related to this logical unit (NOT git add .)
git add path/to/changed/files

# 2. Check what's being committed
git status
git diff --staged

# 3. Commit with conventional message
git commit -m "feat(scope): description of this unit"
```

#### 3.6 Conventional Commit Format

```
type(scope): short description
```

**Types:** `feat`, `fix`, `refactor`, `style`, `docs`, `test`, `chore`

**Scope:** The area of the codebase (`hero`, `i18n`, `nav`, `stig`, `museum`, `layout`, `animation`)

**Examples:**
- `feat(museum): add guided tours section with image gallery`
- `fix(hero): resolve hydration mismatch in entry sequence`
- `refactor(i18n): consolidate translation data structure`

#### 3.7 Communicate Progress

After each completed task, briefly communicate:
- "Completed step 2/5: Added the OfferingsGrid component. Starting step 3."

#### 3.8 Auto-Verify (v2.4+ plans)

After completing a task's steps, check if the task has a `**Verify:**` field in the plan:

- **If verify is a fenced code block** (wrapped in backticks: `` `command here` `` or ` ```command``` `) → run it via Bash, check exit code. Verify commands should be read-only checks (grep, diff, cat, test runners). Never execute commands containing `rm`, `sudo`, `dd`, `mkfs`, or `curl|sh` patterns.
- **If verify is plain text** (no backticks, e.g., "Component renders correctly") → evaluate against current state and report pass/fail with reasoning
- **If no Verify field** → skip, proceed as before

**On verify failure:** Stop immediately. Report the failure with details. Use AskQuestion:
- "Fix and retry" — fix the issue, re-run verify
- "Skip this verify" — mark as skipped, continue to next task
- "Abort wave" — stop execution, return to user

**Never auto-retry.** Always surface the failure and let the user decide.

### Phase 4: Quality Checks

Before considering work complete:

**Pre-submission Quality Checklist:**

- [ ] All TodoWrite tasks marked completed
- [ ] All plan checkboxes checked off (`[x]`)
- [ ] Build passes (see `AGENTS.md` for BUILD_CMD)
- [ ] Lint passes (see `AGENTS.md` for LINT_CMD)
- [ ] Code follows existing patterns (compare with similar files)
- [ ] No debug/temporary code left in
- [ ] No commented-out code blocks
- [ ] All acceptance criteria from the plan are met

**Run quality commands from `AGENTS.md`:**

```bash
# See AGENTS.md → Stack Conventions for project-specific commands
```

If issues found:
1. Fix immediately
2. Commit fixes separately with `fix(scope): ...` format

### Phase 5: Update Plan and Dashboard

After all implementation is complete:

1. Update plan file `docs/plans/...`: change `status: completed`, verify all acceptance criteria checked
2. Update `tasks/dashboard.md`: set `W` column to ✅ for this task
3. Mark Queue `work` item as `[x]`
4. Add Queue item: `- [ ] code-review | branch: \`{branch}\` | created: YYYY-MM-DD-HH-MM`

### Phase 6: Handoff

Use AskQuestion to present next steps:

**Question:** "Implementation complete! What would you like to do next?"

**Options:**
1. **Review code** -- Use compound-review skill for multi-agent code review
2. **Create PR** -- Push branch and create pull request
3. **Commit remaining** -- Stage and commit any uncommitted changes
4. **Document learnings** -- Use compound-compound to capture what was learned

### Start Fast, Execute Faster
- Get clarification once at the start, then execute
- The goal is to FINISH the feature, not create perfect process

### Ship Complete Features
- Mark all tasks completed before moving on
- Don't leave features 80% done

## Common Pitfalls to Avoid

- **Analysis paralysis** -- Don't overthink, read the plan and execute
- **Ignoring plan references** -- The plan has file paths for a reason
- **Testing at the end** -- Test continuously
- **Giant commits** -- Keep commits atomic and focused
- **Committing on dev/main** -- Always use a feature branch
- **Inventing conventions** -- Check `AGENTS.md` before deciding on patterns
