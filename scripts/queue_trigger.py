#!/usr/bin/env python3
"""
queue_trigger.py — PostToolUse hook for Claude Code.

Triggered when Write tool writes a file. If the file matches a known
docs/ pattern, appends a pending Queue item to tasks/dashboard.md.

Usage (in .claude/settings.json):
  {
    "hooks": {
      "PostToolUse": [{
        "matcher": "Write",
        "hooks": [{"type": "command", "command": "python3 scripts/queue_trigger.py"}]
      }]
    }
  }

Claude Code passes tool input via stdin as JSON:
  {"file_path": "...", "content": "..."}
"""

import json
import sys
import re
from datetime import datetime
from pathlib import Path

DASHBOARD = Path("tasks/dashboard.md")
QUEUE_MARKER = "## Queue (pending agent actions)"

RULES = [
    (r"docs/brainstorms/.+-brainstorm\.md$",   "plan",        "docs/brainstorms/{file}"),
    (r"docs/plans/.+-plan\.md$",               "plan-review", "docs/plans/{file}"),
    (r"docs/plan-reviews/.+-plan-review\.md$", "work",        "docs/plan-reviews/{file}"),
    (r"docs/reviews/.+-code-review\.md$",      "compound",    "slug from file"),
]


def extract_slug(path: str) -> str:
    name = Path(path).stem  # e.g. 2026-02-23-16-56-auth-system-plan
    # strip trailing known suffixes
    for suffix in ("-brainstorm", "-plan-review", "-plan", "-code-review"):
        if name.endswith(suffix):
            return name[: -len(suffix)]
    return name


def build_queue_line(file_path: str) -> str | None:
    now = datetime.now().strftime("%Y-%m-%d-%H-%M")
    for pattern, action, ref_template in RULES:
        if re.search(pattern, file_path):
            fname = Path(file_path).name
            slug = extract_slug(file_path)
            ref = ref_template.format(file=fname, slug=slug)
            return f"- [ ] {action:<12} | `{ref}` | created: {now}"
    return None


def append_to_queue(line: str) -> None:
    if not DASHBOARD.exists():
        return
    text = DASHBOARD.read_text()
    if line in text:
        return  # already queued
    if QUEUE_MARKER not in text:
        return
    # insert after queue marker + blank line
    insert_after = QUEUE_MARKER + "\n\n"
    if insert_after not in text:
        insert_after = QUEUE_MARKER + "\n"
    text = text.replace(insert_after, insert_after + line + "\n", 1)
    DASHBOARD.write_text(text)


def main() -> None:
    try:
        data = json.load(sys.stdin)
        file_path = data.get("file_path", "")
    except (json.JSONDecodeError, KeyError):
        return

    line = build_queue_line(file_path)
    if line:
        append_to_queue(line)
        print(f"[queue_trigger] Added: {line.strip()}")


if __name__ == "__main__":
    main()
