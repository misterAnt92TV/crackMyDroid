---
name: penpot-mcp
description: "Use CrackMyDroid designs in Penpot hosted through the local MCP plugin and bridge for diagnostics, design work, and exports; never use Docker."
---

# Penpot MCP

Use this skill for any Penpot task in CrackMyDroid: inspecting a hosted file, exporting/importing
assets, creating or revising Penpot designs, or using the Penpot MCP bridge.

## Scope and precedence

This skill owns Penpot connection, design inspection, design mutation, and exports. For source-code
or Compose implementation changes, also apply `crackmydroid-development`; that skill owns Kotlin,
architecture, previews, and tests. For Penpot design/export decisions, this skill is more specific
and takes precedence over generic UI implementation guidance. Do not use Penpot instructions to
authorize unrelated code or repository changes.

## Hosted UI and local bridge

- Use the hosted Penpot UI and the local bridge below as defaults:
    - Penpot UI: `https://design.penpot.app`
    - MCP plugin manifest: `http://localhost:4400/manifest.json`
    - MCP endpoint: `http://localhost:4401/mcp`
- Do not require or check a local Penpot backend. `PENPOT_BACKEND_URL` remains empty for this
  workflow.
- Never start, stop, inspect, or modify Docker/Compose services for this workflow.
- Do not print or commit values from `scripts/penpot/.env.penpot`, MCP keys, passwords, tokens, or
  CA paths. Read only the endpoint values needed for diagnosis.

The supported topology is intentionally single-path: Penpot UI and files are
hosted, while the plugin and MCP bridge run locally. Do not substitute a local
Penpot backend, Docker, or a hosted MCP endpoint.

## Preflight and connection

1. Read `scripts/penpot/.env.penpot` if it exists, without echoing secrets. Reject plugin/MCP URLs
   that are not `localhost` or `127.0.0.1`.
2. Inspect ports `4400` (plugin), `4401` (MCP HTTP), and `4402` (plugin WebSocket) before starting
   anything:
    - all three owned by the Penpot bridge: reuse the existing instance;
    - only some occupied: stop and report the PIDs, without killing processes;
    - none occupied: use `scripts/penpot/start_penpot_mcp.sh`.
3. The launcher uses an isolated, pinned pnpm runtime. If Corepack reports a missing `pnpm.cjs`,
   rerun the launcher so it repairs its cache; do not run `pnpm recursive run` manually.
4. Check the hosted UI, manifest, MCP endpoint, and WebSocket with short timeouts. If a corporate
   TLS proxy is in use, require an approved `PENPOT_CA_BUNDLE`; never use `--insecure`.
5. Open the hosted `CrackMyDroid` file, load `http://localhost:4400/manifest.json`, open the plugin,
   and click **Connect to MCP server**. Keep the plugin panel open. A
   `No Penpot plugin instances are currently connected` error means the plugin must be
   reloaded/reconnected; it is not an image or file-path error.
6. Do not mutate Penpot until MCP confirms the active file/page. Before using tools, read
   `high_level_overview` once per session, then use only the required Penpot tools.

## Operations

- For read-only requests, use `high_level_overview`, `penpot_api_info`, and read-only
  `execute_code`/`export_shape` operations. Do not edit Penpot.
- For requested design changes, confirm the active file and page, prefer native editable Penpot
  shapes/components, and preserve semantic names and existing layout systems.
- For exports, use absolute local output paths and the repository scripts under `scripts/penpot/`
  where applicable. Do not overwrite unrelated files; inspect targets first.
- For `import_image`, use an absolute path readable by the local bridge and treat textual
  `Tool execution failed` responses as failures even when the MCP envelope lacks `isError`.
- Keep Penpot mutations scoped to the user’s requested file/page. Save a Penpot version when the
  user asks for a durable design revision.
- If any local endpoint or plugin connection fails, stop and report the exact failed endpoint, the
  observed error, and the single corrective action required. On systems where the bridge listens
  only on IPv6, use `http://[::1]:4401/mcp` for direct diagnostics while keeping the documented
  manifest URL on `localhost`. Do not continue with partial writes unless the user explicitly asks
  for an offline artifact-only workflow.

## Known failure modes

- `EADDRINUSE` on `4400`: another bridge is already running; reuse it rather than launching a second
  instance.
- `ERR_PNPM_RECURSIVE_RUN_FIRST_FAIL`: usually the duplicate-start cascade or a broken Corepack
  cache; inspect the first plugin error, not the final SIGKILL.
- `Cannot find ... pnpm.cjs`: the isolated Corepack cache is incomplete; rerun the repository
  launcher to repair it.
- `No Penpot plugin instances are currently connected`: open the hosted file, reload the local
  manifest, reconnect the plugin, and keep its panel open.
- TLS certificate failure for `design.penpot.app`: configure the approved `PENPOT_CA_BUNDLE`; never
  disable verification.

## Repository references

- `scripts/penpot/README.md`: local bridge setup and checks.
- `scripts/penpot/check_penpot_mcp.sh`: endpoint/session preflight.
- `scripts/penpot/start_penpot_mcp.sh`: existing local MCP bridge launcher.
- `scripts/penpot/export_svg_batch.sh`: explicit shape export workflow.
- `docs/design/exports/README.md`: repository export policy and manifest conventions.
