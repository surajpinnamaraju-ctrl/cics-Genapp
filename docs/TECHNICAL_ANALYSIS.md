# GenApp (cics-Genapp) — Technical Analysis

Source repository: https://github.com/surajpinnamaraju-ctrl/cics-Genapp
Upstream: IBM `cicsdev/cics-genapp` — "General insurance application (GenApp) for IBM CICS TS".
Analyzed commit: `f6f3f4b` (`main`). Generated with the Devin Wiki
(https://app.devin.ai/wiki/surajpinnamaraju-ctrl/cics-Genapp) plus direct
source inspection.

## 1. Repository layout

| Path | Contents |
| --- | --- |
| `base/src/*.cbl` | 31 COBOL programs (presentation, business logic, Db2/VSAM data layer) |
| `base/src/*.cpy` | 13 copybooks — commarea, policy record layouts, SOA records |
| `base/src/ssmap.bms` | BMS map source for all five 3270 screens |
| `base/cntl/*.jcl` | 29 JCL jobs: Db2 DDL/bind/seed, CICS resource defs, compile, CSD, optional CICSPlex SM / WSIM / web-services jobs |
| `base/data/ksdscust.txt`, `ksdspoly.txt` | 9 customer + 10 policy records seeded into the VSAM files |
| `base/exec/*.rexx` | `cust1.rexx` / `mac1.rexx` — z/OSMF/USS install helpers |
| `base/wsim/*.txt` | IBM Workload Simulator model/script assets (`ssc*.txt`, `ssp*.txt`, `wsc*.txt`, …) |
| `base/event-bindings/`, `base/images/` | CICS event binding assets and topology images |
| `base/bin/install.sh` | GitHub-clone-to-USS installer |
| `*.md`, `Changes.md` | IBM documentation (Architecture, Installation, Building, Testing, Reference) |

## 2. What the application does

GenApp simulates a general-insurance back office on a single CICS region:
customers and four policy types — **motor (M), endowment (E), house (H),
commercial property (C)** — stored in Db2, mirrored into two VSAM KSDS
files (`KSDSCUST`, `KSDSPOLY`) inside a two-phase commit.

Five 3270 transactions (BMS maps) drive everything:

| Tran | Program (presentation) | Menu options | Business requests used |
| --- | --- | --- | --- |
| `SSC1` | `LGTESTC1` | 1 Inquire, 2 Add, 4 Update | `01ICUS`, `01ACUS`, `01UCUS` |
| `SSP1` | `LGTESTP1` | 1 Inquire, 2 Add, 3 Delete, 4 Update (motor) | `01IMOT`, `01AMOT`, `01DMOT`, `01UMOT` |
| `SSP2` | `LGTESTP2` | 1–4 same (endowment) | `01IEND`, `01AEND`, `01DEND`, `01UEND` |
| `SSP3` | `LGTESTP3` | 1–4 same (house) | `01IHOU`, `01AHOU`, `01DHOU`, `01UHOU` |
| `SSP4` | `LGTESTP4` | 1 Inquire, 2 Add, 3 Delete (commercial — no update) | `01ICOM`/`02ICOM`/`03ICOM`/`05ICOM`, `01ACOM`, `01DCOM` |

`LGSE` → `LGSETUP` initializes named counters (`GENACUSTNUM` etc.) and
rebuilds the `GENACNTL` temporary-storage queue. `LGCF`, `LGPF`, `LGST`
and `SSST` are support transactions for the optional coupling-facility /
dynamic-scripting demos; `LGASTAT1`/`LGWEBST5`/`LGSTSQ` feed the
Workload-Simulator/event-dashboard demos.

### Program layers (best-practice 3-tier COBOL)

```
3270 BMS map ──> LGTESTC1/LGTESTP1..4   (presentation: screens, PF keys)
                     │ EXEC CICS LINK, DFHCOMMAREA (lgcmarea.cpy, 32,500 B)
                     ▼
               LGICUS01/LGACUS01/LGUCUS01   (customer business logic)
               LGIPOL01/LGAPOL01/LGDPOL01/LGUPOL01 (policy business logic)
                     │ EXEC CICS LINK, same commarea
                     ▼
               LGICDB01/LGACDB01/LGUCDB01/LGACDB02   (customer Db2)
               LGIPDB01/LGAPDB01/LGDPDB01/LGUPDB01   (policy Db2)
               LG*VS01 (LGACVS01, LGICVS01, LGUCVS01, LGAPVS01, LGDPVS01,
                        LGIPVS01, LGUPVS01)           (VSAM KSDS mirror)
               LGSTSQ (writes error TSQ), LGSETUP, LGASTAT1, LGWEBST5
```

## 3. Data model (Db2, from `cntl/db2cre.jcl`)

| Table | Key columns | Notes |
| --- | --- | --- |
| `customer` | `customerNumber` IDENTITY(1000001+), name, DOB, address, phones, email | PK `customerNumber` |
| `customer_secure` | `customerNumber` FK→customer CASCADE, `customerPass`, `state_indicator`, `pass_changes` | Seeded with MD5 hashes; **never read by base programs** (belongs to security-extension scenarios) |
| `policy` | `policyNumber` IDENTITY(1000001+), `customerNumber` FK→customer CASCADE, issue/expiry dates, `policyType` X(1), `lastChanged` TIMESTAMP, brokerId, brokersReference, payment, commission | PK `policyNumber`; index on `customerNumber` |
| `endowment` | `policyNumber` FK→policy CASCADE, withProfits/equities/managedFund Y/N, fundName, term, sumAssured, lifeAssured, `paddingData` VARCHAR(32606) | PK = FK |
| `house` | `policyNumber` FK→policy CASCADE, propertyType, bedrooms, value, houseName/number, postcode | |
| `motor` | `policyNumber` FK→policy CASCADE, make, model, value, regNumber, colour, cc, yearOfManufacture DATE, premium, accidents | |
| `Commercial` | `policyNumber` FK→policy CASCADE, request/start/renewal dates, address, zipcode, lat/long, customer, propertyType, fire/crime/flood/weather peril+premium, status, rejectionReason | |
| `Claim` | `claimNumber` IDENTITY, `policyNumber` FK→policy CASCADE, claimDate, paid, value, cause, observations | **Seeded with no rows; no base transaction touches it** |

Seed data (identical in `db2cre.jcl` and `base/data/*.txt`): 10 customers
(1–10: Andrew Pandy, Scott Tracey, John Noakes, Louie Pug, Graham
Cuthbert, Brian Cant, Troy Tempest, Johnny Morris, Micky Murphy, Susan
Stranks) and 10 policies:

| Policy | Type | Customer | Type-table row |
| --- | --- | --- | --- |
| 1 | M | 2 | Ford KA, LL60LOO |
| 2 | M | 10 | Volkswagen Beetle, A567WWR |
| 3 | M | 5 | Dennis Engine, FIRE1 |
| 4 | E | 8 | LIONTMR, J. MORRIS |
| 5 | E | 3 | SHEPPA, Shep |
| 6 | H | 4 | HOUSE, 5 bed, £1.5M |
| 7 | C* | 6 | FARM house row also exists (Db2 `policy.policyType='C'`) |
| 8 | H | 9 | FLAT, 1 bed, £260k |
| 9 | C | 5 | STADIUM (Turf Moor) |
| 10 | C | 1 | OFFICES (Hursley Park) |

\* See §7 for the seed-data discrepancies between `db2cre.jcl`,
`ksdspoly.txt` and `Reference.md`.

## 4. Business rules extracted from the COBOL

### Return codes (`CA-RETURN-CODE`)

| RC | Meaning | Raised by |
| --- | --- | --- |
| `00` | success | everywhere |
| `01` | not found (SQLCODE 100 / VSAM not found) | inquire, update |
| `02` | policy update conflict — `lastChanged` timestamp mismatch | `LGUPDB01` only |
| `70` | add policy failed: customer does not exist (Db2 −530 FK violation) | `LGAPDB01`/`LGDPOL01` path |
| `90` | Db2/VSAM error | all data programs |
| `98` | commarea too short (malformed request) | all data programs |
| `99` | unrecognised request-id / policy type | all data programs |

### Customer flows

- **Inquire** (`01ICUS`): SELECT on `customer` by `customerNumber`;
  RC `01` when absent. Postcode is upper-cased by the presentation layer.
- **Add** (`01ACUS`): customer number from Db2 `IDENTITY` when the named
  counter server `GENACUSTNUM` is unavailable (`LGAC-NCS='NO'` path), or
  from the coupling-facility counter when configured. Then INSERT into
  `customer` **and** `customer_secure` (via `LGACDB02`, fixed MD5 value —
  a demo credential), plus VSAM write. All in one UOW (2PC).
- **Update** (`01UCUS`): UPDATE all nine descriptive columns WHERE
  `customerNumber` matches; RC `01` if absent. No optimistic lock on
  customers.
- There is **no customer delete** in the base app (the `LGUCUS01`
  program is update-only; `01UCUS` is the only customer update request).

### Policy flows

- **Inquire** (`01I*`): SELECT joining `policy` to the type table on
  `policyNumber` AND `customerNumber` — both are required for M/E/H.
  Commercial (`01ICOM`) supports four lookup modes chosen by which key
  fields are non-blank on the 3270 screen: customer+policy (`01ICOM`),
  policy only (`02ICOM`, scroll cursor), customer only (`03ICOM`),
  postcode only (`05ICOM`). The customer+policy form is the only one the
  modern UI must support for parity (the search variants are exercised
  by the web-service/WSIM scenarios).
- **Add** (`01A*`): INSERT into `policy` (policyNumber = IDENTITY,
  `lastChanged` = CURRENT TIMESTAMP) then INSERT into the type table;
  any failure after the policy insert triggers `EXEC CICS ABEND 'LGSQ'`
  to roll back the UOW. FK violation (−530) → RC `70` ("Customer does
  not exist"). `payment`, `brokerId`, `brokersReference` are set to 0 /
  blanks by the presentation layer — they are not on the 3270 maps.
- **Delete** (`01D*`): DELETE FROM `policy` WHERE customerNumber AND
  policyNumber match; the type-table rows disappear via `ON DELETE
  CASCADE`. SQLCODE 100 treated as success (idempotent). Then VSAM delete.
- **Update** (`01U*`, M/E/H only — `LGUPDB01` has no commercial branch):
  optimistic concurrency — cursor `SELECT … FOR UPDATE`-style fetch of
  the policy row, compare `CA-LASTCHANGED` with `lastChanged`; mismatch
  → RC `02` and the update is abandoned; match → UPDATE the type table,
  then UPDATE `policy` (new `CURRENT TIMESTAMP`), VSAM rewrite.
- `CA-LASTCHANGED` is `PIC X(26)` carrying the Db2 TIMESTAMP string.

### Cross-cutting behaviour

- Every program writes errors to TSQ `GENAERRS` via `LGSTSQ`
  (date, time, program name, customer/policy numbers, SQLCODE, and up to
  90 bytes of commarea) — the modern backend replaces this with
  structured logging.
- `LGSETUP` re-initialises the customer-number counter range and the
  `GENACNTL` TSQ from the current DB2 contents; in the modern app the
  equivalent is "ensure sequences are ahead of seeded IDs".
- `CA-NUM-POLICIES` exists in the commarea but is hard-wired to `00` by
  every business-logic program — there is no "list policies for
  customer" feature in the base app.

## 5. Copybooks and interfaces

| Copybook | Role |
| --- | --- |
| `lgcmarea.cpy` | The universal commarea: request-id X(6), return-code 9(2), customer-num 9(10), then a 32,482-byte union of customer / customer-security / policy requests (per-type layouts E/H/M/C/CLAIM) |
| `lgpolicy.cpy` | Db2 row layouts mirroring `db2cre.jcl` column lengths |
| `pollook.cpy` / `polloo2.cpy` | Alternate commarea views used by SOA/web-service programs |
| `soa*.cpy` (8 files) | Records for the web-services (WSDL) interfaces — out of scope for the 3270 parity UI |
| `ssmap.bms` | All five maps: `SSMAPC1` (customer), `SSMAPP1` (motor), `SSMAPP2` (endowment), `SSMAPP3` (house), `SSMAPP4` (commercial) |

## 6. Modernization mapping

### Feature inventory → modern features

| # | Legacy capability | Modern implementation |
| --- | --- | --- |
| F1 | SSC1 customer menu (inquire/add/update) | React page + `GET/POST/PUT /api/customers` |
| F2 | SSP1 motor policy menu (inquire/add/delete/update) | React page + `/api/policies/motor` |
| F3 | SSP2 endowment policy menu | React page + `/api/policies/endowment` |
| F4 | SSP3 house policy menu | React page + `/api/policies/house` |
| F5 | SSP4 commercial menu (inquire 4 modes / add / delete; no update) | React page + `/api/policies/commercial` |
| F6 | Db2 schema + referential integrity + IDENTITY keys | JPA entities, H2 (dev) / Postgres-ready, sequences starting at 1000001 |
| F7 | Seed data (10 customers, 10 policies) | `data.sql` seed loaded on startup |
| F8 | Return-code contract (00/01/02/70/90/98/99) | HTTP status + error body preserving the legacy codes |
| F9 | Optimistic locking on policy update (`lastChanged`) | `@Version`/timestamp compare → HTTP 409 |
| F10 | LGSETUP initialization | `POST /api/admin/setup` + startup sequence check |
| F11 | Error logging (GENAERRS TSQ) | SLF4J structured logs + `/api/health` |

### Out of scope (documented limitations)

- **VSAM two-phase commit** (`LG*VS01`, KSDSCUST/KSDSPOLY): z/OS-specific;
  the relational DB is the system of record in the modern app.
- **Named counter server / coupling facility / TSQs** (`GENACUSTNUM`,
  `GENACNTL`, `GENAERRS`, `GENASTRT/GENASTAT`): replaced by DB sequences
  and logs.
- **CICSPlex SM topology, workload simulator (WSIM), web-services
  (WSA*/GENA* pipeline) and event bindings**: CICS infrastructure demos,
  not business functions.
- **`Claim` table**: no base transaction reads or writes it.
- **`customer_secure` / LGACDB02**: password handling belongs to the
  security-extension scenarios; the base 3270 app has no login. Kept as
  a seeded table for schema parity only.
- **Commercial search cursors** (`02ICOM`/`03ICOM`/`05ICOM`): preserved
  as query endpoints (`GET /api/policies/commercial?policyNumber=`,
  `?customerNumber=`, `?postcode=`) but the primary UI flow uses the
  customer+policy lookup.

## 7. Risks and constraints

1. **Packed/zoned numerics and EBCDIC semantics** — all amounts are
   `PIC 9(n)` DISPLAY numerics; modern API uses JSON numbers/strings.
   Money has no decimal point anywhere (values are integral pounds);
   keep them integral.
2. **Fixed-width CHAR padding** — Db2 `CHAR(n)` columns pad with
   spaces; the 3270 maps trim them. The API must trim trailing spaces
   on output and pad-normalise on input where the COBOL does
   (`INSPECT … REPLACING ALL x'00' BY x'40'`, postcode upper-case).
3. **COMM_AREA length checks (RC 98)** — the modern equivalent is
   request validation (required fields, max lengths per `lgpolicy.cpy`).
4. **FK −530 → RC 70** — adding a policy for a non-existent customer
   must return the same business error, not a generic 500.
5. **Timestamp format** — `lastChanged` is a 26-char Db2 TIMESTAMP
   string; expose ISO-8601 and accept it back for update conflict
   detection.
7. **Seed discrepancies (verified against `db2cre.jcl`)** — three sources
   disagree:
   - `db2cre.jcl` seeds `policy` rows with `policyType` M/M/M/E/E/**C**/H/C/C/C
     for policies 1–10, i.e. policy 7 is type `C` although a `house` row
     (FARM) exists for it.
   - `base/data/ksdspoly.txt` marks the same record 7 as `H`.
   - `Reference.md`'s sample table lists policy 7 as House/customer 6
     and policy 10 as Commercial/customer 1 — consistent with the Db2
     `policyType` values except for policy 7.
   The modern seed follows **Db2** (`db2cre.jcl`) as the system of
   record: every type-table row is loaded exactly as seeded, and the
   `policy.policyType` column drives which menu/API serves the policy.
   Policy 7 is therefore reachable as a commercial policy (its house
   attributes remain stored, mirroring the Db2 state).
7. **No transactions across menus** — each 3270 interaction is its own
   UOW; the modern API keeps one UOW per HTTP call.

## 8. Target architecture

| Layer | Choice | Reason |
| --- | --- | --- |
| Backend | **Spring Boot 3.x (Java 17), Spring Web, Spring Data JPA, H2** | Required by the task; maps cleanly to CICS transactions (controllers ≈ programs, services ≈ business logic, repositories ≈ Db2), H2 gives zero-ops dev/test and the same SQL semantics |
| Frontend | **React 18 + TypeScript + Vite, React Router, plain CSS** | Required by the task; replaces BMS maps with accessible forms |
| Tests | JUnit 5 + Mockito (backend), Vitest + Testing Library (frontend), Playwright (E2E) | Required by the skills |
| CI/CD | GitHub Actions: lint → unit tests → docker builds → GCP auth → Artifact Registry → Cloud Run → post-deploy functional tests + committed report | Required by the `github-actions-ci-cd` skill |
| Deploy | Two Cloud Run services: `genapp-backend`, `genapp-frontend` | Same pattern as the previous carddemo run |

### API contract (v1)

```
GET    /api/health
GET    /api/customers/{customerNumber}                 → customer + RC semantics
POST   /api/customers                                  → create (number from sequence)
PUT    /api/customers/{customerNumber}                 → update
GET    /api/policies/{policyType}/{customerNumber}/{policyNumber}
GET    /api/policies/commercial?customerNumber=|policyNumber=|postcode=
POST   /api/policies/{policyType}                      → create (number from sequence)
PUT    /api/policies/{policyType}/{customerNumber}/{policyNumber}
DELETE /api/policies/{policyType}/{customerNumber}/{policyNumber}
POST   /api/admin/setup                                → LGSETUP equivalent
```

`policyType` ∈ `motor | endowment | house | commercial`. Every response
that mirrors a legacy failure carries `returnCode` (`01/02/70/90/98/99`)
plus an HTTP status (404/409/422/500) so both the UI and functional
tests can assert parity.
