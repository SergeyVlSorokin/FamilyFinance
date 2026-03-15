# Plan Review: Family Finance App

**Plan:** `docs/plans/2026-02-28-family-finance-plan.md`
**Brainstorm:** `docs/brainstorms/2026-02-28-family-finance-brainstorm.md`
**Date:** 2026-03-01
**Verdict:** NEEDS REVISION

---

## Simplicity Feedback

### Findings

| # | What | Why It Matters | Simpler Alternative |
|---|------|----------------|---------------------|
| S1 | **Supabase sync in MVP** — T3 introduces Supabase client, WorkManager sync, and the full offline-first repository pattern before any UI exists | Adds networking, auth, conflict resolution, and background scheduling complexity _before_ we even know the local data model works well. High risk of re-work if schema changes during UI development | Defer Supabase entirely to Wave 2. Build T2-T7 purely against Room. Add sync as T8 once the local app is stable and schemas are proven |
| S2 | **Multi-currency mentioned in brainstorm but absent from plan schema** — Brainstorm lists multi-currency transfers and a `Currencies` table, but `TransactionEntity` in T2 has no `currency` field and no `Currencies` entity | This means we'll either build single-currency now and refactor later, or try to sneak it in during T2 without a clear design. Both are expensive | Decide explicitly: either add `CurrencyEntity` + `currency_id` to T2, or defer to a post-MVP story and remove it from the brainstorm scope |
| S3 | **Wave 3 parallelism is optimistic** — T4/T5/T6/T7 "can be built concurrently" but they _all_ depend on a single `FinanceRepository` API that doesn't exist yet in detail | In practice a solo developer builds these serially. Claiming parallel-readiness adds false confidence | Remove the "concurrent" claim. Order them: T4 → T5 → T6 → T7 (each one solidifies the Repository API for the next) |
| S4 | **Generic `EntityManageScreen` in T7** — A single reusable CRUD screen for Accounts, Categories, _and_ Projects | These entities have very different fields (Account has type/currency/owner, Category has icon/color, Project has date range). A "reusable" screen will quickly accumulate special-case logic | Build three simple, focused screens. Reuse only the scaffold if convenient |

### Positive
- Flat receipt-splitting (two rows instead of parent/child) is a great simplification
- Separating data (T2) from UI (T4+) is a clean layering
- Material 3 / spacious design is well-scoped — no over-the-top custom design system

---

## Feasibility Feedback

### Findings

| # | What | Risk | Suggestion |
|---|------|------|------------|
| F1 | **No `TransactionLine` / split-line entity** — Brainstorm says "flat records," but T5 (FastEntrySheet) describes a split UX that saves "multiple transactions." There's no linking mechanism to tie splits back to one receipt | **High** | Add a nullable `receipt_group_id` (UUID) to `TransactionEntity` so the Timeline can group splits visually while storage stays flat |
| F2 | **Missing `owner_label` on Transaction** — Brainstorm says "Owner label for quick filtering," but only `AccountEntity` has `owner_label`. Transactions themselves have no owner | **Medium** | Add `owner_label` (or derive from Account) and decide if filtering is per-transaction or per-account |
| F3 | **Transfer transactions not modeled** — Brainstorm explicitly lists "Account-to-Account transfers," but the schema has no mechanism for them (a transfer touches two accounts atomically) | **High** | Add a `transfer_linked_id` field or a `TransferEntity` pairing two Transaction rows. Define this in T2 before UI work |
| F4 | **T1 verification command may fail** — `./gradlew clean properties` is not a standard way to verify a Compose project compiles. It prints properties but doesn't compile | **Low** | Change to `./gradlew assembleDebug` for T1 as well, or at minimum `./gradlew tasks` to verify the project structure |
| F5 | **No DI framework specified** — ViewModels need Repository, Repository needs Room + Supabase. Without Hilt/Koin, constructor injection will be manual and fragile | **Medium** | Add Hilt (or Koin) as a T1 dependency. It's a one-time setup cost that pays off in every subsequent task |
| F6 | **No Navigation framework** — T4-T7 define separate screens but there's no mention of Compose Navigation or a NavHost. This is infrastructure that all screens depend on | **Medium** | Add Compose Navigation setup to T1 or T4, with a NavHost containing all routes defined upfront |
| F7 | **Acceptance criteria are vague for verification** — Most verification steps say "manual check," but there are no unit/integration test tasks anywhere in the plan | **Medium** | Add at least DAO unit tests in T2 and ViewModel tests in T4/T5. This prevents regressions as the app grows |

### Positive
- Room + Flow for reactive UI is the correct Android-native approach
- WorkManager for sync is the right tool (survives app kills, respects battery)
- The offline-first architecture (write to Room first) is sound in principle

---

## Codebase Alignment

### Findings

| # | What | Evidence | Suggestion |
|---|------|----------|------------|
| C1 | **No existing code to align against** — This is a greenfield project. The `family_finance` repo currently contains only docs, tasks, and skills | Directory listing shows no `app/`, `build.gradle.kts`, or source files | Fine for T1, but the plan should reference the Android template or `android init` command explicitly |
| C2 | **AGENTS.md has empty Stack Conventions** — `BUILD_CMD`, `LINT_CMD` are `???` | `AGENTS.md` lines 45-47 | T1 should include filling in `BUILD_CMD: ./gradlew assembleDebug` and `LINT_CMD: ./gradlew lint` as a subtask |
| C3 | **Package name `com.familyfinance` may need confirmation** — Plan uses `com.familyfinance` but no `applicationId` is confirmed with the user | Plan file paths reference `com/familyfinance/*` | Ask user to confirm package/applicationId before T1 |
| C4 | **`compound-work` skill expects BUILD_CMD and LINT_CMD** — Without these, the work skill can't verify tasks | `AGENTS.md` lines 45-47 | Must be filled in during T1 to enable subsequent skills |
| C5 | **Brainstorm open questions are unresolved** — Three open questions remain with no decisions captured | Brainstorm lines 48-50 (currency exchange, refunds, auth) | Resolve at least the refund question before T2 (schema impact). Auth can be deferred |

### Positive
- Skill/docs infrastructure is already well-organized
- Dashboard tracking is in place and correctly shows plan-review as the next queue item
- The plan follows the project's established slug naming convention

---

## Recommended Changes

1. **[HIGH] Add `receipt_group_id` and `transfer_linked_id` to the Transaction schema (F1, F3)** — These are critical for the split-receipt UX and transfers, which are both core MVP features. Without them, T5 and future transfer UI can't work correctly.

2. **[HIGH] Defer Supabase sync to post-local-stability (S1)** — Build the entire app against Room first (T1→T2→T4→T5→T6→T7), then add sync as a final task. This halves the initial complexity and avoids reworking sync when schemas inevitably change.

3. **[MEDIUM] Add DI (Hilt) and Navigation setup to T1 (F5, F6)** — These are foundational infrastructure that every subsequent task depends on. Adding them later means refactoring all existing code.

4. **[MEDIUM] Decide on multi-currency now or defer explicitly (S2)** — The brainstorm lists it as MVP scope, but the plan schema doesn't support it. Resolve this contradipction.

5. **[MEDIUM] Add `owner_label` to Transaction or clarify filtering approach (F2)** — Important for the shared-vault privacy model.

6. **[LOW] Replace generic `EntityManageScreen` with focused screens (S4)** — Simpler to build, easier to maintain.

7. **[LOW] Add test tasks to T2 and T4 (F7)** — Even basic DAO tests prevent painful regressions.

8. **[LOW] Fill in AGENTS.md stack conventions in T1 (C2, C4)** — Required for compound-work to function.
