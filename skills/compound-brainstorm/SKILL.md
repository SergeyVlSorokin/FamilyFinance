---
name: compound-brainstorm
description: Explores feature requirements through collaborative dialogue before planning. Use when starting a new feature, discussing ideas, or when the user says "brainstorm", "let's think through", or describes a vague feature idea. Triggers on Russian: "новая фича", "новый функционал", "хочу добавить", "есть идея", "давай обсудим", "брейнсторм", "мозговой штурм", "что если", "как лучше реализовать".
---

# Compound Brainstorm

Explore and refine feature ideas through structured dialogue before committing to implementation. Brainstorming answers WHAT to build. Planning (compound-plan) answers HOW to build it.

**Note: The current year is 2026.** Use this when dating brainstorm documents.

## When to Use

- User describes a feature idea (vague or detailed)
- Starting a new significant piece of work
- Requirements are unclear or have multiple valid approaches
- User explicitly asks to brainstorm
- User says "brainstorm", "let's think through", "let's explore"

## Workflow

### Phase 0: Assess Requirements Clarity

Before jumping into brainstorming, evaluate whether it's actually needed.

**Clear requirements indicators:**
- Specific acceptance criteria provided
- Referenced existing patterns to follow
- Described exact expected behavior
- Constrained, well-defined scope
- User has done this type of work before

**If requirements are already clear:**

Use AskQuestion to suggest:

**Question:** "Your requirements seem detailed enough to proceed directly to planning. What would you prefer?"

**Options:**
1. **Skip to planning** -- Go straight to compound-plan
2. **Brainstorm anyway** -- Explore the idea further, surface edge cases
3. **Quick brainstorm** -- 2-3 key questions, then move to planning

If user chooses "Skip to planning", hand off to compound-plan skill immediately.

### Phase 1: Understand the Idea

#### 1.1 Repository Research (Lightweight)

Run a quick repo scan to understand existing patterns relevant to the idea:

```
Task (subagent_type="explore", readonly=true):
description: "Quick codebase scan for brainstorm"
prompt: "Quickly explore this codebase to understand existing patterns related to: [feature description].

Focus on:
1. Similar features or implementations that already exist
2. Established patterns and conventions
3. Technology stack and constraints
4. File structure conventions

Return: Brief summary of relevant patterns, similar features, and constraints found.
Keep it concise -- this is for brainstorming context, not deep analysis."
```

#### 1.2 Collaborative Dialogue

Use AskQuestion to ask questions **ONE AT A TIME**. Don't overwhelm with multiple questions.

**Question Techniques:**

1. **Start broad, then narrow:**
   - First: "What's the main goal?" / "Who is this for?"
   - Then: "How should X behave when Y?" / "What's the priority?"
   - Finally: "Should we handle edge case Z?" / "What about error states?"

2. **Prefer multiple choice when natural options exist:**
   ```
   Question: "How should we handle user authentication?"
   Options:
   - Session-based (server-side)
   - JWT tokens (client-side)
   - OAuth only (third-party)
   - Let me think about it
   ```

3. **Surface non-obvious edge cases:**
   - "What happens when the data is empty?"
   - "How should this behave on mobile?"
   - "What if the API is slow or unavailable?"
   - "Should this work offline?"

4. **Validate assumptions explicitly:**
   - "I'm assuming X because of Y. Is that correct?"
   - "Based on the codebase, it looks like you use Z pattern. Should we follow that?"

5. **Ask about success criteria:**
   - "How will we know this is done?"
   - "What's the minimum viable version?"

**YAGNI Principle:** Apply "You Aren't Gonna Need It" throughout. If the user mentions future features:
- Acknowledge them
- Suggest keeping them out of scope for now
- Document them as "Future Considerations" in the brainstorm doc
- Focus on what's needed NOW

**Exit condition:** Continue until the idea is clear OR user says "proceed" / "that's enough" / "let's plan".

### Phase 2: Explore Approaches

Based on research and conversation, present **2-3 concrete approaches**.

For each approach, provide:

```markdown
## Approach A: [Name] (Recommended)
**Description:** [2-3 sentences]
**Pros:**
- Pro 1
- Pro 2
**Cons:**
- Con 1
- Con 2
**Best when:** [scenario where this shines]
**Complexity:** [Low | Medium | High]

## Approach B: [Name]
**Description:** [2-3 sentences]
**Pros:**
- Pro 1
- Pro 2
**Cons:**
- Con 1
- Con 2
**Best when:** [scenario where this shines]
**Complexity:** [Low | Medium | High]
```

**Lead with your recommendation** and explain why. Apply YAGNI -- prefer the simpler solution unless there's a strong reason for complexity.

Use AskQuestion to ask which approach the user prefers.

### Phase 3: Capture the Design

Create a new file `docs/brainstorms/YYYY-MM-DD-HH-MM-slug-brainstorm.md`
(current timestamp; slug = short kebab-case feature name).

```markdown
# Brainstorm: {Feature Name}

**Date:** YYYY-MM-DD-HH-MM
**Status:** Planning

## Stakeholder Notes
_(PMs or customers can add context here before brainstorm starts.)_

## What We're Building
[1-2 paragraph summary of the feature and its purpose]

## Key Decisions Made
- **[Decision 1]**: [What we chose] — [Why we chose it]
- **[Decision 2]**: [What we chose] — [Why we chose it]

## Chosen Approach
[Selected approach with reasoning]
*(Why This Approach / Why Not Alternative)*

## Scope
**In Scope:**
- Item 1

**Out of Scope:**
- Item X (reason: YAGNI)

## Open Questions
- [ ] Question 1
```

Keep sections concise: 200-300 words per section max.

Then update `tasks/dashboard.md`:
1. Add a row to the Active Tasks table. In the B column, write a markdown link to the brainstorm file
   (text: ✅, href: `docs/brainstorms/YYYY-MM-DD-HH-MM-slug-brainstorm.md`).
2. Add Queue item:
   `- [ ] plan | slug: \`YYYY-MM-DD-HH-MM-slug\` | created: YYYY-MM-DD-HH-MM`

### Phase 4: Handoff

Use AskQuestion to present next steps:

**Question:** "Brainstorm captured! What would you like to do next?"

**Options:**
1. **Proceed to planning** -- Use compound-plan skill
2. **Refine further** -- Continue exploring the idea
3. **Done for now** -- Save and return later

**Display summary:**

```
Brainstorm: docs/brainstorms/YYYY-MM-DD-HH-MM-slug-brainstorm.md

Key decisions:
- [Decision 1]
- [Decision 2]

Chosen approach: [Approach Name]
Dashboard updated. Queue: plan pending.
```

## Important Guidelines

- **Stay focused on WHAT, not HOW** -- Implementation details belong in the plan
- **Ask one question at a time** -- Don't overwhelm the user
- **Apply YAGNI aggressively** -- Prefer simpler approaches, defer future features
- **Keep outputs concise** -- 200-300 words per section max
- **Reference existing code** -- Ground suggestions in the actual codebase
- **If user is impatient:** Explain "10 minutes of brainstorming saves hours of rework"

**NEVER CODE in this phase!** Just explore and document decisions.
