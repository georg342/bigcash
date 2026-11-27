# Changelog

All notable changes to BigCash are documented here. The format follows Keep a Changelog and the project
uses Semantic Versioning.

## [1.0.0] - 2026-11-27

### Added

- Notification listener with configurable monitored packages for C6 Bank, Nubank and Mercado Pago.
- Raw notification capture (title, text, big text, package, timestamp, hash) that never discards content.
- Cascading parsing engine with regex, mask and keyword rules ordered by priority and scoped per bank.
- Calibration panel: raw history, rule CRUD with reorder, playground with per-rule traces, failure list and
  history reprocessing.
- Dashboard with combinable filters, full-text search, totals, tags and manual correction of transactions.
- Monthly summary built from numbers and ranked lists.
- Local notifications confirming recorded transactions and failed parses, with editable templates.
- CSV/JSON export, JSON import and share-sheet delivery.
- Liquid Glass design system with drifting light blobs, glass surfaces and dedicated light/dark palettes.
- Unit tests for the parsing engine using real notification wording from the three supported banks.

### Notes

- Room 3 (`androidx.room3`) requires `@ColumnTypeConverters` and an explicit `SQLiteDriver`; the database
  uses `BundledSQLiteDriver` with destructive fallback migrations for version 1.
- Room 3 annotations live in `androidx.room3:room3-common`, which the room convention plugin adds
  explicitly.
