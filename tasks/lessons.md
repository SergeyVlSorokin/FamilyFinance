# Lessons

## Architecture & Conventions

## Bugs & Anti-Patterns
- **SQLite CASCADE DELETE with Room REPLACE**: Avoid `@Insert(onConflict = OnConflictStrategy.REPLACE)` for master entities (Accounts, Categories, Projects) if they are parents of `ForeignKey.CASCADE` children (Transactions). Row replacement in SQLite acts as a DELETE/INSERT cycle, triggering the cascade and wiping child history. Use `@Upsert` or targeted `@Query` updates instead.

## Performance & Limitations

## Tooling & Environment
