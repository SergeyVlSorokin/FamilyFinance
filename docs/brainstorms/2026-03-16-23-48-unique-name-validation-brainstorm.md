# Brainstorm: Unique Name Validation

**Date**: 2026-03-16
**Task Slug**: `2026-03-16-23-48-unique-name-validation`

## Problem
Currently, the application allows creating Accounts, Categories, and Projects with duplicate names (except for Accounts, where the DB has a unique constraint but the UI doesn't handle the error gracefully). The user wants to prevent duplicates and keep the entry dialog open with an alert if a duplicate is entered.

## Current State Analysis
- **Accounts**: `AccountEntity` has `indices = [Index(value = ["name"], unique = true)]`. Saving a duplicate triggers a `SQLiteConstraintException`.
- **Categories**: No unique constraint in `CategoryEntity`.
- **Projects**: No unique constraint in `ProjectEntity`.
- **UI**: Dialogs close immediately on "Save" click without server-side/DB-level validation checks in the UI thread.

## Proposed Solutions

### Option 1: ViewModel-level Validation (Soft Lock)
- ViewModels (`SettingsViewModel`) check if the name exists in the current `UiState`.
- **Pros**: Fast, reactive, doesn't require DB migration.
- **Cons**: Potential race conditions (though unlikely in a local single-user app).

### Option 2: Database-level Constraints (Hard Lock)
- Add UNIQUE indices to `CategoryEntity` and `ProjectEntity`.
- **Pros**: Data integrity guaranteed at the source.
- **Cons**: Requires Room migration. Needs careful exception handling in the Repository/ViewModel.

### Option 3: Unified Validation Strategy (Recommended)
1. **DB Level**: Add `UNIQUE` indices to `CategoryEntity` and `ProjectEntity` (consistency with `AccountEntity`).
2. **Domain/Repository Level**: Add `isNameUnique(name: String, type: EntityType): Boolean` or handle exceptions and return a `Result` type.
3. **VM/UI Level**: 
   - Check for uniqueness BEFORE saving.
   - If duplicate: Show error message in the Dialog (Red text / Alert).
   - Do NOT close the dialog.

## Proposed UX Changes
- In `AddAccountDialog`, `AddCategoryDialog`, and `AddProjectDialog`:
  - Add an `errorText` state.
  - On "Save" click, instead of just calling `onSave`, valid name first.
  - Or even better: validate on-the-fly or through the confirm button click.
  - The `onSave` callback in the Screen should return success/failure or be preceded by a check.

## Next Steps
1. Create a `compound-plan` based on the chosen strategy.
2. Implement DB migrations if needed.
3. Update screens and ViewModels.
