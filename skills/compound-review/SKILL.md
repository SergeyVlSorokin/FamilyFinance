---
name: compound-review
description: Multi-perspective code review using parallel review agents. Use when code is ready for review, before merging, or when the user says "review", "check this code", or "is this ready to merge". Triggers on Russian: "сделай ревью", "проверь код", "code review", "можно мержить", "проверь перед мерджем", "ревью кода".
---

# Compound Review

Run multi-perspective code review with specialized agents analyzing different aspects in parallel.

## When to Use

- After implementing a feature (compound-work)
- Before creating a PR or merging
- User wants thorough code review
- Checking code quality before shipping

## Workflow

### Phase 0: Detect Project Type → Choose Agents File

Before launching agents, check what kind of project this is:

```bash
ls roles/ ansible.cfg site.yml 2>/dev/null
```

**If any of these exist** → DevOps/Ansible project → use [`agents-devops.md`](agents-devops.md):
- 4 agents: Secrets Sentinel, Idempotency Guardian, Blast Radius Analyst, Ansible Best Practices
- All run with `model="sonnet"`
- Review verdict: `SAFE TO APPLY | FIX REQUIRED | NEEDS DISCUSSION`

**Otherwise** → Code project → use [`agents.md`](agents.md):
- Up to 8 agents in 2 batches (see below)
- Mixed opus/sonnet/haiku tiers
- Review verdict: `SAFE TO MERGE | FIX REQUIRED | NEEDS DISCUSSION`

### Phase 1: Determine Review Scope

Identify what to review:

1. **Changed files**: Run `git diff --name-only main` or `git diff --name-only HEAD~N`
2. **Specific files**: User-specified files
3. **Entire feature**: All files in a directory

Get the actual diff for review:

```bash
git diff main          # All changes vs main branch
git diff HEAD~N        # Last N commits
git diff --staged      # Staged changes only
```

**Scope assessment**: Count changed files and diff size.
- Small change (1-3 files, <200 lines): Quick Review Mode
- Medium change (4-10 files, <500 lines): Standard Review (Batch 1)
- Large change (10+ files, 500+ lines): Full Review (Batch 1 + Batch 2)

### Phase 2: Launch Parallel Review Agents

Read agent prompts from [agents.md](agents.md). Replace `{diff}` with the actual diff output. Run agents in batches of 4.

**Batch 1 (always run -- 4 agents in parallel):**

```
Task 1 (subagent_type="general-purpose", model="opus", readonly=true):
  description: "Security review"
  prompt: [Security Sentinel prompt from agents.md with {diff} replaced]

Task 2 (subagent_type="general-purpose", model="sonnet", readonly=true):
  description: "Performance review"
  prompt: [Performance Oracle prompt from agents.md with {diff} replaced]

Task 3 (subagent_type="general-purpose", model="opus", readonly=true):
  description: "Architecture review"
  prompt: [Architecture Strategist prompt from agents.md with {diff} replaced]

Task 4 (subagent_type="general-purpose", model="sonnet", readonly=true):
  description: "Simplicity review"
  prompt: [Code Simplicity Reviewer prompt from agents.md with {diff} replaced]
```

**Batch 2 (for medium/large changes -- 4 agents in parallel):**

```
Task 5 (subagent_type="general-purpose", model="haiku", readonly=true):
  description: "Pattern review"
  prompt: [Pattern Recognition Specialist prompt from agents.md with {diff} replaced]

Task 6 (subagent_type="general-purpose", model="sonnet", readonly=true):
  description: "Language/Framework review"
  prompt: [TypeScript and Next.js Reviewer prompt from agents.md with {diff} replaced]

Task 7 (subagent_type="general-purpose", model="haiku", readonly=true):
  description: "Accessibility review"
  prompt: [Accessibility and UX Reviewer prompt from agents.md with {diff} replaced]

Task 8 (subagent_type="general-purpose", model="haiku", readonly=true):
  description: "Agent-native review"
  prompt: [Agent-Native Reviewer prompt from agents.md with {diff} replaced]
```

**Model selection rationale (3-tier):**
- **opus**: Security Sentinel, Architecture Strategist — missing vulnerabilities or architectural issues is expensive; these need the strongest analytical model
- **sonnet**: Performance Oracle, Simplicity Reviewer, Language/Framework — analytical tasks requiring judgement
- **haiku**: Pattern Recognition, Accessibility, Agent-Native — checklist-style compliance checks

### Phase 3: Collect and Synthesize Findings

After all agents complete, consolidate results:

1. **Collect** all findings from every agent
2. **Deduplicate** overlapping findings (same file/line from multiple agents)
3. **Categorize** by severity using this classification:

| Severity | Description | Action | Examples |
|----------|-------------|--------|----------|
| **P1 Critical** | Security vulnerabilities, data loss, crashes, broken functionality | MUST fix before merge | XSS, hardcoded secrets, missing auth, crashes |
| **P2 Important** | Performance issues, missing error handling, architectural problems | SHOULD fix before merge | Missing validation, tight coupling |
| **P3 Nice-to-have** | Style improvements, minor optimizations, suggestions | CONSIDER fixing | Naming, minor refactors, documentation gaps |

4. **Estimate effort** for each finding: Small (< 30 min), Medium (30 min - 2 hours), Large (> 2 hours)

### Phase 4: Create Review Summary

Present a structured review report to the user:

```markdown
## Code Review Summary

**Review Target:** [branch name or PR description]
**Files Reviewed:** [count]
**Review Depth:** [Quick | Standard | Full]

### Verdict: [SAFE TO MERGE | FIX REQUIRED | NEEDS DISCUSSION]

Criteria:
- SAFE TO MERGE: No P1, 0-2 minor P2 findings
- FIX REQUIRED: Any P1, or 3+ P2 findings
- NEEDS DISCUSSION: Architectural concerns that need team input

---

### P1 Critical ([count] findings) -- BLOCKS MERGE

- [ ] **[Agent Name]** `file:line` - Issue description
  - Risk: [what could go wrong]
  - Fix: [recommended solution]
  - Effort: [Small | Medium | Large]

### P2 Important ([count] findings) -- SHOULD FIX

- [ ] **[Agent Name]** `file:line` - Issue description
  - Recommendation: [what to do]
  - Effort: [Small | Medium | Large]

### P3 Nice-to-have ([count] findings) -- CONSIDER

- [ ] **[Agent Name]** `file:line` - Issue description

### What's Working Well

- [Positive observation 1 from agents]
- [Positive observation 2 from agents]

### Agents Used

- Security Sentinel
- Performance Oracle
- Architecture Strategist
- Code Simplicity Reviewer
- [additional agents if Batch 2 was run]
```

### Phase 5: Address Findings

Use AskQuestion to present options:

**Question:** "How would you like to address the review findings?"

**Options:**
1. **Fix all P1/P2 issues** - Automatically fix all critical and important findings
2. **Fix specific issues** - Choose which findings to address (list by number)
3. **Discuss findings** - Talk through findings before deciding
4. **Skip to merge** - Proceed without fixes (NOT recommended if P1 exists)

**If fixing:**
1. Address P1 findings first, then P2
2. Make changes for each finding
3. Commit with conventional format: `fix(review): address [agent name] feedback - [brief description]`
4. Re-run only the affected agent to verify fix if the change is complex

### Phase 5.5: Optional UAT Checkpoint

After review findings are synthesized and addressed, offer the user a chance to verify key deliverables before finalizing.

**Plan availability guard:** This phase is best for compound workflow reviews that have a plan in the dashboard `P` column. For ad-hoc/quick reviews with no plan, skip UAT by default OR ask the user to provide a plan path before continuing.

Use AskQuestion:

**Question:** "Code review complete. Would you like to verify key deliverables before finalizing the review?"

**Options:**
1. **No — proceed to write review** (Recommended) — skip UAT, write review file
2. **Yes — walk me through acceptance criteria** — run UAT checklist

**If user selects UAT:**

1. Locate the plan:
   - Compound task: read the plan from dashboard `P` column link
   - Ad-hoc review: ask user for a plan path; if none is available, skip UAT and proceed to Phase 6
2. Extract items from the plan's `## Acceptance Criteria` section
3. For each criterion, use AskQuestion: "Pass" / "Fail" / "Skip"
4. If Fail → launch an Explore subagent to diagnose the issue, then suggest a fix
5. Collect all results (pass/fail/skip with notes) for embedding in the review file

**If user skips UAT:** Proceed directly to Phase 6.

### Phase 6: Write Result and Update Dashboard

Write the review report to `docs/reviews/YYYY-MM-DD-HH-MM-slug-code-review.md` (current timestamp).

**If UAT was run in Phase 5.5**, append a `## UAT Results` section to the review file:

```markdown
## UAT Results

| Criterion | Result | Notes |
|-----------|--------|-------|
| Criterion 1 | Pass | — |
| Criterion 2 | Fail | [description of failure + fix applied] |
| Criterion 3 | Skip | [reason] |
```

Update `tasks/dashboard.md`:
- Set `R` column for this task to a markdown link to the review file (✅ or ❌)
- Mark Queue `code-review` item as `[x]`
- Add Queue item: `- [ ] compound | slug: \`slug\` | created: YYYY-MM-DD-HH-MM`

### Phase 7: Handoff

Use AskQuestion after review is complete:

**Question:** "Review complete. What would you like to do next?"

**Options:**
1. **Fix remaining issues** - Address any unfixed findings
2. **Document learnings** - Use compound-compound skill to capture insights from this review
3. **Create PR** - Push branch and create pull request
4. **Merge** - Proceed with merge (only if verdict is SAFE TO MERGE)

**Important:** If the review uncovered recurring patterns or non-obvious lessons, actively suggest using compound-compound to document those learnings. This is how knowledge compounds.

## Quick Review Mode

For small changes (hotfixes, typos, config changes), run an abbreviated single-agent review:

```
Task (subagent_type="general-purpose", readonly=true):
"You are a senior code reviewer doing a quick review. Check for:
1. Security issues (secrets, injection, auth bypass)
2. Obvious bugs or logic errors
3. Missing error handling
4. Breaking changes

Be concise. Only report actual issues, not style preferences.
Rate each finding as P1 (critical), P2 (important), or P3 (minor).

Changes to review:
[git diff output]"
```

## Protected Artifacts

The following paths are compound-engineering pipeline artifacts and must NEVER be flagged for deletion or removal by any review agent:

- `tasks/dashboard.md` -- Active task registry
- `tasks/history/` -- Historical records
- `tasks/lessons.md` -- Project learnings
- `docs/brainstorms/` -- Brainstorm documents
- `docs/plans/` -- Plan documents
- `docs/plan-reviews/` -- Plan review documents
- `docs/reviews/` -- Code review documents

If a review agent flags any file in these directories, discard that finding during synthesis.

## Tips

- Run full review for features, quick review for hotfixes
- P1 findings should ALWAYS block merge
- If agents disagree on severity, escalate to P1 (err on the side of caution)
- Document recurring findings in compound-compound -- this is how you compound
- Don't run review on WIP code -- finish implementation first
- For very large diffs, split the diff by file groups and run focused reviews
