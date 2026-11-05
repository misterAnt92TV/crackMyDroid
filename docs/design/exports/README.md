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

The bridge owns three local listeners: plugin manifest on `4400`, MCP HTTP on
`4401`, and plugin WebSocket on `4402`. Start it only with
`scripts/penpot/start_penpot_mcp.sh`; the launcher reuses a complete existing
instance and refuses partial or unexpected port ownership. Keep the hosted
`CrackMyDroid` file and the connected plugin window open during imports and
exports. If direct diagnostics resolve `localhost` incorrectly on an IPv6-only
listener, use `http://[::1]:4401/mcp` for the diagnostic client only.

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

For image imports, pass an absolute path readable by the local bridge. A
`No Penpot plugin instances are currently connected` response means the plugin
must be reloaded from `http://localhost:4400/manifest.json` and connected again;
it is not a problem with the image path. TLS errors against `design.penpot.app`
require the approved `PENPOT_CA_BUNDLE`; certificate verification must not be
disabled.

Penpot dimension tokens use `px`; map them one-to-one to Compose `dp` at the
Android baseline density, rather than copying pixel literals into composables.

## Agent policy

Agents may use the active page only for `high_level_overview`, `penpot_api_info`
and `export_shape`. They must propose a Compose patch for review instead of
modifying Penpot. Map tokens to `MaterialTheme`, preserve `LocalStrings`, and
keep `HomeContent` states/previews intact.
