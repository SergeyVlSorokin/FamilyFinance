#!/usr/bin/env python3
"""
housekeeping.py — Interactive maintenance for engram Compound task chains.

Finds unclosed chains in tasks/dashboard.md and offers to close them.

Usage (run from project root):
    python3 scripts/housekeeping.py

Findings:
  - Pending queue items    [ ] unchecked actions in Queue section
  - Stale closed items     [x] checked items (offer to clean up)
  - Stranded tasks         Active task with all phases done but Phase != Done
"""

import sys
from pathlib import Path

DASHBOARD = Path("tasks/dashboard.md")

# Map queue action name → skill to suggest when user picks [a]sk Claude
SKILL_HINTS = {
    "plan":         "/compound-plan",
    "plan-review":  "/compound-plan-review",
    "work":         "/compound-work",
    "code-review":  "/compound-review",
    "compound":     "/compound-compound",
    "result-log":   "/compound-result-log",
    "fix-review":   "/compound-work  (re-open work phase)",
}

DONE_ICONS = {"✅"}
ACTIVE_ICONS = {"🔄", "⏳", "❌", "⚠️"}


# ── Parsing helpers ────────────────────────────────────────────────────────────

def read_dashboard() -> str:
    if not DASHBOARD.exists():
        print(f"Error: {DASHBOARD} not found. Run from project root.")
        sys.exit(1)
    return DASHBOARD.read_text()


def parse_active_tasks(text: str) -> list[dict]:
    """Return list of task dicts from the Active Tasks table."""
    tasks = []
    in_table = False
    for line in text.splitlines():
        if line.startswith("## Active Tasks"):
            in_table = True
            continue
        if in_table and line.startswith("##"):
            break
        if not in_table:
            continue
        if not line.startswith("|"):
            continue
        cells = [c.strip() for c in line.split("|")]
        # cells[0] is empty (before first |), cells[-1] is empty (after last |)
        cells = cells[1:-1]
        if len(cells) < 9:
            continue
        # Skip header rows and separator rows
        if cells[0] in ("Slug", "") or cells[0].startswith("---") or "_no active" in cells[0]:
            continue
        tasks.append({
            "slug":     cells[0],
            "phase":    cells[1],
            "B":        cells[2],
            "P":        cells[3],
            "PR":       cells[4],
            "W":        cells[5],
            "R":        cells[6],
            "priority": cells[7],
            "started":  cells[8],
            "raw_line": line,
        })
    return tasks


def parse_queue(text: str) -> list[dict]:
    """Return list of queue item dicts."""
    items = []
    in_queue = False
    for line in text.splitlines():
        if line.startswith("## Queue"):
            in_queue = True
            continue
        if in_queue and line.startswith("##"):
            break
        if not in_queue:
            continue
        if line.startswith("- [ ] ") or line.startswith("- [x] "):
            done = line.startswith("- [x] ")
            body = line[6:].strip()
            # Extract action (first word/token before |)
            action = body.split("|")[0].strip()
            items.append({
                "done":     done,
                "action":   action,
                "body":     body,
                "raw_line": line,
            })
    return items


def is_stranded(task: dict) -> bool:
    """Task has at least one ✅ phase indicator but Phase column isn't Done."""
    if task["phase"].lower() in ("done", ""):
        return False
    phase_vals = [task["B"], task["P"], task["PR"], task["W"], task["R"]]
    # All non-empty phase indicators are ✅
    filled = [v for v in phase_vals if v and v != "—" and v != "-"]
    if not filled:
        return False
    return all(v in DONE_ICONS for v in filled)


# ── Interactive prompt ─────────────────────────────────────────────────────────

def prompt(label: str, options: list[tuple[str, str]]) -> str:
    """Print label + options, return chosen key."""
    opts_str = "  ".join(f"[{k}] {desc}" for k, desc in options)
    while True:
        try:
            choice = input(f"\n  {label}\n  {opts_str}\n  > ").strip().lower()
        except (EOFError, KeyboardInterrupt):
            print("\nAborted.")
            sys.exit(0)
        valid = {k for k, _ in options}
        if choice in valid:
            return choice
        print(f"  Invalid. Choose one of: {', '.join(sorted(valid))}")


# ── Actions ────────────────────────────────────────────────────────────────────

def mark_queue_done(text: str, raw_line: str) -> str:
    """Replace `- [ ]` with `- [x]` for the given raw_line."""
    return text.replace(raw_line, raw_line.replace("- [ ] ", "- [x] ", 1), 1)


def remove_queue_line(text: str, raw_line: str) -> str:
    """Remove a queue line (and its trailing newline) from text."""
    return text.replace(raw_line + "\n", "", 1).replace(raw_line, "", 1)


def update_task_phase(text: str, raw_line: str, new_phase: str) -> str:
    """Replace the Phase cell in the task table row."""
    cells = raw_line.split("|")
    if len(cells) >= 3:
        cells[2] = f" {new_phase} "
        new_line = "|".join(cells)
        return text.replace(raw_line, new_line, 1)
    return text


# ── Main ───────────────────────────────────────────────────────────────────────

def main() -> None:
    text = read_dashboard()
    tasks = parse_active_tasks(text)
    queue = parse_queue(text)

    pending_items = [q for q in queue if not q["done"]]
    stale_items   = [q for q in queue if q["done"]]
    stranded      = [t for t in tasks if is_stranded(t)]

    total = len(pending_items) + len(stale_items) + len(stranded)
    if total == 0:
        print("✓ All clear — no unclosed chains found.")
        return

    print(f"\nengram housekeeping — found {total} item(s)\n")

    stats = {"closed": 0, "skipped": 0, "ask": 0, "cleaned": 0}

    # ── 1. Pending queue items ─────────────────────────────────────────────────
    for item in pending_items:
        action = item["action"]
        skill  = SKILL_HINTS.get(action, f"/{action}")
        label  = f"Pending queue: {action}  |  {item['body']}"
        choice = prompt(label, [("c", "close"), ("s", "skip"), ("a", f"ask Claude ({skill})")])

        if choice == "c":
            text = mark_queue_done(text, item["raw_line"])
            stats["closed"] += 1
            print(f"  ✓ Marked as done: {action}")
        elif choice == "s":
            stats["skipped"] += 1
        else:
            print(f"  → Run: {skill}")
            stats["ask"] += 1

    # ── 2. Stranded tasks ──────────────────────────────────────────────────────
    for task in stranded:
        label = f"Stranded task: `{task['slug']}` (Phase: {task['phase']}, all phases ✅)"
        choice = prompt(label, [("c", "mark Done"), ("s", "skip"), ("a", "ask Claude (/compound-result-log)")])

        if choice == "c":
            text = update_task_phase(text, task["raw_line"], "Done")
            stats["closed"] += 1
            print(f"  ✓ Phase set to Done: {task['slug']}")
        elif choice == "s":
            stats["skipped"] += 1
        else:
            print("  → Run: /compound-result-log")
            stats["ask"] += 1

    # ── 3. Stale closed items ──────────────────────────────────────────────────
    if stale_items:
        count = len(stale_items)
        label = f"{count} closed [x] queue item(s) — remove to keep Queue tidy?"
        choice = prompt(label, [("c", "remove all"), ("s", "skip")])

        if choice == "c":
            for item in stale_items:
                text = remove_queue_line(text, item["raw_line"])
            stats["cleaned"] = count
            print(f"  ✓ Removed {count} closed item(s)")
        else:
            stats["skipped"] += count

    # ── Write back if changed ──────────────────────────────────────────────────
    original = read_dashboard()
    if text != original:
        DASHBOARD.write_text(text)
        print(f"\n  tasks/dashboard.md  updated")

    print(
        f"\nHousekeeping complete. "
        f"Closed: {stats['closed']}  "
        f"Skipped: {stats['skipped']}  "
        f"Ask-Claude: {stats['ask']}  "
        f"Cleaned: {stats['cleaned']}\n"
    )


if __name__ == "__main__":
    main()
