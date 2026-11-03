# Penpot SVG exports

Configurazione centralizzata:

- `scripts/penpot/.env.penpot`
  - `PENPOT_UI_URL=http://localhost:9001`
  - `PENPOT_MCP_URL=http://localhost:6060`
  - `PENPOT_MCP_KEY=` (opzionale)

## Verifica finale stack locale

```bash
./scripts/penpot/check_penpot_mcp.sh
```

## Export batch (Screen + Component)

```bash
./scripts/penpot/export_svg_batch.sh
```

Output:

- `docs/design/exports/screens/*.svg`
- `docs/design/exports/components/*.svg`
- `docs/design/exports/EXPORT_INDEX.md`
