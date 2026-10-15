# Feature: sale-decrements-stock

## Objective
Registering a sale must decrement product stock on the server, atomically, and reject the sale when stock is insufficient.

## Problem
`api/ventas.php` POST inserts sales and updates client debt but never touches `sap_pr00.pr_stk`. Stock only changes by manual edits, so inventory drifts with every sale. The only stock check is client-side against a cached product list.

## Why
Every other inventory improvement (history, alerts, reports) depends on stock reflecting reality.

## Scope
- Backend (`C:\Users\marco\sodapp\api\ventas.php`, POST only): inside the existing transaction, per product line run a conditional atomic update
  `UPDATE sap_pr00 SET pr_stk = pr_stk - ? WHERE pr_nom = ? AND pr_emp = ? AND deleted_at IS NULL AND pr_stk >= ?`.
  Zero affected rows → rollback and HTTP 409 `Stock insuficiente: <nombre>`.
- App: after a successful sale, invalidate the cached product list so Inventario/Ventas show the new stock; surface the 409 message to the user.

## Out of scope (follow-ups)
- DELETE of a sale restores neither stock nor client debt today; keep both consistent in a later task.
- Stock movement history, `updated_at IS NULL` typo in `productos.php` PUT, server-side validation of stock values.

## Constraints
- Atomic conditional update (no read-then-write) to avoid race conditions between devices.
- Products with NULL `pr_stk` will fail the `>=` check and be rejected as insufficient stock.
- Branch `feat/sale-decrements-stock` from `main` in both repos; test in `stage`, then merge to `main`.

## Tasks
- [x] T1 Backend: decrement stock atomically in ventas.php POST, 409 on insufficient stock. Route: delegated (2 repos, 2+ non-trivial files). Commit `b31b6cd` (sodapp, `feat/sale-decrements-stock`).
- [x] T2 App: invalidate product cache after successful sale; show 409 error message. Route: delegated (same writer). Commit `248ef7b` (SodAppComposse, `feat/sale-decrements-stock`).

## Acceptance criteria
- A sale of N units lowers `pr_stk` by N for that product and owner.
- A sale exceeding stock returns 409, inserts no sale rows and leaves debt and stock unchanged.
- Inventario shows updated stock after a sale without restarting the app.

## Checks
- `php -l api/ventas.php`
- Backend test if runnable (`tests/`); otherwise manual check against stage.
- `./gradlew :app:compileDebugKotlin` (or the unit test task if relevant tests exist).

## Progress
- T1 done: `api/ventas.php` POST prepares the conditional `UPDATE sap_pr00 ... pr_stk >= ?` once and runs it per line after the sale insert; 0 affected rows → rollback + 409 `Stock insuficiente: <nombre>`. `deleted_at` verified in `tablasDB/sap_pr00.sql`.
- T2 done: `VentasViewModel.postVenta` calls `productoRepository.clearCache()` on success (mirrors the client cache drop). `VentaRepositoryImpl` maps a 409 body `{"error":"Stock insuficiente: X"}` to `R.string.stock_insuficiente_format` with X; `Ventas.kt` already toasts `VentasUiState.Error`, so it was not changed.

## Verification evidence
- `php -l api/ventas.php`: No syntax errors detected.
- Backend test: exception. `tests/isolation_test.php` needs a live MySQL `sodapp_test` (and mysqli, not enabled in the CLI php.ini); no local MySQL was running (connection refused). No RED/GREEN claimed; manual check against stage still pending.
- App RED: new tests `an accepted sale drops the cached product list so stock is reloaded` and `a 409 insufficient stock response surfaces the product name` failed before the change.
- App GREEN: `./gradlew :app:compileDebugKotlin` OK; `./gradlew :app:testDebugUnitTest` OK, 53 test cases, 0 failures (JAVA_HOME = Android Studio JBR).

## Next step
Manual check on stage: sale lowers `pr_stk`; oversell returns 409 with no sale rows and unchanged debt/stock; Inventario shows new stock. Then merge to `main`. Follow-up: products with NULL `pr_stk` are now rejected; sale DELETE restores neither stock nor debt.
