# Brainstorm: Family Finance App

**Date:** 2026-02-28-22-00
**Status:** Planning

## Stakeholder Notes
Goal is to build a high-performance Android application to track family finances, with the potential to scale to thousands of users as a SaaS/B2C product. 

## What We're Building
A shared-vault family finance tracker. The app will allow multiple family members to track accounts containing money (banks, cash, credit cards, investments), track income and categorized expenses, and handle multi-currency transfers. The standout feature is a fast UI for splitting single receipts into multiple categorized expenses, alongside project/trip tagging for highly specific financial reporting (e.g., "Food expenses during the Barcelona Trip").

## Key Decisions Made
- **Tech Stack**: Jetpack Compose (Native Android) — *Provides the best native performance and modern development experience.*
- **Backend Database**: Supabase (PostgreSQL) — *Chosen over Firestore to easily support complex SQL aggregations (Sums, Group By Category/Month) required for financial reporting, and for better predictable cost-scaling with thousands of users.*
- **Privacy Model**: Fully Shared Vault — *Simplifies the data model. All family members see all accounts and transactions. We use an 'Owner' label for quick filtering rather than strict Row Level Security (RLS) isolation between spouses.*
- **Expense Splitting Structure**: Flat Records with Optional Projects — *A split receipt (e.g., $100 total = $80 Food, $20 Consumables) will be saved as two distinct database rows. Both rows require a Category, but can optionally be tagged to a specific "Project" (e.g., Trip to Lulea) to group them later.*

## Chosen Approach
We will build a reactive, offline-capable Compose app backed by a Supabase cloud database. The data taxonomy will be heavily normalized in SQL:
- `Accounts` (Bank Accounts, Cash vaults, Credit Cards with negative balances)
- `Transactions` (Income, Expenses, Transfers) -> *Mandatory link to Category and Account*
- `Categories` (Food, Consumables, Salary, etc.)
- `Projects` (Optional grouping tags like "Lulea Trip")
- `Currencies` (Support for multi-currency holding and exchange transfers)

*(Why This Approach / Why Not Alternative)*
This SQL-first approach front-loads the database design work but makes the eventual reporting dashboard (Timeline charts, Expense by Category per month) trivial and fast to compute on the server, saving the Android client from heavy data processing.

## Scope
**In Scope (MVP):**
- Android Native UI (Jetpack Compose)
- Create/Edit/View Accounts (Bank, Cash, Investment, Credit Card)
- Record Income (with source)
- Record Expenses (with Category, Account, Date, Amount, Receiver)
- Fast receipt-splitting UI
- Multi-currency support and Account-to-Account transfers
- Project assignment to group intermixed timeline expenses
- Returns/Refunds (Adding money back to account)
- Minimal Visualizations: Timeline total, Cash vs Non-Cash, Expenses by Category per month
- Periodic reconciliation (check if recorded balances match reality)

**Out of Scope (For Now):**
- Web Interface / iOS App (YAGNI for MVP)
- Strict inter-family privacy rules (Fully shared vault adopted instead)
- Live investment ticker tracking (Investment accounts will just have manually updated "total values" for MVP)

## Open Questions
- [ ] How will currency exchange rates be handled during a transfer? (Manual entry by user vs API lookup?)
- [ ] Do we need a dedicated "Refund" transaction type, or is it just an "Income" mapped to an "Expense" category?
- [ ] How will user authentication and "Family Group" invitations work in Supabase?
