# envases-retornables

Objective: track returnable containers (envases): empty stock per product, containers returned on each sale, and a per-client, per-product balance with manual initial load.
Branch: `feat/envases-retornables` in both repos (backend worktree `C:\Users\marco\sodapp-envases`, from `origin/main` 2bc2cf8; app from `origin/main` 476ab76). Delivery: single-pr per repo (forecast ~1400 lines; user chose "Un PR por repo", API first). Runners: backend `php tests/isolation_test.php` (XAMPP PHP + MySQL `sodapp_test`); app `gradlew testDebugUnitTest` with `JAVA_HOME` = Android Studio `jbr`.

## Specs

User request (L1): "se necesita que al momento de registrar una venta se permita aclarar cuántos envases devuelve el cliente, para si lograr un balance en la cantidad de envases regristrados".
User decisions: balance "Por cliente y producto"; stock model "Vamos con b" (`pr_stk` = full units in warehouse, as today; new counter for empties); initial client balance "Carga manual".

- S1 Schema (migration `tablasDB/migrations/005_envases_retornables.sql`, number 004 is reserved for dias-entrega T7; independent of it; style of 003: header, before report, idempotent DDL, after report):
  - `sap_pr00`: `pr_ret TINYINT(1) NOT NULL DEFAULT 0` (returnable), `pr_vac INT NOT NULL DEFAULT 0` (empty containers in warehouse).
  - `sap_vt00`: `vt_dev INT NULL DEFAULT NULL` (containers returned in that sale line). NULL = line has no container effect (sale before this feature, or non-returnable product).
  - New `sap_ce00` (client container balance): `ce_cli INT NOT NULL`, `ce_pro INT NOT NULL` (= `pr_ide`), `ce_can INT NOT NULL DEFAULT 0`, `ce_emp INT NOT NULL`, PK `(ce_cli, ce_pro)`, FKs to `sap_cl00.cl_ide` and `sap_pr00.pr_ide` (InnoDB, utf8mb4_general_ci). Existing data is not modified.
- S2 Sale POST (`api/ventas.php`): each item in `productos` accepts optional `"devueltos"` (int >= 0, default 0). In the same transaction as today:
  - returnable product: insert `vt_dev = devueltos`; `pr_vac = pr_vac + devueltos`; client balance `ce_can += cantidad - devueltos` (upsert, atomic SQL, no read-modify-write).
  - non-returnable product: `vt_dev = NULL`; `devueltos > 0` -> rollback + 400 `{"success":false,"error":"El producto <nombre> no es retornable"}`; `devueltos` negative or not an integer -> 400.
  - Existing behavior unchanged otherwise (stock decrement, debt, 201 response). Old app versions (no `devueltos`) keep working, treated as 0.
  - Balance may go negative (assumption, same as stock: unreliable initial data must not block deliveries).
- S3 Sale DELETE (`api/ventas.php`): when the deleted row has `vt_dev IS NOT NULL`, in one transaction revert `pr_vac -= vt_dev` and `ce_can -= (vt_can - vt_dev)`. Rows with `vt_dev NULL` behave as today. Stock and debt reversal stay out of scope (existing follow-up).
- S4 Products (`api/productos.php`): GET returns `pr_ret` and `pr_vac`. POST accepts optional `retornable` (bool, default false) and `vacios` (int >= 0, default 0). PUT accepts optional query params `retornable` (0/1) and `vacios` (absolute int >= 0); when omitted those columns are not touched, so an old app PUT (`nombre, precio, cantidad`) behaves exactly as today.
- S5 Refill (`api/envases.php` POST `{"accion":"llenar","producto":"<nombre>","cantidad":N}`, N int > 0): atomic `pr_vac -= N, pr_stk = COALESCE(pr_stk,0) + N` only if owned, not deleted, returnable and `pr_vac >= N`. Not found/not owned -> 404; not returnable -> 400; not enough empties -> 409 `{"success":false,"error":"No hay suficientes envases vacíos"}`. Success 200 with the updated product.
- S6 Client balance (`api/envases.php`):
  - GET `?clienteId=` -> `{"success":true,"envases":[{"pr_ide","pr_nom","ce_can"}]}` for every owned, non-deleted returnable product (0 when no row). Client not owned/deleted -> 404.
  - PUT body `{"clienteId","productoId","cantidad"}` sets the absolute balance (manual initial load / correction), `cantidad` int >= 0 else 400; product must be owned and returnable (404 / 400); client owned (404). Upsert.
- S7 App, products: product model maps `pr_ret`, `pr_vac`. Create and edit screens get a "Retornable" toggle and an "Envases vacíos" quantity (shown when returnable). Stock screen shows empties for returnable products and a "Llenar" action (quantity) that calls S5 and shows the 409 message.
- S8 App, sale form (`Ventas.kt AddVentaForm`): for each selected returnable product, an "Envases devueltos" stepper (default 0, >= 0) sent as `devueltos` in `ProductoVenta`. Non-returnable products show nothing and send 0.
- S9 App, client detail (`DeudaScreen.kt`): an "Envases" section listing each returnable product with the client's balance (S6 GET), with an edit action to set the balance (S6 PUT). Refreshes after a sale is registered or deleted.
- S10 Docs: user manual (`docs/manual-de-uso`) and any API docs mention containers returned, refill and client balance, if those docs exist.

## Tasks

- [x] T1 S1-S6 backend: migration 005 (applied to `sodapp_test`), `ventas.php`, `productos.php`, new `envases.php`, tests in `tests/isolation_test.php`. Route: delegated writer (parallel with T2; disjoint repo). RED 171/68 -> GREEN 239/0; parent spot check 239/0; helpers moved to `api/lib.php`. Backend commit `8c00a6f`. Risk: item 1 (migration). Independent verifier: S1-S6 pass, no blockers; differential probe of old-app requests byte-identical to baseline; DB checksums restored. RDD assess high, review due; consent granted; lenses risk + reliability -> approved and acknowledged (lineage review-04e80fe6581fd81e). Informational findings only: 500 bodies echo mysqli errors (pre-existing pattern); soft-deleted product asymmetry; envases PUT rejects negative balances (per S6); llenar 409 also covers race with delete/unflag; migration-before-deploy dependency.
- [x] T2 S7-S10 app: models, ApiServices, repositories, view models, screens, unit tests, docs. Route: delegated writer (parallel with T1; codes against the S2-S6 contract). Writer: RED (compile) -> GREEN 85 tests, assembleDebug OK. Response shapes checked against backend `envases.php` (envases / producto / envase keys). Hilt binding moved into `di/RepositoryModule.kt`; parent spot check 85/0 + assembleDebug OK. App commit `7755df3`. RDD assess: medium, `under_budget`, no review due. Manual (`docs/manual-de-uso/MANUAL.md`) updated but the folder is untracked, not committed; PDF not regenerated.
- [ ] T3 Seam check: app staging APK against local API, manual flow (sale with returns, balance, refill, delete). Route: inline.
- [ ] T4 Definition of done: PRs to `main` in both repos (API first), run migration 005 in production, deploy API, release APK, production smoke test. ORDER: migration 005 BEFORE uploading the API (new ventas POST / productos POST use `sap_ce00`, `vt_dev`, `pr_ret`, `pr_vac` and return 500 without them); check step 1 reports InnoDB for sap_cl00 and sap_pr00 in prod.

## Log

- L1 2026-10-28 user: "Agreguemos nuevas funcionalidades al control de stock a través de las ventas, se necesita que al momento de registrar una venta se permita aclarar cuántos envases devuelve el cliente, para si lograr un balance en la cantidad de envases regristrados"
- L2 2026-10-28 user chose balance "Por cliente y producto (Recommended)".
- L3 2026-10-28 user: "Analiza la posibilidad de que pr_stk represente la cantidad de envases que posee indiferentemente de si está lleno o vacío y agregar otro atributo que indique si el producto está lleno o vacio". Analysis: a per-product full/empty flag cannot represent both states at once; option A (pr_stk = total) changes the meaning of a column live clients read; option B keeps pr_stk = full and adds pr_vac. User: "Vamos con b".
- L4 2026-10-28 user chose initial balance "Carga manual (Recommended)". Stored balance (`sap_ce00`) instead of computing from sales, because manual load and historical sales would double count.
- L5 2026-10-28 exploration: sales reference products by name; `ventas.php` DELETE reverts neither stock nor debt; app delete flow subtracts debt client-side; `productos.php` PUT is the only stock path (absolute value, `updated_at IS NULL` guard bug, out of scope); app sale form blocks when stock < quantity locally (contradicts backend, out of scope).
- L6 2026-10-28 user chose delivery "Un PR por repo". T1 and T2 launched as parallel delegated writers.
- L7 2026-10-28 verifier advisories (not blockers): selling a soft-deleted returnable product updates the client balance but not `pr_vac` (pre-existing: sale validation ignores `deleted_at`); `prepare_or_fail` exits inside transactions without explicit rollback (implicit rollback on close, pre-existing pattern); `devueltos > cantidad` accepted (balance may go negative, per assumption).
