#!/usr/bin/env python3
"""Check local Markdown links and GitHub-style heading fragments."""

import html
import re
import subprocess
import sys
from pathlib import Path
from urllib.parse import unquote, urlsplit


LINK_PATTERN = re.compile(r"(?<!!)\[[^\]]+\]\(([^)]+)\)")
HEADING_PATTERN = re.compile(r"^ {0,3}#{1,6}\s+(.+?)\s*#*\s*$", re.MULTILINE)
ANCHOR_PATTERN = re.compile(r"<a\s+(?:id|name)=([\"'])([^\"']+)\1", re.IGNORECASE)
FENCE_PATTERN = re.compile(r"^ {0,3}(`{3,}|~{3,})")


def markdown_files(root):
    output = subprocess.check_output(
        ["git", "ls-files", "-co", "--exclude-standard", "-z", "--", "*.md"],
        cwd=root,
    )
    return [root / path.decode() for path in output.split(b"\0") if path]


def without_fenced_code(content):
    lines = []
    fence_char = None
    fence_length = 0
    for line in content.splitlines():
        match = FENCE_PATTERN.match(line)
        if match:
            fence = match.group(1)
            if fence_char is None:
                fence_char, fence_length = fence[0], len(fence)
            elif fence[0] == fence_char and len(fence) >= fence_length:
                fence_char, fence_length = None, 0
            continue
        if fence_char is None:
            lines.append(line)
    return "\n".join(lines)


def heading_slug(heading):
    heading = html.unescape(heading)
    heading = re.sub(r"`([^`]*)`", r"\1", heading)
    heading = re.sub(r"<[^>]+>", "", heading)
    heading = re.sub(r"!?\[([^\]]*)\]\([^)]*\)", r"\1", heading)
    heading = heading.lower()
    heading = re.sub(r"[^\w\-\s]", "", heading, flags=re.UNICODE)
    return re.sub(r"\s", "-", heading)


def fragments_by_file(files):
    result = {}
    for path in files:
        content = without_fenced_code(path.read_text(encoding="utf-8"))
        used = set()
        fragments = set()
        for heading in HEADING_PATTERN.finditer(content):
            base = heading_slug(heading.group(1))
            slug = base
            suffix = 0
            while slug in used:
                suffix += 1
                slug = f"{base}-{suffix}"
            used.add(slug)
            fragments.add(slug)
        fragments.update(match.group(2) for match in ANCHOR_PATTERN.finditer(content))
        result[path.resolve()] = fragments
    return result


def link_target(raw_target, source, root):
    target = raw_target.strip()
    if target.startswith("<") and ">" in target:
        target = target[1 : target.index(">")]
    else:
        target = target.split(maxsplit=1)[0]
    parsed = urlsplit(target)
    if parsed.scheme or parsed.netloc:
        return None, None
    path = unquote(parsed.path)
    if not path:
        resolved = source
    elif path.startswith("/"):
        resolved = root / path.lstrip("/")
    else:
        resolved = source.parent / path
    fragment = unquote(parsed.fragment)
    return resolved.resolve(), fragment


def main():
    root = Path(sys.argv[1]).resolve()
    files = markdown_files(root)
    fragments = fragments_by_file(files)
    errors = []
    checked = 0
    for source in files:
        content = without_fenced_code(source.read_text(encoding="utf-8"))
        for match in LINK_PATTERN.finditer(content):
            resolved, fragment = link_target(match.group(1), source, root)
            if resolved is None:
                continue
            checked += 1
            if not resolved.exists():
                errors.append(f"{source.relative_to(root)}: missing link target {match.group(1)}")
            elif fragment and resolved.suffix.lower() == ".md":
                known = fragments.get(resolved)
                if known is None or fragment not in known:
                    errors.append(
                        f"{source.relative_to(root)}: missing heading anchor {match.group(1)}"
                    )

    if errors:
        print("\n".join(errors), file=sys.stderr)
        print(f"Markdown link check failed ({len(errors)} issue(s)).", file=sys.stderr)
        return 1
    print(f"Markdown link check passed ({checked} local links, {len(files)} files).")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
