# Browser Testing Report — GenApp Modernization

- **Date (UTC):** 2026-11-20
- **Branch:** `devin/1795170391-genapp-modernization` (work in progress, pre-PR)
- **Environment:** Local — Spring Boot backend (H2, seeded Db2 data) on `:8080`, Vite dev server on `:5173`
- **Framework:** Playwright 1.48.2 (Chromium), suite `modernized/frontend/e2e/genapp.spec.ts`
- **Machine-readable results:** `modernized/frontend/test-results/results.json`

## Summary

| Total | Passed | Failed | Skipped | Initial score |
| --- | --- | --- | --- | --- |
| 10 | 10 | 0 | 0 | **100%** |

Backend unit/integration tests (JUnit 5 + MockMvc, `mvn verify`): **20/20 passed**.
Frontend unit tests (Vitest + Testing Library): **4/4 passed**. Lint, typecheck and production build: clean.

## Test cases

| # | Scenario | Type | Result |
| --- | --- | --- | --- |
| 1 | Dashboard loads and shows backend health (UP) | positive | PASS |
| 2 | Customer inquire returns seeded customer (Andrew Pandy, PI101OO) | positive | PASS |
| 3 | Customer inquire with unknown number shows `[RC 01]` | negative | PASS |
| 4 | Customer add allocates identity number (1000001+) and round-trips (postcode uppercased ZZ99YY) | positive | PASS |
| 5 | Customer field length validation returns `[RC 98]` | negative | PASS |
| 6 | Motor policy inquire returns seeded policy (FORD KA, customer 2 / policy 1) | positive | PASS |
| 7 | Policy inquire with wrong type/keys shows `[RC 01]` | negative | PASS |
| 8 | Motor policy add → update → delete round-trip | positive | PASS |
| 9 | Commercial search by postcode BB104BX returns BURNLEY FOOTBALL CLUB row | positive | PASS |
| 10 | House policy type mismatch on motor row shows `[RC 01]` | negative | PASS |

## Evidence

Screenshots captured during the run (full page, light/dark glass UI):

- `modernized/frontend/test-results/artifacts/01-dashboard.png` — dashboard with backend health
- `modernized/frontend/test-results/artifacts/02-customer-inquire.png` — seeded customer detail
- `modernized/frontend/test-results/artifacts/03-customer-add.png` — customer add result
- `modernized/frontend/test-results/artifacts/04-policy-inquire.png` — motor policy detail
- `modernized/frontend/test-results/artifacts/05-policy-add.png` — policy add form/result
- `modernized/frontend/test-results/artifacts/06-commercial-search.png` — commercial postcode search

Playwright HTML/JSON report and traces: `modernized/frontend/test-results/` (traces retained per failure policy; none failed in the final run).

## Figma design comparison

Reference: Figma file `zQwYQprRnZaRDvDk77lWF3` ("Boundary" design system — the supplied design reference contains game screens, not GenApp screens; its visual language was applied to GenApp).

| Design token | Figma reference | Implemented | Match |
| --- | --- | --- | --- |
| Background | Near-black dark teal gradient | `#060F11` base with subtle radial glow | Yes (flattened, no photo backdrop) |
| Accent | Lime `#D7FF67` | Lime `#D7FF67` for brand, active tabs, primary buttons, health dot | Yes |
| Panels | Translucent dark, 1px subtle border, 16–24px radius | Glass panels: translucent dark, 1px border, 16px radius | Yes |
| Typography | Inter; uppercase micro-labels; bold white headings | Inter; uppercase micro-labels; bold white headings | Yes |
| Buttons | Pill-shaped, low-contrast dark / lime primary | Pill buttons, lime primary, ghost secondary | Yes |
| Layout | Left-aligned text blocks, generous padding, thin dividers | Same grid, KV tables with dividers | Yes |

Deviations: photographic stadium backdrop replaced with a static gradient (no asset pipeline for the React app); GenApp content is form/table-driven rather than marketing/game HUD, so composition follows the design language rather than the frame layouts.

## Known limitations

- Backend runs on in-memory H2 seeded from `data.sql` (Db2 seed parity); production persistence is out of scope for this modernization stage.
- Post-deployment functional tests against Cloud Run URLs are defined in `modernized/scripts/functional-test.mjs` and will run in the CD workflow after merge.
