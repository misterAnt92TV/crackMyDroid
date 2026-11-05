---
name: penpot-mcp
description: "Use CrackMyDroid designs in Penpot hosted through the local MCP plugin and bridge for diagnostics, design work, and exports; never use Docker."
---

# Penpot MCP

Use this skill for any Penpot task in CrackMyDroid: inspecting a hosted file, exporting/importing assets, creating or revising Penpot designs, or using the Penpot MCP bridge.

## Hosted UI and local bridge

- Use the hosted Penpot UI and the local bridge below as defaults:
  - Penpot UI: `https://design.penpot.app`
  - MCP plugin manifest: `http://localhost:4400/manifest.json`
  - MCP endpoint: `http://localhost:4401/mcp`
- Do not require or check a local Penpot backend. `PENPOT_BACKEND_URL` remains empty for this workflow.
- Never start, stop, inspect, or modify Docker/Compose services for this workflow.
- Do not print or commit values from `scripts/penpot/.env.penpot`, MCP keys, passwords, tokens, or CA paths. Read only the endpoint values needed for diagnosis.

## Preflight and connection

1. Read `scripts/penpot/.env.penpot` if it exists, without echoing secrets. Use the hosted UI and local plugin/MCP endpoints above; reject a plugin or MCP endpoint that is not on `localhost` or `127.0.0.1`.
2. Check the hosted UI, plugin manifest, and MCP endpoint with short timeouts. If a corporate TLS proxy is in use, require an approved `PENPOT_CA_BUNDLE`; never use `--insecure`.
3. If the MCP bridge is down, point the user to the existing repository launcher `scripts/penpot/start_penpot_mcp.sh`; do not duplicate it or replace it with Docker commands. The launcher may be run by the user in a separate terminal.
4. Ask the user to open the hosted `CrackMyDroid` file, load the plugin from the local manifest, and connect it to the MCP endpoint. Do not mutate Penpot until the active file/page is confirmed through MCP.
5. Before using Penpot tools, read `high_level_overview` once per session. Then use only the Penpot MCP tools needed for the requested operation.

## Operations

- For read-only requests, use `high_level_overview`, `penpot_api_info`, and read-only `execute_code`/`export_shape` operations. Do not edit Penpot.
- For requested design changes, confirm the active file and page, prefer native editable Penpot shapes/components, and preserve semantic names and existing layout systems.
- For exports, use absolute local output paths and the repository scripts under `scripts/penpot/` where applicable. Do not overwrite unrelated files; inspect targets first.
- Keep Penpot mutations scoped to the user’s requested file/page. Save a Penpot version when the user asks for a durable design revision.
- If any local endpoint or plugin connection fails, stop and report the exact failed endpoint, the observed error, and the single corrective action required. Do not continue with partial writes unless the user explicitly asks for an offline artifact-only workflow.

## Repository references

- `scripts/penpot/README.md`: local bridge setup and checks.
- `scripts/penpot/check_penpot_mcp.sh`: endpoint/session preflight.
- `scripts/penpot/start_penpot_mcp.sh`: existing local MCP bridge launcher.
- `scripts/penpot/export_svg_batch.sh`: explicit shape export workflow.
- `docs/design/exports/README.md`: repository export policy and manifest conventions.
