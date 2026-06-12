#!/usr/bin/env python3
"""Print the Hugo pretty URL for a content file (default permalinks, no uglyURLs).

Usage: hugo_url.py <path-relative-to-content/en>
Example: hugo_url.py "docs/Jikkou CLI/Commands/jikkou-apply.md"  ->  /docs/jikkou-cli/commands/jikkou-apply/
"""
import sys
import pathlib

p = pathlib.Path(sys.argv[1])
parts = list(p.parts)
name = parts[-1]
if name in ("_index.md", "index.md"):
    segs = parts[:-1]                       # section / page-bundle -> directory URL
else:
    segs = parts[:-1] + [name[:-3]]         # leaf page -> strip ".md"
slug = "/".join(s.lower().replace(" ", "-") for s in segs)
print("/" + slug + "/")
