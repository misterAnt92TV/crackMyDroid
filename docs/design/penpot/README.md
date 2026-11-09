# CrackMyDroid Penpot source

For an upload-ready bundle of screen and component SVGs, see
[`export/README.md`](export/README.md). It can be imported without connecting to
the hosted Penpot file.

`CrackMyDroid.penpot` is the importable backup of the local Penpot file. Commit it
after each accepted design revision together with the generated files in
`../exports/`.

The file must contain these pages:

- `Design system`: light/dark tokens and components `SectionHeader`, `AppCard`,
  `AppAccordion`, `PackageIcon`, and `OperationItem`.
- `Home`: the normal Home screen frame.
- `Home – empty/error`: empty history and failed-operation variants.
- `Assets`: icon and illustration export sources.

The account/session used to create this first file is intentionally not stored in
the repository. Create or open the file in [Penpot hosted](https://design.penpot.app),
export it from the dashboard, save it here as `CrackMyDroid.penpot`, and import it
again when restoring a workstation.
