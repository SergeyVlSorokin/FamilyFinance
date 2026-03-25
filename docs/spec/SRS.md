# Software Requirements Specification (SRS)
## Family Finance App — MVP

---

## 1. Product Context

A **shared-vault family finance tracker** for Android. Multiple family members can manage accounts (bank, cash, credit card, investment), record income and expenses, split receipts, tag transactions to projects, and reconcile account balances. Intended as a personal tool scalable to a B2C SaaS product.

### Scope

| In Scope | Out of Scope |
|---|---|
| Android Native (Jetpack Compose) | iOS / Web |
| Local-first Room database | Live cloud sync (Supabase — post-MVP) |
| Expense, Income, Transfer recording | Multi-currency / exchange rates |
| Fast receipt splitting (single screen) | Inter-family privacy / RLS |
| Project/trip tagging | Live investment ticker tracking |
| Account reconciliation | User authentication / family invitations |
| Timeline with filters | |
| Settings: Accounts, Categories, Projects | |

### Definitions

| Term | Meaning |
|---|---|
| **Shared Vault** | All family members see all accounts and transactions. Owner labels are for filtering only, not security. |
| **Receipt Split** | One physical receipt stored as 2+ expense rows, all sharing a `receipt_group_id` UUID. |
| **Project** | Optional grouping tag (e.g. "Lulea Trip") attached to transactions for cross-category reporting. |
| **Transfer** | Money moved between internal accounts — stored as two linked rows sharing `transfer_linked_id`. |
| **Owner Label** | Freetext field (e.g. "Serge") on accounts and transactions for filtering. Not enforced as security. |

---

## 2. Stakeholders

| ID | Name | Role | Influence |
|---|---|---|---|
| STK-001 | Primary Family Member | Daily recorder of transactions and accounts | Critical |
| STK-002 | Secondary Family Member (Spouse) | Passive viewer; occasional contributor | High |
| STK-003 | Product Owner / Developer | Vision, architecture, sole developer & primary user | Critical |

---

## 3. Business Rules

| ID | Rule |
|---|---|
| **BR-001** | Fully shared vault — no RLS between family members |
| **BR-002** | Split receipts stored as flat rows with shared `receipt_group_id` |
| **BR-003** | Local-first architecture — Room before Supabase; app must work fully offline |
| **BR-004** | Every transaction **must** have a Category and Account |
| **BR-005** | Single-currency MVP — no multi-currency support until post-MVP |

---

## 4. User Characteristics (Personas)

### UCH-001 — Active Recorder
> Enters transactions at point of purchase. Needs speed above all. Works offline constantly.
- **Expertise:** Intermediate · **Usage:** Frequent

### UCH-002 — Passive Viewer
> Browses balances and timeline. Rarely enters data. Wants a clean, uncluttered view.
- **Expertise:** Novice · **Usage:** Occasional

---

## 5. User Requirements

### UR-001 — Record an Expense *(High)*
> As an Active Recorder, I want to quickly record an expense with a category and account, so that my spending is tracked without interrupting my day.

- User can open the expense entry UI in 1 tap from the dashboard
- Category and Account are mandatory before saving
- Expense appears in the timeline immediately after save

---

### UR-002 — Split Receipt Entry *(High)*
> As an Active Recorder, I want to split a single receipt into multiple categorized lines on one screen, so that I can accurately record mixed-category shopping trips.

- Split lines added on the same screen without navigation
- Remainder auto-calculates in real-time
- All split rows saved sharing the same `receipt_group_id`
- Timeline groups split rows visually

---

### UR-003 — Account Transfer *(High)*
> As an Active Recorder, I want to transfer money between accounts, so that my balances always reflect reality.

- Source and destination accounts must be different
- Both account balances update correctly
- Two linked rows created with shared `transfer_linked_id`

---

### UR-004 — Account Balances Dashboard *(High)*
> As a Passive Viewer, I want to see all account balances at a glance, so that I understand the family's overall financial position.

- Dashboard shows each account with name, type, and current balance
- Total wealth figure (sum of all balances) displayed prominently
- Balances update reactively — no manual refresh needed

---

### UR-005 — Transaction Timeline *(High)*
> As a Passive Viewer, I want to scroll through a chronological timeline, so that I can find and understand past transactions.

- Transactions ordered descending by date, grouped by month
- Split receipt rows visually grouped
- Filterable by **owner_label**, **category**, or **project**

---

### UR-006 — Project Tagging *(Medium)*
> As an Active Recorder, I want to tag transactions with a Project (e.g. "Barcelona Trip"), so that I can report on spending by project across categories.

- Projects managed in Settings
- Project tag is optional on any transaction
- Timeline filterable by project

---

### UR-007 — Settings / Configuration *(High)*
> As an Active Recorder, I want to manage Accounts, Categories, and Projects from Settings, so that the app matches our family's financial structure.

- Full CRUD for Accounts (name, type, owner_label, **opening balance**)
- Full CRUD for Categories (name, icon, color)
- Full CRUD for Projects (name, start_date, end_date)
- Changes appear immediately in entry forms

---

### UR-008 — Fully Offline *(High)*
> As an Active Recorder, I want to use the app without any internet connection.

- All CRUD works in airplane mode
- Data persists across restarts
- No network permission required for core features

---

### UR-009 — Account Reconciliation *(High)*
> As an Active Recorder, I want to periodically verify an account's actual balance against the app's records, so that I can detect and correct discrepancies.

- Shows recorded vs actual balance and the discrepancy
- Zero discrepancy → "Reconciled ✓" with timestamp, no transaction created
- Non-investment accounts → option to create `RECONCILIATION_ADJUSTMENT` transaction
- Investment accounts → option to create `REVALUATION` transaction
- All correction transactions clearly labeled in the timeline

---

## 6. Functional Requirements

### Transaction Entry

| ID | Title | Priority | Traces To |
|---|---|---|---|
| FR-001 | Record Single Expense | Must-have | UR-001, BR-004 |
| FR-002 | Record Income | Must-have | UR-001, BR-004 |
| FR-003 | Split Receipt Entry | Must-have | UR-002, BR-002 |
| FR-004 | Account-to-Account Transfer | Must-have | UR-003, BR-004 |

#### FR-001: Record Single Expense
The system shall allow the user to create an expense transaction with a mandatory Category and Account, plus amount, date, optional note and receiver.

**Acceptance Criteria:**
- Saving without Category → rejected with validation message
- Saving without Account → rejected with validation message
- Valid expense persisted to Room and appears in timeline immediately

*Verified by: SCN-001*

---

#### FR-002: Record Income
The system shall allow the user to record income with a mandatory Category and Account, plus amount, date, optional source.

**Acceptance Criteria:**
- Income increases the linked account balance
- Saving without Category or Account is rejected
- Income shows with a distinct positive visual indicator in the timeline

*Verified by: SCN-002*

---

#### FR-003: Split Receipt Entry
The system shall allow splitting a single receipt into 2+ expense lines on one screen. Each line requires a Category and amount. Remaining unallocated amount auto-calculates. On save, each line is persisted as a separate row sharing a `receipt_group_id` UUID.

**Acceptance Criteria:**
- Multiple split lines added without leaving the screen
- Remaining amount updates in real-time
- Saving where lines sum > total → rejected
- All saved rows share the same non-null `receipt_group_id`
- Timeline groups split rows under one receipt heading

*Verified by: SCN-003*

---

#### FR-004: Account-to-Account Transfer
The system shall persist a transfer as exactly two linked rows (debit + credit) sharing a `transfer_linked_id` UUID, both with type=TRANSFER.

**Acceptance Criteria:**
- Same source and destination account → rejected
- Source balance decreases, destination increases by transfer amount
- Two DB rows created sharing a non-null `transfer_linked_id`
- Both legs shown linked in the timeline

*Verified by: SCN-004*

---

#### FR-014: Application Navigation & Structure
The system shall provide a centralized and type-safe navigation mechanism. All screens must be reachable via a defined navigation graph. Back navigation must be handled consistently across all feature areas.

**Acceptance Criteria:**
- All routes are defined in a centralized sealed class
- NavGraph encapsulates the NavHost configuration
- Back navigation from sub-screens always returns to the correct parent or hub
- MainActivity contains zero navigation business logic

---

### Dashboard & Timeline

#### FR-005: Account Balances Dashboard
The system shall display all accounts with name, type, and current balance, plus total wealth. Balances derived reactively from transactions via Flow.

**Acceptance Criteria:**
- Loads without manual refresh
- Total wealth = sum of all account balances
- New transaction updates balance within 1 second, no screen refresh
- Empty state shows a prompt to create an account

*Verified by: SCN-005*

---

#### FR-006: Transaction Timeline
The system shall provide a scrollable timeline in descending date order, grouped by month, with split receipt visual grouping, filterable by owner_label, category, and project.

**Acceptance Criteria:**
- Descending date order with month separator headers
- Split rows indented and grouped
- Category filter shows only matching transactions
- Owner label filter shows only matching transactions
- Project filter shows only tagged transactions regardless of category

*Verified by: SCN-006, SCN-009*

---

### Configuration

#### FR-007: Configuration Management (Accounts, Categories, Projects)
Settings hub with dedicated sub-screens for CRUD of Accounts, Categories, and Projects.

**Acceptance Criteria:**
- Navigate to dedicated screens for each entity type
- Create Account with name, type (Bank/Cash/Investment/CreditCard), owner_label, and **opening balance** (defaults to 0)
- Non-zero opening balance → system auto-creates an `OPENING_BALANCE` transaction (excluded from cash-flow reports)
- Create Category with name, icon, color
- Create Project with name, start_date, end_date
- Edit/delete all three types
- New items appear immediately in entry form pickers

*Verified by: SCN-007, SCN-013*

---

### Infrastructure

#### FR-008: Local-Only Data Persistence (Room)
The system shall persist all data exclusively to Room (SQLite). No network required for any CRUD operation.

**Acceptance Criteria:**
- All CRUD works in airplane mode
- Data persists across restarts
- No network permission required for core functionality

*Verified by: SCN-008*

---

#### FR-013: Single Default Currency (MVP)
The system shall operate in a single implicit default currency with no currency selection, conversion, or exchange rate UI.

**Acceptance Criteria:**
- No currency picker in the UI
- No `currency_code` column in schema
- All amounts displayed with a single app-wide currency symbol

---

### Project Tagging

#### FR-009: Project Tagging on Transactions
The system shall allow optionally tagging any transaction with a pre-configured Project. `project_id` is nullable. Timeline filterable by project.

**Acceptance Criteria:**
- Project picker appears as optional in entry form
- Transaction can be saved with no project
- Project filter on timeline shows all tagged transactions across categories
- Tag remains correct after project is edited

*Verified by: SCN-009*

---

### Reconciliation

#### FR-010: Account Reconciliation — Balance Verification
The system shall provide a reconciliation flow where the user enters the actual balance to compute a discrepancy against the recorded balance.

**Acceptance Criteria:**
- Initiatable for any account
- Shows: recorded balance, actual balance field, computed discrepancy (actual − recorded)
- Discrepancy = 0 → "Reconciled ✓" with timestamp, no transaction created
- Discrepancy ≠ 0 → proceeds to FR-011 (non-investment) or FR-012 (investment)

*Verified by: SCN-010*

---

#### FR-011: Reconciliation Correction — Bank, Cash, Credit Card
Creates a `RECONCILIATION_ADJUSTMENT` transaction to correct the discrepancy. Negative discrepancy = Unrecorded Expense; positive = Unrecorded Income.

**Acceptance Criteria:**
- Correction pre-filled with discrepancy amount (locked, user cannot change)
- User can add an optional note
- After save, account balance equals the entered actual value
- Correction labeled "⚖ Reconciliation Adjustment" in timeline, filterable/excludable

*Verified by: SCN-011*

---

#### FR-012: Reconciliation Correction — Investment Accounts (Revaluation)
Creates a `REVALUATION` transaction for investment value changes. Excluded from cash-flow reports.

**Acceptance Criteria:**
- Revaluation only offered for Investment account type
- Pre-filled with absolute discrepancy, labeled "Revaluation"
- Gain and loss visually distinguishable in timeline
- Excluded from expense/income category reports by default
- After save, account balance equals entered market value
- Multiple revaluations form a trackable portfolio history

*Verified by: SCN-012*

---

## 7. Transaction Type Reference

| Type | Description | Included in Cash-Flow Reports? |
|---|---|---|
| `INCOME` | Real money received | ✅ Yes |
| `EXPENSE` | Real money spent | ✅ Yes |
| `TRANSFER` | Move between internal accounts | ❌ No |
| `OPENING_BALANCE` | Auto-created when account is added with initial balance | ❌ No |
| `RECONCILIATION_ADJUSTMENT` | Correction for unrecorded cash flows | ✅ Yes (separately filterable) |
| `REVALUATION` | Investment asset value change | ❌ No |
