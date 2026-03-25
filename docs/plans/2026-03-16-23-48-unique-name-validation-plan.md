# Plan: Unique Name Validation

**Date**: 2026-03-16
**Task Slug**: `2026-03-16-23-48-unique-name-validation`

## Goal
Prevent duplicate names for Accounts, Categories, and Projects with proper UI feedback.

## Proposed Changes

### 1. Database Level
- Add `@Index(value = ["name"], unique = true)` to `CategoryEntity` and `ProjectEntity`.
- Increment `FamilyFinanceDatabase` version to 2.
- (Dev decision): Use destructive migration for simplicity or add a manual migration if data preservation is required. Given it's a dev environment, destructive migration is faster but manual is safer. I'll stick to a simple version bump first.

### 2. Domain/Repository Level
- Add `isNameUnique` checks to `FinanceRepository`.
- Ensure `CreateAccountUseCase` handles name uniqueness correctly.

### 3. ViewModel Layer
- Update `SettingsViewModel` with a `validationError` state flow.
- Logic: Before calling `save...`, check if the name already exists in the current list.

### 4. UI Layer
- Update `Add...Dialog` in `AccountManageScreen`, `CategoryManageScreen`, and `ProjectManageScreen`.
- Display a red error message if the name exists.
- Keep dialog open on "Save" if validation fails.

## Verification
- Unit tests for DB constraints.
- Manual testing of all three screens.
