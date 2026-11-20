#!/usr/bin/env node
/**
 * Post-deployment functional tests for the modernized GenApp.
 * Runs against the deployed backend (and frontend proxy) URLs.
 * Writes a Markdown + JSON report to modernized/reports/.
 *
 * Env: BACKEND_URL (required), FRONTEND_URL (optional), GITHUB_SHA (optional).
 * Exit code 1 when any test fails.
 */
import { mkdirSync, writeFileSync } from 'node:fs';
import { join } from 'node:path';

const BACKEND = (process.env.BACKEND_URL || '').replace(/\/$/, '');
const FRONTEND = (process.env.FRONTEND_URL || '').replace(/\/$/, '');
const SHA = process.env.GITHUB_SHA || 'local';

if (!BACKEND) {
  console.error('BACKEND_URL is required');
  process.exit(2);
}

const results = [];

async function check(name, fn) {
  try {
    const detail = await fn();
    results.push({ name, status: 'passed', detail: detail || '' });
    console.log(`PASS  ${name}`);
  } catch (e) {
    results.push({ name, status: 'failed', detail: String(e?.message || e) });
    console.log(`FAIL  ${name}: ${e?.message || e}`);
  }
}

async function api(base, path, options = {}) {
  const res = await fetch(`${base}${path}`, {
    headers: { 'Content-Type': 'application/json' },
    ...options,
  });
  let body = null;
  try { body = await res.json(); } catch { /* 204 etc. */ }
  return { status: res.status, body };
}

function expect(cond, msg) {
  if (!cond) throw new Error(msg);
}

function expectRc(r, status, rc) {
  expect(r.status === status, `expected HTTP ${status}, got ${r.status}`);
  expect(r.body?.returnCode === rc, `expected returnCode ${rc}, got ${JSON.stringify(r.body)}`);
}

// ---------- backend direct ----------

await check('backend health endpoint returns UP', async () => {
  const r = await api(BACKEND, '/api/health');
  expect(r.status === 200 && r.body?.status === 'UP', `unexpected: ${JSON.stringify(r.body)}`);
});

await check('seeded customer 1 inquire (Andrew Pandy)', async () => {
  const r = await api(BACKEND, '/api/customers/1');
  expect(r.status === 200, `HTTP ${r.status}`);
  expect(r.body?.firstName === 'Andrew' && r.body?.lastName === 'Pandy', JSON.stringify(r.body));
});

await check('unknown customer returns RC 01 / HTTP 404', async () => {
  expectRc(await api(BACKEND, '/api/customers/999999'), 404, '01');
});

let newCustomerNumber = null;
await check('customer add allocates identity number >= 1000001', async () => {
  const r = await api(BACKEND, '/api/customers', {
    method: 'POST',
    body: JSON.stringify({
      firstName: 'Func', lastName: 'Test', dateOfBirth: '1990-01-01',
      houseName: '', houseNumber: '1', postcode: 'ab12cd',
      phoneHome: '', phoneMobile: '', emailAddress: 'func@test.com',
    }),
  });
  expect(r.status === 201, `HTTP ${r.status}: ${JSON.stringify(r.body)}`);
  expect(r.body?.customerNumber >= 1000001, `bad number ${r.body?.customerNumber}`);
  expect(r.body?.postcode === 'AB12CD', `postcode not uppercased: ${r.body?.postcode}`);
  newCustomerNumber = r.body.customerNumber;
});

await check('added customer is retrievable', async () => {
  const r = await api(BACKEND, `/api/customers/${newCustomerNumber}`);
  expect(r.status === 200 && r.body?.lastName === 'Test', JSON.stringify(r.body));
});

await check('customer field length validation returns RC 98 / HTTP 400', async () => {
  expectRc(await api(BACKEND, '/api/customers', {
    method: 'POST',
    body: JSON.stringify({ firstName: 'A'.repeat(11), lastName: 'X' }),
  }), 400, '98');
});

await check('customer update replaces fields', async () => {
  const r = await api(BACKEND, '/api/customers/1', {
    method: 'PUT',
    body: JSON.stringify({
      firstName: 'Andrew', lastName: 'Pandy', dateOfBirth: '1950-07-11',
      houseName: ' ', houseNumber: '34', postcode: 'pi101oo',
      phoneHome: '01962 811234', phoneMobile: '07799 123456',
      emailAddress: 'A.Pandy@beebhouse.com',
    }),
  });
  expect(r.status === 200, `HTTP ${r.status}`);
  expect(r.body?.postcode === 'PI101OO', `postcode: ${r.body?.postcode}`);
});

await check('seeded motor policy inquire (customer 2, policy 1)', async () => {
  const r = await api(BACKEND, '/api/policies/motor/2/1');
  expect(r.status === 200, `HTTP ${r.status}`);
  expect(r.body?.make === 'FORD' && r.body?.model === 'KA', JSON.stringify(r.body));
});

await check('unknown policy returns RC 01 / HTTP 404', async () => {
  expectRc(await api(BACKEND, '/api/policies/motor/2/999999'), 404, '01');
});

await check('policy type mismatch returns RC 01 / HTTP 404', async () => {
  expectRc(await api(BACKEND, '/api/policies/house/2/1'), 404, '01');
});

await check('policy add for missing customer returns RC 70 / HTTP 422', async () => {
  expectRc(await api(BACKEND, '/api/policies/motor', {
    method: 'POST',
    body: JSON.stringify({ customerNumber: 999999, make: 'X' }),
  }), 422, '70');
});

let newPolicyNumber = null;
let newPolicyLastChanged = null;
await check('motor policy add allocates identity number', async () => {
  const r = await api(BACKEND, '/api/policies/motor', {
    method: 'POST',
    body: JSON.stringify({
      customerNumber: 2, issueDate: '2024-01-01', expiryDate: '2025-01-01',
      make: 'Func', model: 'E2E', value: 1000, regNumber: 'FUNE2E', colour: 'BLUE',
      cc: 1000, yearOfManufacture: '2020-01-01', premium: 100, accidents: 0,
    }),
  });
  expect(r.status === 201, `HTTP ${r.status}: ${JSON.stringify(r.body)}`);
  expect(r.body?.policyNumber >= 1000001, `bad number ${r.body?.policyNumber}`);
  expect(r.body?.regNumber === 'FUNE2E', `regNumber: ${r.body?.regNumber}`);
  newPolicyNumber = r.body.policyNumber;
  newPolicyLastChanged = r.body.lastChanged;
});

await check('policy update with stale lastChanged returns RC 02 / HTTP 409', async () => {
  expectRc(await api(BACKEND, `/api/policies/motor/2/${newPolicyNumber}`, {
    method: 'PUT',
    body: JSON.stringify({ customerNumber: 2, lastChanged: '2000-01-01T00:00:00', make: 'STALE' }),
  }), 409, '02');
});

await check('policy update with current lastChanged succeeds', async () => {
  const r = await api(BACKEND, `/api/policies/motor/2/${newPolicyNumber}`, {
    method: 'PUT',
    body: JSON.stringify({
      customerNumber: 2, lastChanged: newPolicyLastChanged, make: 'FuncUpdated',
    }),
  });
  expect(r.status === 200, `HTTP ${r.status}: ${JSON.stringify(r.body)}`);
  expect(r.body?.make === 'FuncUpdated', `make: ${r.body?.make}`);
  newPolicyLastChanged = r.body.lastChanged;
});

await check('policy delete removes the policy', async () => {
  const del = await api(BACKEND, `/api/policies/motor/2/${newPolicyNumber}`, { method: 'DELETE' });
  expect(del.status === 204, `HTTP ${del.status}`);
  expectRc(await api(BACKEND, `/api/policies/motor/2/${newPolicyNumber}`), 404, '01');
});

await check('commercial search by postcode returns seeded policy', async () => {
  const r = await api(BACKEND, '/api/policies/commercial/search?postcode=BB104BX');
  expect(r.status === 200, `HTTP ${r.status}`);
  expect(Array.isArray(r.body) && r.body.length === 1, JSON.stringify(r.body));
  expect(r.body[0].customer === 'Burnley Football Club', r.body[0].customer);
});

await check('commercial update is unsupported (RC 99 / HTTP 400)', async () => {
  const cur = await api(BACKEND, '/api/policies/commercial/5/9');
  expect(cur.status === 200, `inquire failed: HTTP ${cur.status}`);
  expectRc(await api(BACKEND, '/api/policies/commercial/5/9', {
    method: 'PUT',
    body: JSON.stringify({ customerNumber: 5, lastChanged: cur.body.lastChanged }),
  }), 400, '99');
});

await check('unknown policy type returns RC 99 / HTTP 400', async () => {
  expectRc(await api(BACKEND, '/api/policies/banana/2/1'), 400, '99');
});

// ---------- frontend (validates nginx proxy to backend) ----------

if (FRONTEND) {
  await check('frontend serves the SPA', async () => {
    const res = await fetch(`${FRONTEND}/`);
    expect(res.status === 200, `HTTP ${res.status}`);
    const html = await res.text();
    expect(html.includes('GENAPP') || html.includes('root'), 'SPA shell not found');
  });

  await check('frontend /api proxy reaches backend', async () => {
    const r = await api(FRONTEND, '/api/customers/1');
    expect(r.status === 200 && r.body?.firstName === 'Andrew', `HTTP ${r.status}`);
  });

  await check('frontend negative path returns RC 01 through proxy', async () => {
    expectRc(await api(FRONTEND, '/api/customers/999999'), 404, '01');
  });
} else {
  results.push({ name: 'frontend checks', status: 'skipped', detail: 'FRONTEND_URL not set' });
  console.log('SKIP  frontend checks (FRONTEND_URL not set)');
}

// ---------- report ----------

const total = results.length;
const passed = results.filter((r) => r.status === 'passed').length;
const failed = results.filter((r) => r.status === 'failed').length;
const skipped = results.filter((r) => r.status === 'skipped').length;
const score = total === 0 ? 0 : Math.round((passed / total) * 1000) / 10;
const timestamp = new Date().toISOString();

const md = [
  '# GenApp Post-Deployment Functional Test Report',
  '',
  `- **Timestamp (UTC):** ${timestamp}`,
  `- **Commit SHA:** ${SHA}`,
  `- **Backend URL:** ${BACKEND}`,
  `- **Frontend URL:** ${FRONTEND || '(not set)'}`,
  '',
  '## Summary',
  '',
  `| Total | Passed | Failed | Skipped | Score |`,
  `| --- | --- | --- | --- | --- |`,
  `| ${total} | ${passed} | ${failed} | ${skipped} | ${score}% |`,
  '',
  '## Results',
  '',
  '| # | Test | Status | Detail |',
  '| --- | --- | --- | --- |',
  ...results.map((r, i) => `| ${i + 1} | ${r.name} | ${r.status.toUpperCase()} | ${r.detail.replace(/\|/g, '\\|')} |`),
  '',
].join('\n');

const outDir = join(process.cwd(), 'modernized', 'reports');
mkdirSync(outDir, { recursive: true });
const stamp = timestamp.replace(/[:.]/g, '-');
const base = `functional-report-${stamp}-${SHA.slice(0, 7)}`;
writeFileSync(join(outDir, `${base}.md`), md);
writeFileSync(join(outDir, `${base}.json`), JSON.stringify({ timestamp, sha: SHA, total, passed, failed, skipped, score, results }, null, 2));
writeFileSync(join(outDir, 'latest.md'), md);

console.log(`\nScore: ${score}% (${passed}/${total} passed, ${failed} failed, ${skipped} skipped)`);
console.log(`Report: modernized/reports/${base}.md`);
process.exit(failed > 0 ? 1 : 0);
