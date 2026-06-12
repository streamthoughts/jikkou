#!/usr/bin/env python3
"""Insert a URL into a Markdown page's YAML frontmatter `aliases` list. Idempotent.

Usage: inject_alias.py <file.md> <old_url>
"""
import sys
import pathlib

f = pathlib.Path(sys.argv[1])
url = sys.argv[2]
text = f.read_text(encoding="utf-8")
if not text.startswith("---\n"):
    raise SystemExit(f"{f}: no YAML frontmatter; cannot inject alias")
end = text.index("\n---", 4)          # end of frontmatter block
fm = text[4:end]
body = text[end:]                     # starts with "\n---"
lines = fm.splitlines()

if any(line.strip() == f"- {url}" for line in lines):
    sys.exit(0)                       # alias already present

out = []
if any(line.startswith("aliases:") for line in lines):
    inserted = False
    for line in lines:
        out.append(line)
        if line.startswith("aliases:") and not inserted:
            out.append(f"  - {url}")
            inserted = True
else:
    out = lines + ["aliases:", f"  - {url}"]

f.write_text("---\n" + "\n".join(out) + body, encoding="utf-8")
