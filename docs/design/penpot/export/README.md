# CrackMyDroid Penpot import bundle

This directory contains a standalone bundle for importing the current app
design into Penpot:

- `screens/*.svg`: one 720 × 1280 SVG for each documented Android screen/state.
  Each SVG embeds its original screenshot, so it remains self-contained and
  visually faithful. The screenshot pixels are not individually editable.
- `components/*-light.svg` and `components/*-dark.svg`: editable SVG references
  for the reusable Compose UI components, in both semantic themes.
- `design-tokens.json`: the DTCG tokens used by the app theme.
- `manifest.json`: screen/component source mapping, dimensions, and SHA-256
  digests.

## Import into Penpot

Open or create a Penpot file, then import the SVGs from `screens/` and
`components/` using **File → Import** or by dragging the files onto the canvas.
Import `design-tokens.json` separately as the design-token reference. The screen
frames contain an embedded raster image; component references contain editable
SVG shapes and text, but not Compose behavior.

## Regenerate and verify

From the repository root:

```bash
python3 scripts/penpot/generate_import_bundle.py
python3 scripts/penpot/generate_import_bundle.py --check
```

The generator reads the screenshot list from
`docs/design/exports/screenshots/manifest.json`, the source PNG captures, the
shared Compose component sources, and the canonical
`docs/design/exports/design-tokens.json`. It does not connect to or modify the
hosted Penpot file.
