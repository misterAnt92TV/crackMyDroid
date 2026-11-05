# Penpot design-to-Compose handoff

The local Penpot file is the visual source of truth. This directory contains the
versioned handoff used by an agent to propose Compose changes:

- `design-tokens.json`: DTCG tokens matching `CrackMyDroidTheme`.
- `EXPORT_MANIFEST.json`: exported Home frames/assets, their SHA-256 and Compose targets.
- `screens/*.svg`: assets exported by the active Penpot page.

## One-time local setup

1. Copy `scripts/penpot/.env.penpot.example` to `scripts/penpot/.env.penpot`.
2. Open `https://design.penpot.app` and create/import `CrackMyDroid`.
3. Run `./scripts/penpot/start_penpot_mcp.sh` in a separate terminal.
4. In Penpot, use **Plugins → Load from URL** with
   `http://localhost:4400/manifest.json`, open the plugin and click **Connect**.
5. Configure the MCP client with `http://localhost:4401/mcp`.

This workflow uses Penpot hosted for the UI/file and a local MCP bridge for the
plugin and tool endpoint. It does not start Docker or require a local backend.

## Checks and export

`./scripts/penpot/check_penpot_mcp.sh` checks the UI, backend, MCP tools and the
connection to the currently active Penpot page.

Run the exporter after selecting the Home page. It intentionally requires every
shape id and output name, avoiding accidental bulk exports:

```bash
./scripts/penpot/export_svg_batch.sh \
  --shape <home-frame-id>:home-default \
  --shape <app-icon-id>:app-icon
```

The command uses MCP `export_shape`, stores SVGs under `screens/`, and rewrites
`EXPORT_MANIFEST.json`. It never changes the Penpot file.

Penpot dimension tokens use `px`; map them one-to-one to Compose `dp` at the
Android baseline density, rather than copying pixel literals into composables.

## Agent policy

Agents may use the active page only for `high_level_overview`, `penpot_api_info`
and `export_shape`. They must propose a Compose patch for review instead of
modifying Penpot. Map tokens to `MaterialTheme`, preserve `LocalStrings`, and
keep `HomeContent` states/previews intact.
