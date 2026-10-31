# historial-stock

Objective: record every stock change per product and show it in the app. Branch `feat/historial-stock` (stacked on `fix/stock-ventas-followups`) (app + backend `c:\Users\marco\sodapp`).
Delivery: ask-on-risk. Runners: backend `php tests/isolation_test.php` (XAMPP PHP, `ISOLATION_TEST_PORT=8799`), app `./gradlew :app:testDebugUnitTest`.

## Specs

User request (L1): "Agregar un historial de movimientos de stock." Scope approved by the user ("si me sirve") on 2026-10-30 as proposed:

- S1 Schema: new table `sap_ms00`, one row per stock change: product, date, movement type, quantity (+/−), resulting stock and the related sale when there is one. Migration `tablasDB/migrations/006_historial_stock.sql` (004 is reserved for dias-entrega T7), plain schema-qualified SQL like 005 (production phpMyAdmin fails on PREPARE/information_schema). Existing data is not modified.
- S2 Recorded movements, always in the same transaction as the stock change:
  - venta (−) — `api/ventas.php` POST, one row per sale line, linked to the sale id.
  - venta eliminada (+) — `api/ventas.php` DELETE, linked to the deleted sale id.
  - llenado de envases (+) — `api/envases.php` POST `llenar`.
  - ajuste manual al editar el producto — `api/productos.php` PUT, quantity = new stock − previous stock; no row when the stock does not change.
  - alta del producto — `api/productos.php` POST, quantity = initial stock.
- S3 Read endpoint: the app lists a product's movements, newest first. Product must be owned and not deleted (404 otherwise).
- S4 App: from Inventario, a "Historial" button per product opens the list of its movements, newest first.
- S5 Out of scope: movements before this feature ("el historial arranca vacío").
- S6 Docs: user manual mentions the history.

## Tasks

| ID | Specs | Route | Status | Commit |
|---|---|---|---|---|
| T1 | S1, S2, S3 | inline | done | backend `1743239` |
| T2 | S4, S6 | inline | done | app (this commit) |

## Log

- L1 (2026-10-30): "Agregar un historial de movimientos de stock." Proposed scope: table `sap_ms00`; movements venta, venta eliminada, llenado, ajuste manual (difference), alta (initial stock); "Historial" button per product in Inventario; history starts empty. User: "si me sirve".
- L2: route inline (no writer trigger: one behavior per repo, sequential dependency T2 on T1). Risk: item 1 (new migration in production).
- L3 (2026-10-30): user asked "trabajaras esa implementacion en otra rama?" — moved to `feat/historial-stock`, stacked on `fix/stock-ventas-followups` (history records the stock restored on sale delete).
- L4 T1: migration 006 applied to local `sodapp_test`; `record_stock_movement()` in `api/lib.php` inserts from `sap_pr00` after the change, in the same transaction (POST/PUT productos, envases llenar now run in transactions). `api/movimientos.php` GET, latest 200, newest first. RED 9 failing → GREEN 267 passed, 0 failed; cleanup leaves `sap_ms00` empty.
- L5 T2: history opens in a dialog from the Inventario card (no navigation change); separate `MovimientosUiState` so a failure never hides the product list. RED (compile) → GREEN 91 unit tests, assembleStaging and lintStaging pass. Manual updated (untracked `docs/manual-de-uso`). Not checked on a device (no phone connected; emulator not viable on 5.8 GB RAM).
- Next: device check, merge (`fix/stock-ventas-followups` first), run 006 in production phpMyAdmin after a backup, FTP `lib.php`, `movimientos.php`, `ventas.php`, `productos.php`, `envases.php`.
