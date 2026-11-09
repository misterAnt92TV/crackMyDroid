#!/usr/bin/env python3
"""Generate a self-contained SVG bundle that can be imported into Penpot."""

import argparse
import base64
import hashlib
import json
import re
import sys
import xml.etree.ElementTree as ET
from pathlib import Path
from xml.sax.saxutils import escape


ROOT = Path(__file__).resolve().parents[2]
SCREEN_MANIFEST = ROOT / "docs/design/exports/screenshots/manifest.json"
TOKEN_SOURCE = ROOT / "docs/design/exports/design-tokens.json"
OUTPUT = ROOT / "docs/design/penpot/export"
SCREEN_OUTPUT = Path("screens")
COMPONENT_OUTPUT = Path("components")

COMPONENTS = (
    ("AppAccordion", "presentation/components/AppAccordion.kt", "accordion"),
    ("AppButton", "presentation/components/AppButton.kt", "buttons"),
    ("AppCard", "presentation/components/card/AppCard.kt", "card"),
    ("AppDialog", "presentation/components/AppDialog.kt", "dialog"),
    ("AppListItem", "presentation/components/AppListItem.kt", "list_item"),
    ("AppProgressIndicator", "presentation/components/AppProgressIndicator.kt", "progress"),
    ("AppText", "presentation/components/text/AppText.kt", "text"),
    ("AppTextField", "presentation/components/AppTextField.kt", "text_field"),
    ("CardBlock", "presentation/components/card/CardBlock.kt", "card_block"),
    ("ErrorDialogComponent", "presentation/components/ErrorDialogComponent.kt", "error_dialog"),
    ("ExportDetailsCard", "presentation/components/card/ExportDetailsCard.kt", "export_details"),
    ("LastOperationItemComponent", "ui/home/view/LastOperationItemComponent.kt", "operation"),
    ("PackageIcon", "presentation/components/AppIcon.kt", "package_icon"),
    ("PenTestCheckItemComponent", "presentation/components/pentest/PenTestAtomics.kt", "check_item"),
    ("PenTestChecksListComponent", "presentation/components/pentest/PenTestChecksList.kt", "check_list"),
    ("PenTestProgressHeaderComponent", "presentation/components/pentest/PenTestAtomics.kt", "check_progress"),
    ("ScrollToTopButton", "presentation/components/ScrollToTopButton.kt", "scroll_top"),
    ("SearchFieldWithHistoryComponent", "presentation/components/SearchFieldWithHistory.kt", "search"),
    ("SectionHeader", "presentation/components/SectionHeader.kt", "section_header"),
)

PALETTES = {
    "light": {
        "background": "#F7F9FC",
        "surface": "#FFFFFF",
        "surface_variant": "#E9EDF4",
        "on_surface": "#1C1F26",
        "on_surface_variant": "#444A56",
        "primary": "#1A73E8",
        "on_primary": "#FFFFFF",
        "primary_container": "#D2E3FC",
        "secondary": "#188038",
        "secondary_container": "#CEEAD6",
        "error": "#D93025",
        "error_container": "#FDE8E7",
        "outline": "#737A88",
        "outline_variant": "#C2C8D3",
    },
    "dark": {
        "background": "#101418",
        "surface": "#101418",
        "surface_variant": "#3F4652",
        "on_surface": "#E3E8F0",
        "on_surface_variant": "#C0C7D2",
        "primary": "#A8C7FA",
        "on_primary": "#053062",
        "primary_container": "#174EA6",
        "secondary": "#81C995",
        "secondary_container": "#0F5223",
        "error": "#F28B82",
        "error_container": "#8C1D18",
        "outline": "#8A92A1",
        "outline_variant": "#3F4652",
    },
}


def png_dimensions(path):
    data = path.read_bytes()
    if data[:8] != b"\x89PNG\r\n\x1a\n":
        raise ValueError(f"Expected a PNG screenshot: {path}")
    return int.from_bytes(data[16:20], "big"), int.from_bytes(data[20:24], "big")


def public_component_names(directory):
    function_pattern = re.compile(r"(?m)^fun\s+([A-Z][A-Za-z0-9_]*)\s*\(")
    return {
        name
        for path in directory.rglob("*.kt")
        for name in function_pattern.findall(path.read_text(encoding="utf-8"))
    }


def svg_start(title, description, width, height, source):
    return [
        f'<svg xmlns="http://www.w3.org/2000/svg" width="{width}" height="{height}" '
        f'viewBox="0 0 {width} {height}" role="img" '
        f'data-source="{escape(source, {chr(34): "&quot;"})}">',
        f"<title>{escape(title)}</title>",
        f"<desc>{escape(description)}</desc>",
    ]


def rect(x, y, width, height, fill, radius=0, stroke=None, stroke_width=1):
    attrs = (
        f'x="{x}" y="{y}" width="{width}" height="{height}" fill="{fill}" '
        f'rx="{radius}"'
    )
    if stroke:
        attrs += f' stroke="{stroke}" stroke-width="{stroke_width}"'
    return f"<rect {attrs}/>"


def circle(cx, cy, radius, fill, stroke=None, stroke_width=1):
    attrs = f'cx="{cx}" cy="{cy}" r="{radius}" fill="{fill}"'
    if stroke:
        attrs += f' stroke="{stroke}" stroke-width="{stroke_width}"'
    return f"<circle {attrs}/>"


def line(x1, y1, x2, y2, color, stroke_width=2):
    return (
        f'<line x1="{x1}" y1="{y1}" x2="{x2}" y2="{y2}" '
        f'stroke="{color}" stroke-width="{stroke_width}" stroke-linecap="round"/>'
    )


def text(x, y, value, color, size=20, weight=400):
    return (
        f'<text x="{x}" y="{y}" fill="{color}" font-family="Inter, Roboto, sans-serif" '
        f'font-size="{size}" font-weight="{weight}">{escape(value)}</text>'
    )


def icon_badge(x, y, palette, symbol="!"):
    return "".join(
        [
            circle(x + 22, y + 22, 22, palette["primary_container"]),
            text(x + 14, y + 30, symbol, palette["primary"], 22, 700),
        ]
    )


def component_art(kind, p):
    surface = p["surface"]
    foreground = p["on_surface"]
    secondary = p["on_surface_variant"]
    primary = p["primary"]
    border = p["outline_variant"]
    frame = rect(24, 24, 672, 232, p["background"], 22, border)
    parts = [frame]

    if kind == "accordion":
        parts += [
            rect(48, 48, 624, 184, surface, 16, border),
            icon_badge(68, 66, p, "A"),
            text(126, 91, "com.example.security", foreground, 19, 600),
            text(126, 119, "com.example.security.app", secondary, 15),
            text(126, 148, "3 results", primary, 14, 500),
            text(630, 98, "⌃", primary, 25, 600),
            line(68, 164, 652, 164, border),
            text(76, 195, "No unexpected backup or debug permissions.", foreground, 15),
        ]
    elif kind == "buttons":
        parts += [
            rect(54, 51, 612, 54, primary, 18),
            text(253, 85, "Analyze device", p["on_primary"], 18, 600),
            rect(54, 123, 292, 48, surface, 18, primary, 2),
            text(142, 154, "Details", primary, 16, 600),
            rect(362, 123, 304, 48, p["primary_container"], 18),
            text(464, 154, "Export report", foreground, 16, 600),
            rect(54, 188, 612, 42, p["surface_variant"], 16),
            text(274, 215, "Disabled action", secondary, 15, 500),
        ]
    elif kind == "card":
        parts += [
            rect(48, 54, 624, 172, surface, 18, border),
            text(76, 96, "Analysis result", foreground, 21, 600),
            text(76, 132, "Tap the card to view the details.", secondary, 16),
            rect(76, 164, 126, 36, p["primary_container"], 12),
            text(96, 188, "View details", primary, 14, 600),
        ]
    elif kind == "dialog":
        parts += [
            rect(48, 44, 624, 190, surface, 24, border),
            text(78, 84, "Confirm action", foreground, 22, 600),
            text(78, 120, "This operation will scan the selected device.", secondary, 16),
            text(78, 146, "You can cancel at any time.", secondary, 16),
            rect(376, 174, 116, 40, p["primary_container"], 14),
            text(400, 200, "Cancel", primary, 14, 600),
            rect(506, 174, 138, 40, primary, 14),
            text(535, 200, "Confirm", p["on_primary"], 14, 600),
        ]
    elif kind == "list_item":
        parts += [
            rect(48, 54, 624, 172, surface, 16, border),
            icon_badge(68, 77, p, "i"),
            text(126, 94, "Security analysis", foreground, 19, 600),
            text(126, 124, "Review the latest device findings.", secondary, 15),
            text(126, 151, "Updated just now", primary, 14, 500),
            text(628, 118, "›", primary, 29, 400),
        ]
    elif kind == "progress":
        parts += [
            text(64, 89, "Analyzing device", foreground, 20, 600),
            rect(64, 116, 592, 12, p["surface_variant"], 6),
            rect(64, 116, 382, 12, primary, 6),
            text(64, 163, "64%", primary, 15, 600),
            text(132, 163, "Checks running", secondary, 15),
            line(64, 190, 656, 190, border),
            text(64, 222, "Reduced-motion mode uses a status label.", secondary, 13),
        ]
    elif kind == "text":
        parts += [
            text(64, 82, "Section title", foreground, 25, 700),
            text(64, 126, "Body text with normal emphasis.", foreground, 17),
            text(64, 165, "Supporting information", secondary, 14),
            text(64, 207, "Attention required", p["error"], 16, 600),
        ]
    elif kind == "text_field":
        parts += [
            rect(54, 62, 612, 72, surface, 14, border, 2),
            rect(76, 53, 134, 20, p["background"]),
            text(82, 69, "Device name", primary, 13, 500),
            text(82, 108, "Pixel device", foreground, 17),
            text(58, 163, "Enter a name to continue", p["error"], 13),
        ]
    elif kind == "card_block":
        parts += [
            rect(48, 50, 624, 182, surface, 18, border),
            circle(87, 91, 17, p["primary_container"]),
            text(79, 98, "↓", primary, 20, 700),
            text(118, 98, "Export complete", foreground, 20, 600),
            text(76, 143, "The report is ready to share.", foreground, 16),
            text(76, 180, "Saved to Downloads", secondary, 14),
        ]
    elif kind == "error_dialog":
        parts += [
            rect(48, 44, 624, 190, surface, 24, border),
            circle(91, 89, 20, p["error_container"]),
            text(85, 97, "!", p["error"], 19, 700),
            text(126, 94, "Operation failed", foreground, 21, 600),
            text(78, 137, "The operation could not be completed.", secondary, 16),
            text(78, 162, "Please try again in a moment.", secondary, 16),
            rect(526, 178, 116, 40, primary, 14),
            text(563, 204, "OK", p["on_primary"], 14, 600),
        ]
    elif kind == "export_details":
        parts += [
            rect(48, 46, 624, 190, surface, 18, border),
            text(72, 81, "Export details", foreground, 19, 600),
            text(72, 111, "PenTest report generated successfully", secondary, 14),
            text(72, 145, "File  crackmydroid-report.txt", foreground, 14),
            text(72, 171, "Format  TXT", primary, 14, 500),
            text(72, 198, "Path  /storage/emulated/0/Download", secondary, 13),
            text(72, 221, "12 checks run  ·  2 warnings", secondary, 13),
        ]
    elif kind == "operation":
        parts += [
            rect(48, 46, 624, 190, surface, 18, border),
            circle(92, 91, 20, p["secondary_container"]),
            text(83, 98, "✓", p["secondary"], 18, 700),
            text(128, 87, "APK scan completed", foreground, 17, 500),
            text(128, 115, "Result: 42 applications", secondary, 14),
            text(128, 143, "Today, 10:30", secondary, 13),
            text(634, 92, "×", secondary, 22),
            line(76, 165, 644, 165, border),
            circle(92, 199, 16, p["error_container"]),
            text(86, 205, "!", p["error"], 15, 700),
            text(128, 205, "Activity launch failed", p["error"], 14, 500),
        ]
    elif kind == "package_icon":
        parts += [
            circle(360, 140, 66, p["secondary_container"]),
            rect(328, 117, 64, 48, p["secondary"], 24),
            circle(343, 138, 4, p["on_primary"]),
            circle(377, 138, 4, p["on_primary"]),
            line(338, 106, 329, 94, p["secondary"], 4),
            line(382, 106, 391, 94, p["secondary"], 4),
            text(286, 224, "Fallback Android icon · 64 dp", secondary, 14),
        ]
    elif kind == "check_item":
        parts += [
            rect(48, 46, 624, 190, surface, 18, border),
            circle(88, 85, 14, p["secondary_container"]),
            text(81, 91, "✓", p["secondary"], 14, 700),
            text(120, 91, "Manifest flags", foreground, 16, 500),
            circle(88, 132, 14, p["error_container"]),
            text(83, 138, "!", p["error"], 15, 700),
            text(120, 130, "Library scan", foreground, 16, 500),
            text(120, 151, "Known dependency warning", p["error"], 12),
            circle(88, 196, 14, p["surface_variant"]),
            text(81, 202, "…", secondary, 14, 700),
            text(120, 202, "Permission risk · queued", secondary, 15),
        ]
    elif kind == "check_list":
        parts += [
            rect(48, 42, 624, 198, surface, 18, border),
            text(72, 75, "PenTest checks", foreground, 18, 600),
            circle(88, 111, 12, p["secondary_container"]),
            text(82, 116, "✓", p["secondary"], 12, 700),
            text(112, 117, "Manifest flags", foreground, 14),
            line(72, 136, 648, 136, border),
            circle(88, 157, 12, p["error_container"]),
            text(84, 162, "!", p["error"], 12, 700),
            text(112, 163, "Library scan · dependency warning", foreground, 14),
            line(72, 182, 648, 182, border),
            circle(88, 203, 12, p["surface_variant"]),
            text(83, 208, "…", secondary, 12, 700),
            text(112, 209, "Permission risk · queued", foreground, 14),
        ]
    elif kind == "check_progress":
        parts += [
            text(64, 77, "PenTest progress", foreground, 19, 600),
            rect(64, 96, 592, 12, p["surface_variant"], 6),
            rect(64, 96, 412, 12, primary, 6),
            text(64, 145, "✓ 5", p["secondary"], 16, 700),
            text(190, 145, "× 2", p["error"], 16, 700),
            text(316, 145, "◷ 3", secondary, 16, 600),
            text(64, 192, "7 of 10 checks complete", secondary, 14),
        ]
    elif kind == "scroll_top":
        parts += [
            circle(360, 140, 38, primary),
            line(360, 157, 360, 122, p["on_primary"], 4),
            line(346, 136, 360, 122, p["on_primary"], 4),
            line(374, 136, 360, 122, p["on_primary"], 4),
            text(278, 216, "Floating action · scroll to top", secondary, 14),
        ]
    elif kind == "search":
        parts += [
            rect(52, 48, 616, 68, p["surface_variant"], 16),
            text(76, 73, "Search permissions", secondary, 12),
            text(76, 99, "camera", foreground, 16),
            text(630, 92, "×", secondary, 20),
            rect(54, 137, 162, 42, p["primary_container"], 20),
            circle(76, 158, 8, primary),
            text(93, 164, "camera", foreground, 14),
            rect(230, 137, 178, 42, p["surface_variant"], 20),
            circle(252, 158, 8, primary),
            text(269, 164, "location", foreground, 14),
            text(56, 211, "Search history suggestions", secondary, 13),
        ]
    elif kind == "section_header":
        parts += [
            rect(48, 74, 624, 94, surface, 20, border),
            circle(93, 121, 22, p["primary_container"]),
            text(84, 129, "☰", primary, 17, 600),
            text(218, 129, "Device info", foreground, 21, 600),
            circle(628, 121, 22, p["primary_container"]),
            text(622, 128, "?", primary, 18, 600),
        ]
    else:
        raise ValueError(f"Missing component artwork for {kind}")

    return "\n".join(parts)


def component_svg(name, source, kind, theme):
    title = f"{name} · {theme} theme"
    description = f"Editable SVG visual reference for {name}, based on {source}."
    lines = svg_start(title, description, 720, 280, source)
    lines.append(rect(0, 0, 720, 280, PALETTES[theme]["background"]))
    lines.append(component_art(kind, PALETTES[theme]))
    lines.append("</svg>")
    return "\n".join(lines) + "\n"


def build_outputs():
    screenshot_data = json.loads(SCREEN_MANIFEST.read_text(encoding="utf-8"))
    tokens = json.loads(TOKEN_SOURCE.read_text(encoding="utf-8"))
    source_root = ROOT / "shared/src/commonMain/kotlin/com/crackmydroid/shared"
    component_root = source_root / "presentation/components"
    discovered_components = public_component_names(component_root)
    exported_components = {
        name
        for name, source, _ in COMPONENTS
        if source.startswith("presentation/components/")
    }
    if discovered_components != exported_components:
        missing = sorted(discovered_components - exported_components)
        stale = sorted(exported_components - discovered_components)
        raise ValueError(
            f"Component inventory mismatch; missing exports={missing}, stale exports={stale}"
        )
    operation_source = source_root / "ui/home/view/LastOperationItemComponent.kt"
    if "LastOperationItemComponent" not in public_component_names(operation_source.parent):
        raise ValueError("LastOperationItemComponent is no longer declared in its source file")

    files = {}
    assets = {"screens": [], "components": []}

    seen_screens = set()
    expected_dimensions = tuple(
        int(dimension) for dimension in screenshot_data["capture"]["dimensions"].split("x")
    )
    for screen in screenshot_data["screens"]:
        screenshot = Path(screen["image"])
        if screenshot.is_absolute() or ".." in screenshot.parts:
            raise ValueError(f"Unsafe screenshot path: {screenshot}")
        source = ROOT / "docs/design/exports/screenshots" / screenshot
        if not source.is_file():
            raise FileNotFoundError(f"Missing screen capture: {source}")
        slug = screenshot.stem
        if slug in seen_screens:
            raise ValueError(f"Duplicate screen asset name: {slug}")
        seen_screens.add(slug)
        width, height = png_dimensions(source)
        if (width, height) != expected_dimensions:
            raise ValueError(
                f"Screenshot dimensions for {source} are {width}x{height}, "
                f"expected {expected_dimensions[0]}x{expected_dimensions[1]}"
            )
        image_bytes = source.read_bytes()
        image = base64.b64encode(image_bytes).decode("ascii")
        title = f"{screen['name']} — {screen['state']}"
        description = f"{screen['name']}. {screen['state']}. Route: {screen['route']}."
        output = SCREEN_OUTPUT / f"{slug}.svg"
        lines = svg_start(title, description, width, height, str(source.relative_to(ROOT)))
        lines.append(
            f'<image x="0" y="0" width="{width}" height="{height}" '
            f'preserveAspectRatio="none" href="data:image/png;base64,{image}"/>'
        )
        lines.append("</svg>")
        svg = "\n".join(lines) + "\n"
        ET.fromstring(svg)
        if base64.b64decode(image) != image_bytes:
            raise ValueError(f"Embedded screenshot did not round-trip: {source}")
        files[OUTPUT / output] = svg
        assets["screens"].append(
            {
                "name": screen["name"],
                "state": screen["state"],
                "route": screen["route"],
                "source": str(source.relative_to(ROOT)),
                "file": str(output),
                "format": "svg-with-embedded-png",
                "width": width,
                "height": height,
                "sha256": hashlib.sha256(image_bytes).hexdigest(),
            }
        )

    seen_components = set()
    for name, source_relative, kind in COMPONENTS:
        if name in seen_components:
            raise ValueError(f"Duplicate component: {name}")
        seen_components.add(name)
        source = source_root / source_relative
        if not source.is_file():
            raise FileNotFoundError(f"Missing Compose component source: {source}")
        source_path = str(source.relative_to(ROOT))
        variants = []
        for theme in PALETTES:
            filename = f"{name.lower().replace('component', '')}-{theme}.svg"
            output = COMPONENT_OUTPUT / filename
            svg = component_svg(name, source_path, kind, theme)
            ET.fromstring(svg)
            files[OUTPUT / output] = svg
            variants.append(str(output))
        assets["components"].append(
            {
                "name": name,
                "source": source_path,
                "format": "editable-svg",
                "variants": variants,
            }
        )

    files[OUTPUT / "design-tokens.json"] = json.dumps(tokens, indent=2, ensure_ascii=False) + "\n"
    for asset in assets["screens"]:
        asset["exportSha256"] = hashlib.sha256(
            files[OUTPUT / asset["file"]].encode("utf-8")
        ).hexdigest()
    for component in assets["components"]:
        component["exports"] = [
            {
                "file": filename,
                "sha256": hashlib.sha256(files[OUTPUT / filename].encode("utf-8")).hexdigest(),
            }
            for filename in component.pop("variants")
        ]

    manifest = {
        "schemaVersion": 1,
        "application": "CrackMyDroid",
        "source": {
            "screenshots": "docs/design/exports/screenshots/manifest.json",
            "tokens": str(TOKEN_SOURCE.relative_to(ROOT)),
            "componentRoot": "shared/src/commonMain/kotlin/com/crackmydroid/shared",
            "capture": screenshot_data["capture"],
        },
        "tokens": "design-tokens.json",
        "screens": assets["screens"],
        "components": assets["components"],
        "notes": [
            "Screen SVGs embed the original PNG capture as a data URI; their screenshot pixels are not individually editable.",
            "Component SVGs use editable SVG shapes and text as visual references and do not include Compose runtime behavior.",
            "Component exports include light and dark palettes. Screen captures reflect the source screenshot manifest's captured theme.",
        ],
    }
    files[OUTPUT / "manifest.json"] = json.dumps(manifest, indent=2, ensure_ascii=False) + "\n"
    return files


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument(
        "--check",
        action="store_true",
        help="verify generated files are present and current without writing",
    )
    args = parser.parse_args()
    outputs = build_outputs()
    stale = []
    for path, content in outputs.items():
        expected = content.encode("utf-8")
        if args.check:
            if not path.is_file() or path.read_bytes() != expected:
                stale.append(path.relative_to(ROOT))
        else:
            path.parent.mkdir(parents=True, exist_ok=True)
            path.write_bytes(expected)
    if args.check and stale:
        print("Penpot export is missing or stale:", file=sys.stderr)
        for path in stale:
            print(f"  {path}", file=sys.stderr)
        return 1
    if args.check:
        print(f"Penpot export is current ({len(outputs)} generated files).")
    else:
        print(f"Generated {len(outputs)} Penpot export files under {OUTPUT.relative_to(ROOT)}.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
