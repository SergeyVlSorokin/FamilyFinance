# Plan: Family Finance App

**Date:** 2026-02-28
**Brainstorm:** docs/brainstorms/2026-02-28-family-finance-brainstorm.md
**Status:** Revised (post plan-review)
**Review:** docs/plan-reviews/2026-03-01-00-45-family-finance-plan-review.md

## Implementation Preferences
- **UI Layout:** Spacious/Modern (Clean, large tap targets, whitespace, icons).
- **Entry UX:** Single-Screen Sheet (Dynamic split lines on the exact same screen).
- **Data Sync:** Deferred — Build fully offline with Room first. Add Supabase sync as a final phase once schemas are stable.

## User Review Required
> [!IMPORTANT]
> Since this is a brand new project, **T1** will initialize an Android project from scratch in the repository root. Please confirm you are OK with the standard Android Gradle layout being placed directly into the `family_finance` root.
> Also, please confirm the desired **applicationId / package name** (plan assumes `com.familyfinance`).

> [!WARNING]
> **Multi-currency is deferred to post-MVP.** The brainstorm listed it as in-scope, but adding `Currencies` table + exchange rates significantly increases schema and UI complexity. The plan will support a single default currency for now; multi-currency can be added as a follow-up story once the core app is stable.

> [!NOTE]
> **Brainstorm open questions still unresolved:**
> - How will currency exchange rates be handled? (Deferred with multi-currency)
> - Is a "Refund" a separate type or just Income? → **Decision needed before T2** — plan assumes refunds are regular income transactions tagged to an expense category.
> - How will user auth / family invitations work? (Deferred with Supabase sync)

## Acceptance Criteria
- [ ] Project initialized with Jetpack Compose, Hilt, and Compose Navigation.
- [ ] Room Database established with `Accounts`, `Categories`, `Transactions`, and `Projects` tables.
- [ ] Transaction schema supports split receipts (`receipt_group_id`) and transfers (`transfer_linked_id`).
- [ ] Repository pattern implemented against Room (no remote sync yet).
- [ ] Accounts Dashboard displays balances expansively and clearly.
- [ ] Fast Entry Bottom Sheet implemented allowing dynamic receipt splitting within a single screen.
- [ ] Transaction Timeline view implemented.
- [ ] Configuration Screens implemented for managing Accounts, Categories, and Projects (separate focused screens).
- [ ] DAO and ViewModel unit tests exist for core data operations.
- [ ] AGENTS.md `BUILD_CMD` and `LINT_CMD` filled in.

## Plan

### T1: Initialize Android Project Foundation
**Files:** `build.gradle.kts`, `app/build.gradle.kts`, `settings.gradle.kts`, `app/src/main/AndroidManifest.xml`, `AGENTS.md`
**Verify:** `./gradlew assembleDebug` completes successfully.
**Done when:** A blank Jetpack Compose Android app compiles without errors, with DI and navigation scaffolding in place.

- [ ] Generate standard Android project structure (Jetpack Compose template).
- [ ] Add dependencies: Compose, Room, Hilt, Compose Navigation, Kotlin Coroutines.
- [ ] Set up Hilt application class and `@HiltAndroidApp` annotation.
- [ ] Create `NavHost` with placeholder routes for Dashboard, Timeline, FastEntry, and Settings.
- [ ] Update `AGENTS.md` — set `BUILD_CMD: ./gradlew assembleDebug` and `LINT_CMD: ./gradlew lint`.

### T2: Room Database & Schema
**Files:** `app/src/main/java/com/familyfinance/data/local/AppDatabase.kt`, `app/src/main/java/com/familyfinance/data/local/entities/*`, `app/src/main/java/com/familyfinance/data/local/dao/*`
**Verify:** `./gradlew assembleDebug` succeeds with Room annotation processors. DAO unit tests pass via `./gradlew testDebugUnitTest`.
**Done when:** All entity tables and DAOs are defined with passing unit tests.

- [ ] Create `AccountEntity` (id, name, type, currency, owner_label).
- [ ] Create `CategoryEntity` (id, name, icon, color).
- [ ] Create `ProjectEntity` (id, name, start_date, end_date).
- [ ] Create `TransactionEntity`:
  - id, account_id, category_id, project_id (nullable), amount, date, receiver, note
  - `owner_label` — for shared-vault filtering
  - `receipt_group_id` (nullable UUID) — links split-receipt rows together
  - `transfer_linked_id` (nullable UUID) — pairs two rows in a transfer
  - `type` enum: INCOME, EXPENSE, TRANSFER
- [ ] Implement DAOs returning `Flow<List<T>>` for reactive UI.
- [ ] Write DAO unit tests (insert, query, update, delete for each entity).

### T3: Repository Layer
**Files:** `app/src/main/java/com/familyfinance/data/repository/FinanceRepository.kt`
**Verify:** `./gradlew assembleDebug` succeeds.
**Done when:** `FinanceRepository` wraps all DAO operations and is injectable via Hilt.

- [ ] Create `FinanceRepository` as a Hilt `@Singleton` wrapping all DAOs.
- [ ] Expose `Flow`-based read methods and suspend write methods.
- [ ] Implement `saveSplitReceipt(lines: List<TransactionEntity>)` — assigns shared `receipt_group_id`.
- [ ] Implement `saveTransfer(from: TransactionEntity, to: TransactionEntity)` — assigns shared `transfer_linked_id`.

### T4: Spacious Dashboard UI (Accounts & Balances)
**Files:** `app/src/main/java/com/familyfinance/ui/dashboard/DashboardScreen.kt`, `app/src/main/java/com/familyfinance/ui/dashboard/DashboardViewModel.kt`
**Verify:** Manual check: Open app, observe mock accounts with modern Material 3 cards. ViewModel unit tests pass.
**Done when:** A modern, spacious dashboard pulls `Flow<List<Account>>` and aggregates total wealth.

- [ ] Build `DashboardViewModel` (injected via Hilt) to aggregate account totals.
- [ ] Design `DashboardScreen` utilizing Material 3 Cards, large typography, and clean whitespace.
- [ ] Write ViewModel unit test verifying balance aggregation logic.

### T5: Fast Entry Split Sheet UX
**Files:** `app/src/main/java/com/familyfinance/ui/entry/FastEntrySheet.kt`, `app/src/main/java/com/familyfinance/ui/entry/FastEntryViewModel.kt`
**Verify:** Manual check: Open bottom sheet, type $100, add split $20 to Consumables, see remainder auto-calculate.
**Done when:** The single-screen split UI dynamically calculates remainders and saves linked flat records.

- [ ] Build `FastEntryViewModel` capturing StateFlow of split lines (Amount + Category).
- [ ] Auto-calculate remainder as user adds splits.
- [ ] Implement the `FastEntrySheet` BottomSheet.
- [ ] Wire 'Save' to `FinanceRepository.saveSplitReceipt()` when splits exist, or regular save for single transactions.

### T6: Timeline & Reporting View
**Files:** `app/src/main/java/com/familyfinance/ui/timeline/TimelineScreen.kt`
**Verify:** Manual check: Click on 'Timeline' tab and view chronological scrolling layout with grouped splits.
**Done when:** A scrollable timeline showing all transactions, grouped by month, with split-receipt visual grouping.

- [ ] Build `TimelineScreen` listing transactions ordered by date descending.
- [ ] Visually group transactions sharing the same `receipt_group_id`.
- [ ] Add basic filtering by owner_label and category.

### T7: Configuration & Settings Screens
**Files:** `app/src/main/java/com/familyfinance/ui/settings/SettingsScreen.kt`, `app/src/main/java/com/familyfinance/ui/settings/AccountsManageScreen.kt`, `app/src/main/java/com/familyfinance/ui/settings/CategoriesManageScreen.kt`, `app/src/main/java/com/familyfinance/ui/settings/ProjectsManageScreen.kt`
**Verify:** Manual check: Navigate to settings, add a new Category, and see it appear in the database.
**Done when:** Users can CRUD Accounts, Categories, and Projects via dedicated screens.

- [ ] Build `SettingsScreen` as a hub to navigate to specific entity managers.
- [ ] Build `AccountsManageScreen` — fields: name, type, currency, owner_label.
- [ ] Build `CategoriesManageScreen` — fields: name, icon, color.
- [ ] Build `ProjectsManageScreen` — fields: name, start_date, end_date.
- [ ] Wire save/delete actions to the `FinanceRepository`.

### T8: Supabase Sync Layer (Post-Stabilization)
**Files:** `app/src/main/java/com/familyfinance/data/remote/SupabaseClient.kt`, `app/src/main/java/com/familyfinance/sync/SyncWorker.kt`
**Verify:** Manual check: Add a transaction offline, observe it syncing to Supabase when connectivity is restored.
**Done when:** WorkManager-based sync pushes unsynced rows to Supabase, and the app works identically offline.

- [ ] Add Supabase Kotlin SDK and Ktor dependencies.
- [ ] Setup Supabase Client singleton.
- [ ] Add `sync_status` field to `TransactionEntity` (PENDING, SYNCED, FAILED).
- [ ] Implement `SyncWorker` capturing unsynced rows and uploading to Supabase.
- [ ] Add retry/backoff logic for failed syncs.

## Execution Order
- **Wave 1:** T1, T2 (Independent foundation) → depends: None
- **Wave 2:** T3 (Repository wrapping Room) → depends: T1, T2
- **Wave 3:** T4 → T5 → T6 → T7 (UI screens, built serially — each solidifies the Repository API) → depends: T3
- **Wave 4:** T8 (Supabase sync — added only after local app is stable) → depends: all above
