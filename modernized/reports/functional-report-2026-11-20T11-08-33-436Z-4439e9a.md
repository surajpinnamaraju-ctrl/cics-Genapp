# GenApp Post-Deployment Functional Test Report

- **Timestamp (UTC):** 2026-11-20T11:08:33.436Z
- **Commit SHA:** 4439e9aacd330403d6777b992b8d2c0b6d899048
- **Backend URL:** https://genapp-backend-ziqswljzga-uc.a.run.app
- **Frontend URL:** (not set)

## Summary

| Total | Passed | Failed | Skipped | Score |
| --- | --- | --- | --- | --- |
| 19 | 18 | 0 | 1 | 94.7% |

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
| 19 | frontend checks | SKIPPED | FRONTEND_URL not set |
