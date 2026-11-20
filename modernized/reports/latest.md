# GenApp Post-Deployment Functional Test Report

- **Timestamp (UTC):** 2026-11-20T11:19:52.314Z
- **Commit SHA:** 2a90763ac06e86fa217c403921f3185de1f800c9
- **Backend URL:** https://genapp-backend-ziqswljzga-uc.a.run.app
- **Frontend URL:** https://genapp-frontend-ziqswljzga-uc.a.run.app

## Summary

| Total | Passed | Failed | Skipped | Score |
| --- | --- | --- | --- | --- |
| 21 | 21 | 0 | 0 | 100% |

## Results

| # | Test | Status | Detail |
| --- | --- | --- | --- |
| 1 | backend health endpoint returns UP | PASSED |  |
| 2 | seeded customer 1 inquire (Andrew Pandy) | PASSED |  |
| 3 | unknown customer returns RC 01 / HTTP 404 | PASSED |  |
| 4 | customer add allocates identity number >= 1000001 | PASSED |  |
| 5 | added customer is retrievable | PASSED |  |
| 6 | customer field length validation returns RC 98 / HTTP 400 | PASSED |  |
| 7 | customer update replaces fields | PASSED |  |
| 8 | seeded motor policy inquire (customer 2, policy 1) | PASSED |  |
| 9 | unknown policy returns RC 01 / HTTP 404 | PASSED |  |
| 10 | policy type mismatch returns RC 01 / HTTP 404 | PASSED |  |
| 11 | policy add for missing customer returns RC 70 / HTTP 422 | PASSED |  |
| 12 | motor policy add allocates identity number | PASSED |  |
| 13 | policy update with stale lastChanged returns RC 02 / HTTP 409 | PASSED |  |
| 14 | policy update with current lastChanged succeeds | PASSED |  |
| 15 | policy delete removes the policy | PASSED |  |
| 16 | commercial search by postcode returns seeded policy | PASSED |  |
| 17 | commercial update is unsupported (RC 99 / HTTP 400) | PASSED |  |
| 18 | unknown policy type returns RC 99 / HTTP 400 | PASSED |  |
| 19 | frontend serves the SPA | PASSED |  |
| 20 | frontend /api proxy reaches backend | PASSED |  |
| 21 | frontend negative path returns RC 01 through proxy | PASSED |  |
