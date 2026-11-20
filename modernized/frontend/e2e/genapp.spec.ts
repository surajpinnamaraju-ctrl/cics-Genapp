import { expect, test } from '@playwright/test';

const ARTIFACTS = 'test-results/artifacts';

test.describe('GenApp modernization — browser functional tests', () => {
  test('dashboard loads and shows backend health', async ({ page }) => {
    await page.goto('/');
    await expect(page.locator('.brand')).toContainText('GENAPP');
    await expect(page.getByText(/Backend UP/)).toBeVisible();
    await page.screenshot({ path: `${ARTIFACTS}/01-dashboard.png`, fullPage: true });
  });

  test('customer inquire returns seeded customer', async ({ page }) => {
    await page.goto('/customers');
    await page.getByRole('button', { name: 'Inquire' }).last().click();
    await expect(page.getByText('Andrew Pandy')).toBeVisible();
    await expect(page.getByText('PI101OO')).toBeVisible();
    await page.screenshot({ path: `${ARTIFACTS}/02-customer-inquire.png`, fullPage: true });
  });

  test('customer inquire with unknown number shows RC 01', async ({ page }) => {
    await page.goto('/customers');
    await page.getByLabel(/Customer number/).fill('999');
    await page.getByRole('button', { name: 'Inquire' }).last().click();
    await expect(page.getByRole('alert')).toContainText('[RC 01]');
  });

  test('customer add allocates identity number and round-trips', async ({ page }) => {
    await page.goto('/customers');
    await page.getByRole('button', { name: 'Add', exact: true }).click();
    await page.getByLabel(/First name/).fill('E2E');
    await page.getByLabel(/Last name/).fill('Browser');
    await page.getByLabel(/Postcode/).fill('zz99yy');
    await page.getByRole('button', { name: 'Add customer' }).click();
    await expect(page.getByText(/Added customer 10000\d\d/)).toBeVisible();
    await expect(page.getByLabel(/Postcode/)).toHaveValue('ZZ99YY');
    await page.screenshot({ path: `${ARTIFACTS}/03-customer-add.png`, fullPage: true });
  });

  test('customer field length validation returns RC 98', async ({ page }) => {
    await page.goto('/customers');
    await page.getByRole('button', { name: 'Add', exact: true }).click();
    await page.getByLabel(/First name/).fill('A'.repeat(11));
    await page.getByLabel(/Last name/).fill('Toolong');
    await page.getByRole('button', { name: 'Add customer' }).click();
    await expect(page.getByRole('alert')).toContainText('[RC 98]');
  });

  test('motor policy inquire returns seeded policy', async ({ page }) => {
    await page.goto('/policies/motor');
    await page.getByRole('button', { name: 'Inquire' }).last().click();
    await expect(page.getByText('FORD')).toBeVisible();
    await expect(page.getByText('LL60LOO')).toBeVisible();
    await page.screenshot({ path: `${ARTIFACTS}/04-policy-inquire.png`, fullPage: true });
  });

  test('policy inquire with wrong keys shows RC 01', async ({ page }) => {
    await page.goto('/policies/motor');
    await page.getByLabel(/Policy number/).fill('999');
    await page.getByRole('button', { name: 'Inquire' }).last().click();
    await expect(page.getByRole('alert')).toContainText('[RC 01]');
  });

  test('motor policy add and delete round-trip', async ({ page }) => {
    await page.goto('/policies/motor');
    await page.getByRole('button', { name: 'Add', exact: true }).click();
    await page.getByLabel(/Make/).fill('E2E');
    await page.getByLabel(/Model/).fill('TEST');
    await page.getByLabel(/Registration/).fill('E2ETEST');
    await page.getByRole('button', { name: 'Add policy' }).click();
    await expect(page.getByText(/Added motor policy/)).toBeVisible();

    await page.screenshot({ path: `${ARTIFACTS}/05-policy-add.png`, fullPage: true });

    // delete the policy just created (customer 2, new policy number from success message)
    const match = (await page.getByText(/Added motor policy (\d+)/).textContent())?.match(/(\d+)/);
    const newNumber = match?.[1];
    expect(newNumber).toBeTruthy();
    await page.getByRole('button', { name: 'Delete', exact: true }).click();
    await page.getByLabel(/Policy number/).fill(newNumber!);
    await page.getByRole('button', { name: 'Delete policy' }).click();
    await expect(page.getByText(/Deleted motor policy/)).toBeVisible();
  });

  test('commercial search by postcode returns seeded policy', async ({ page }) => {
    await page.goto('/policies/commercial');
    await page.getByRole('button', { name: 'Search', exact: true }).click();
    await page.getByLabel(/Postcode/).fill('BB104BX');
    await page.getByRole('button', { name: 'Search' }).last().click();
    await expect(page.getByRole('cell', { name: 'Burnley Football Club' })).toBeVisible();
    await page.screenshot({ path: `${ARTIFACTS}/06-commercial-search.png`, fullPage: true });
  });

  test('house policy type mismatch shows RC 01', async ({ page }) => {
    // policy 1 is motor; requesting it under house must fail
    await page.goto('/policies/house');
    await page.getByLabel(/Customer number/).fill('2');
    await page.getByLabel(/Policy number/).fill('1');
    await page.getByRole('button', { name: 'Inquire' }).last().click();
    await expect(page.getByRole('alert')).toContainText('[RC 01]');
  });
});
