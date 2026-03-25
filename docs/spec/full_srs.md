# Software Requirements Specification (SRS)

## 1. Product Context
**Product:** A shared-vault family finance tracker allowing multiple family members to manage accounts (bank, cash, credit card, investment), log income and categorized expenses, split receipts, tag transactions to projects/trips, and run basic financial reports.
**Perspective:** A standalone native Android application for personal/family financial tracking. Intended as a B2C SaaS product with potential to scale to thousands of users. No dependency on external financial APIs at MVP; data is stored locally (Room) with Supabase cloud sync added post-stabilization.

### Definitions

*   **Shared Vault:** A privacy model where all family members see all accounts and transactions. Owner labels are used for quick filtering rather than strict data isolation.

*   **Receipt Split:** A single physical receipt recorded as two or more distinct expense rows in the database, each with its own Category, all linked via a shared receipt_group_id UUID.

*   **Project:** An optional grouping tag (e.g., 'Lulea Trip') that can be attached to transactions to enable specific cross-category financial reporting.

*   **Transfer:** A movement of funds between two internal accounts. Stored as two linked TransactionEntity rows (debit + credit) sharing a transfer_linked_id UUID.

*   **Owner Label:** A string field on accounts and transactions indicating which family member owns or created the record, used for filtering, not for security enforcement.


---

## 2. Stakeholders

### STK-001: End User — the person who actively records transactions and manages accounts on a daily basis.
*   **Concern:** 

### STK-002: End User — a co-owner of the shared vault who can view all accounts and transactions and optionally contribute entries.
*   **Concern:** 

### STK-003: Sponsor, Architect, and sole developer. Defines product vision, priorities, and technical decisions. Also the primary end-user for the MVP phase.
*   **Concern:** 


---

## 3. Requirements

### 3.1 User Requirements

*   **UR-001:** As a **Active Recorder**, I want **As an Active Recorder, I want to quickly record an expense with a category and account, so that my spending is tracked without interrupting my day.**
    *   *Trace:* {
  "stakeholders": [
    "STK-001"
  ],
  "business_goals": [
    "BR-004"
  ]
}

*   **UR-002:** As a **Active Recorder**, I want **As an Active Recorder, I want to split a single receipt into multiple categorized expense lines on one screen, so that I can accurately record mixed-category shopping trips without multiple separate entries.**
    *   *Trace:* {
  "stakeholders": [
    "STK-001"
  ],
  "business_goals": [
    "BR-002"
  ]
}

*   **UR-003:** As a **Active Recorder**, I want **As an Active Recorder, I want to transfer money between two accounts (e.g., from bank to cash wallet), so that my account balances always reflect reality.**
    *   *Trace:* {
  "stakeholders": [
    "STK-001"
  ],
  "business_goals": [
    "BR-004"
  ]
}

*   **UR-004:** As a **Passive Viewer**, I want **As a Passive Viewer, I want to see all account balances at a glance on the dashboard, so that I understand the family's overall financial position without digging through transactions.**
    *   *Trace:* {
  "stakeholders": [
    "STK-001",
    "STK-002"
  ],
  "business_goals": [
    "BR-001"
  ]
}

*   **UR-005:** As a **Passive Viewer**, I want **As a Passive Viewer, I want to scroll through a chronological timeline of all transactions, so that I can understand what happened when and find specific transactions.**
    *   *Trace:* {
  "stakeholders": [
    "STK-001",
    "STK-002"
  ],
  "business_goals": [
    "BR-001"
  ]
}

*   **UR-006:** As a **Active Recorder**, I want **As an Active Recorder, I want to tag transactions with a Project (e.g., 'Barcelona Trip'), so that I can later view and report on spending grouped by that project across any categories.**
    *   *Trace:* {
  "stakeholders": [
    "STK-001"
  ],
  "business_goals": []
}

*   **UR-007:** As a **Active Recorder**, I want **As an Active Recorder, I want to manage Accounts, Categories, and Projects from a Settings area, so that the app's reference data matches our family's actual financial structure.**
    *   *Trace:* {
  "stakeholders": [
    "STK-001",
    "STK-003"
  ],
  "business_goals": []
}

*   **UR-008:** As a **Active Recorder**, I want **As an Active Recorder, I want to use the app fully offline without any connectivity, so that I can record expenses anywhere — in a store, on a trip, or with no signal.**
    *   *Trace:* {
  "stakeholders": [
    "STK-001"
  ],
  "business_goals": [
    "BR-003"
  ]
}

*   **UR-009:** As a **Active Recorder**, I want **As an Active Recorder, I want to periodically reconcile an account by entering its actual real-world balance, so that I can detect discrepancies between the app's records and reality and correct them with a proper audit trail.**
    *   *Trace:* {
  "stakeholders": [
    "STK-001",
    "STK-003"
  ],
  "business_goals": [
    "BR-001"
  ]
}


### 3.2 Functional Requirements

#### FR-001: Record Single Expense
The system shall allow the user to create an expense transaction linked to a mandatory Category and Account, with an amount, date, optional note, and optional receiver.
*   **Priority:** Must-have
*   **Acceptance Criteria:**
    
    - Saving an expense without a Category is rejected with a validation message
    
    - Saving an expense without an Account is rejected with a validation message
    
    - A valid expense is persisted to Room and appears in the timeline immediately
    
*   **Trace:** {
  "user_requirements": [
    "UR-001"
  ],
  "business_rules": [
    "BR-004"
  ],
  "design_nodes": [
    "VIEW-001",
    "VIEW-004",
    "DATA-001"
  ],
  "verification_plans": [
    "SCN-001"
  ]
}

#### FR-002: Record Income
The system shall allow the user to create an income transaction linked to a mandatory Category and Account, with an amount, date, optional source, and optional note.
*   **Priority:** Must-have
*   **Acceptance Criteria:**
    
    - Income transaction increases the linked account balance
    
    - Saving income without a Category or Account is rejected
    
    - Income appears in the timeline with a distinct visual indicator (positive direction)
    
*   **Trace:** {
  "user_requirements": [
    "UR-001"
  ],
  "business_rules": [
    "BR-004"
  ],
  "design_nodes": [
    "VIEW-001",
    "VIEW-004",
    "DATA-001"
  ],
  "verification_plans": [
    "SCN-002"
  ]
}

#### FR-003: Split Receipt Entry
The system shall allow the user to split a single receipt into 2 or more expense lines on a single screen. Each line requires a Category and amount. The system shall auto-calculate the remaining unallocated amount as lines are added. On save, each line is persisted as a separate TransactionEntity row sharing a common receipt_group_id UUID.
*   **Priority:** Must-have
*   **Acceptance Criteria:**
    
    - User can add up to N split lines on the fast entry screen without changing screens
    
    - Remaining amount updates in real-time as line amounts change
    
    - Saving a split where lines sum > total amount is rejected with a validation message
    
    - All saved split rows share the same non-null receipt_group_id
    
    - Timeline groups split rows visually under one receipt heading
    
*   **Trace:** {
  "user_requirements": [
    "UR-002"
  ],
  "business_rules": [
    "BR-002"
  ],
  "design_nodes": [
    "VIEW-001",
    "VIEW-002",
    "VIEW-004",
    "DATA-001"
  ],
  "verification_plans": [
    "SCN-003"
  ]
}

#### FR-004: Account-to-Account Transfer
The system shall allow the user to record a transfer of funds between two different accounts. The system shall persist this as exactly two linked TransactionEntity rows (one debit, one credit) sharing a transfer_linked_id UUID. Both rows must have type=TRANSFER.
*   **Priority:** Must-have
*   **Acceptance Criteria:**
    
    - User must select distinct source and destination accounts (same account rejected)
    
    - Source account balance decreases by the transfer amount
    
    - Destination account balance increases by the transfer amount
    
    - Two database rows are created sharing a non-null transfer_linked_id
    
    - Timeline shows both legs of the transfer linked visually
    
*   **Trace:** {
  "user_requirements": [
    "UR-003"
  ],
  "business_rules": [
    "BR-004"
  ],
  "design_nodes": [
    "VIEW-001",
    "VIEW-002",
    "VIEW-004",
    "DATA-001"
  ],
  "verification_plans": [
    "SCN-004"
  ]
}

#### FR-005: Account Balances Dashboard
The system shall display a dashboard screen showing all accounts with their name, type, and current balance, plus an aggregated total wealth figure. Balances shall be derived reactively from persisted transactions using Flow-based data streams.
*   **Priority:** Must-have
*   **Acceptance Criteria:**
    
    - Dashboard loads and displays all accounts without manual refresh
    
    - Total wealth is the sum of all account balances
    
    - Adding a new transaction updates the relevant account balance within 1 second without screen refresh
    
    - Empty state (no accounts) shows a helpful prompt to create an account
    
*   **Trace:** {
  "user_requirements": [
    "UR-004"
  ],
  "business_rules": [
    "BR-003"
  ],
  "design_nodes": [
    "VIEW-001",
    "VIEW-002",
    "VIEW-004",
    "DATA-001",
    "ADR-003"
  ],
  "verification_plans": [
    "SCN-005"
  ]
}

#### FR-006: Transaction Timeline
The system shall provide a scrollable timeline screen listing all transactions in descending date order, grouped by month. Split receipt rows shall be visually grouped. The screen shall support filtering by owner_label, category, and project.
*   **Priority:** Must-have
*   **Acceptance Criteria:**
    
    - All transactions appear in descending date order
    
    - Month separator headers divide transactions by calendar month
    
    - Transactions sharing a receipt_group_id are shown as an indented group
    
    - Filtering by category shows only transactions of that category
    
    - Filtering by owner_label shows only transactions with that label
    
    - Filtering by project shows only transactions tagged to that project, regardless of category
    
*   **Trace:** {
  "user_requirements": [
    "UR-005"
  ],
  "business_rules": [
    "BR-001"
  ],
  "design_nodes": [
    "VIEW-001",
    "VIEW-004",
    "DATA-001"
  ],
  "verification_plans": [
    "SCN-006",
    "SCN-009"
  ]
}

#### FR-007: Configuration Management (Accounts, Categories, Projects)
The system shall provide a Settings hub with dedicated sub-screens for creating, editing, and deleting Accounts, Categories, and Projects. Changes shall be immediately reflected app-wide.
*   **Priority:** Must-have
*   **Acceptance Criteria:**
    
    - User can navigate from a Settings hub to dedicated screens for Accounts, Categories, and Projects
    
    - User can create an Account with name, type (Bank/Cash/Investment/CreditCard), owner_label, and an opening balance (defaults to 0 if left empty)
    
    - When an account is created with a non-zero opening balance, the system shall automatically create a system-generated OPENING_BALANCE transaction for that amount, dated at account creation time
    
    - OPENING_BALANCE transactions are excluded from expense/income category reports; they represent starting state, not real cash flow
    
    - User can create a Category with name, icon, and color
    
    - User can create a Project with name, start_date, and end_date
    
    - User can edit and delete existing items of all three types
    
    - Newly created items appear immediately in entry form pickers
    
*   **Trace:** {
  "user_requirements": [
    "UR-007"
  ],
  "business_rules": [],
  "design_nodes": [
    "VIEW-001",
    "VIEW-004",
    "DATA-001"
  ],
  "verification_plans": [
    "SCN-007",
    "SCN-013"
  ]
}

#### FR-008: Local-Only Data Persistence (Room)
The system shall persist all financial data exclusively to a local Room (SQLite) database. No network call shall be required for any create, read, update, or delete operation. The system shall function fully without internet connectivity.
*   **Priority:** Must-have
*   **Acceptance Criteria:**
    
    - All CRUD operations complete successfully with no network connection
    
    - Data is persisted across app restarts
    
    - No network permission usage is required for core offline functionality
    
*   **Trace:** {
  "user_requirements": [
    "UR-008"
  ],
  "business_rules": [
    "BR-003"
  ],
  "design_nodes": [
    "VIEW-003",
    "DATA-001",
    "ADR-001"
  ],
  "verification_plans": [
    "SCN-008"
  ]
}

#### FR-009: Project Tagging on Transactions
The system shall allow the user to optionally tag any transaction with a pre-configured Project. The project_id field on TransactionEntity is nullable; untagged transactions are valid. The timeline shall support filtering by project.
*   **Priority:** Should-have
*   **Acceptance Criteria:**
    
    - Project picker appears as an optional field in the transaction entry screen
    
    - A transaction can be saved with project_id = null (no project)
    
    - Filtering the timeline by a project shows all transactions tagged to it, regardless of category
    
    - Transactions tagged to a project remain correctly linked after the project is edited
    
*   **Trace:** {
  "user_requirements": [
    "UR-006"
  ],
  "business_rules": [],
  "design_nodes": [
    "VIEW-001",
    "VIEW-004",
    "DATA-001"
  ],
  "verification_plans": [
    "SCN-009"
  ]
}

#### FR-010: Account Reconciliation — Balance Verification
The system shall provide a Reconciliation flow for any account where the user enters the actual real-world balance. The system shall compute and display the discrepancy (actual minus recorded balance). If the discrepancy is zero, the system shall record a reconciliation timestamp on the account and require no further action. If a discrepancy exists, the system shall present the appropriate correction action based on account type (see FR-011 for non-investment, FR-012 for investment).
*   **Priority:** Must-have
*   **Acceptance Criteria:**
    
    - Reconciliation can be initiated for any existing account
    
    - The screen shows: account name, currently recorded balance, user-entered actual balance, and computed discrepancy
    
    - Discrepancy = actual balance − recorded balance; a negative value means the app shows more money than exists
    
    - When discrepancy is exactly 0, a 'Reconciled ✓' confirmation is shown with a timestamp; no transaction is created
    
    - When discrepancy is non-zero, the flow proceeds to account-type-appropriate correction (FR-011 or FR-012)
    
*   **Trace:** {
  "user_requirements": [
    "UR-009"
  ],
  "business_rules": [
    "BR-001"
  ],
  "design_nodes": [
    "VIEW-001",
    "VIEW-004",
    "DATA-001",
    "ADR-003"
  ],
  "verification_plans": [
    "SCN-010"
  ]
}

#### FR-011: Reconciliation Correction — Bank, Cash, Credit Card Accounts
When a reconciliation discrepancy is detected on a Bank, Cash, or Credit Card account, the system shall allow the user to create a correction transaction of type RECONCILIATION_ADJUSTMENT. If discrepancy is negative (app overstates balance), the transaction is an 'Unrecorded Expense'. If discrepancy is positive (app understates balance), the transaction is an 'Unrecorded Income'. The transaction amount equals the absolute value of the discrepancy. After saving, the account's recorded balance shall match the entered actual balance exactly.
*   **Priority:** Must-have
*   **Acceptance Criteria:**
    
    - Correction transaction is pre-filled with the discrepancy amount and labeled 'Reconciliation Adjustment'
    
    - User cannot change the amount of the pre-filled correction (it is locked to the discrepancy)
    
    - User can optionally add a note to the correction transaction
    
    - After saving, querying the account balance returns the user-entered actual value
    
    - Correction transaction appears in the timeline distinctly labeled (e.g. '⚖ Reconciliation Adjustment') and is filterable/excludable
    
*   **Trace:** {
  "user_requirements": [
    "UR-009"
  ],
  "business_rules": [
    "BR-004"
  ],
  "design_nodes": [
    "VIEW-001",
    "VIEW-004",
    "DATA-001"
  ],
  "verification_plans": [
    "SCN-011"
  ]
}

#### FR-012: Reconciliation Correction — Investment Accounts (Revaluation)
When a reconciliation discrepancy is detected on an Investment account, the system shall allow the user to create a REVALUATION transaction. A positive discrepancy (current market value > last recorded value) represents unrealized capital gain. A negative discrepancy represents unrealized capital loss. The transaction amount equals the absolute discrepancy value, signed appropriately. Revaluation transactions are stored as a distinct type and displayed separately from regular Income/Expense entries in the timeline.
*   **Priority:** Must-have
*   **Acceptance Criteria:**
    
    - Revaluation is only offered for account type = Investment; non-investment accounts use FR-011 instead
    
    - Revaluation transaction is pre-filled with the absolute discrepancy amount and labeled 'Revaluation'
    
    - Positive revaluation (gain) and negative revaluation (loss) are visually distinguishable in the timeline
    
    - Revaluation transactions are excluded from expense/income category reports by default (they are not real cash flows)
    
    - After saving, querying the account balance returns the user-entered actual market value
    
    - Multiple revaluations over time form a history of investment value development visible in the timeline
    
*   **Trace:** {
  "user_requirements": [
    "UR-009"
  ],
  "business_rules": [],
  "design_nodes": [
    "VIEW-001",
    "VIEW-004",
    "DATA-001"
  ],
  "verification_plans": [
    "SCN-012"
  ]
}

#### FR-013: Single Default Currency — No Multi-Currency Support (MVP)
The system shall operate exclusively in a single implicit default currency for the MVP. The system shall NOT provide currency selection, currency conversion, or exchange rate functionality. All monetary amounts are stored as plain numeric values without a currency code.
*   **Priority:** Must-have
*   **Acceptance Criteria:**
    
    - No currency picker appears anywhere in the UI
    
    - The TransactionEntity and AccountEntity schemas have no currency_code column
    
    - All amounts are displayed with a single app-wide currency symbol configured at build time
    
*   **Trace:** {
  "user_requirements": [
    "UR-008"
  ],
  "business_rules": [
    "BR-005"
  ],
  "design_nodes": [
    "DATA-001",
    "ADR-004"
  ],
  "verification_plans": [
    "SCN-008"
  ]
}

#### FR-014: Application Navigation & Structure
The system shall provide a centralized and type-safe navigation mechanism. All screens must be reachable via a defined navigation graph. Back navigation must be handled consistently across all feature areas.
*   **Priority:** Must-have
*   **Acceptance Criteria:**
    
    - All routes are defined in a centralized sealed class
    
    - NavGraph encapsulates the NavHost configuration
    
    - Back navigation from sub-screens always returns to the correct parent or hub
    
    - MainActivity contains zero navigation business logic
    
*   **Trace:** {
  "user_requirements": [
    "UR-007"
  ],
  "design_nodes": [
    "VIEW-004",
    "ADR-005"
  ],
  "verification_plans": [
    "SCN-014"
  ]
}

#### FR-015: Unique Name Identification (Accounts, Categories, Projects)
The system shall enforce name uniqueness for Accounts, Categories, and Projects to prevent data ambiguity. When a user attempts to save a duplicate name, the system shall provide clear visual feedback and prevent the record from being saved until a unique name is provided.
*   **Priority:** Must-have
*   **Acceptance Criteria:**
    
    - The system shall not allow saving an Account with a name that already exists in the accounts table.
    
    - The system shall not allow saving a Category with a name that already exists in the categories table.
    
    - The system shall not allow saving a Project with a name that already exists in the projects table.
    
    - When a duplicate name is entered in a creation/edit dialog, the system shall display an alert or error message (e.g., 'Name already exists').
    
    - The creation/edit dialog shall remain open if a name uniqueness violation is detected upon submission.
    
    - The 'Save' button may be disabled if the current name is detected as a duplicate (real-time validation).
    
*   **Trace:** {
  "user_requirements": [
    "UR-007"
  ],
  "business_rules": [],
  "design_nodes": [
    "DATA-001",
    "VIEW-005"
  ],
  "verification_plans": [
    "SCN-007"
  ]
}

#### FR-016: Uniform Monetary Entry Style
The system shall provide a consistent monetary entry UI across all screens. Users shall enter values as decimals which the system automatically scales to integer cents for storage.
*   **Priority:** Must-have
*   **Acceptance Criteria:**
    
    - All amount fields use the same '$ ' prefix and bold typography
    
    - Entry is decimal-based (no explicit 'cents' input required)
    
    - Consistency across: New Transaction, Add Account, and Reconciliation
    
*   **Trace:** {
  "user_requirements": [
    "UR-001",
    "UR-007",
    "UR-009"
  ],
  "design_nodes": [
    "VIEW-005"
  ]
}


### 3.3 Non-Functional Requirements


### 3.4 Constraints


### 3.5 Assumptions

