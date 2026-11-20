import { fireEvent, render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import App from './App';
import { api, ApiError } from './api';

vi.mock('./api', async (importOriginal) => {
  const actual = await importOriginal<typeof import('./api')>();
  return {
    ...actual,
    api: {
      health: vi.fn(),
      getCustomer: vi.fn(),
      addCustomer: vi.fn(),
      updateCustomer: vi.fn(),
      getPolicy: vi.fn(),
      addPolicy: vi.fn(),
      updatePolicy: vi.fn(),
      deletePolicy: vi.fn(),
      searchCommercial: vi.fn(),
    },
  };
});

function renderAt(path: string) {
  return render(
    <MemoryRouter initialEntries={[path]}>
      <App />
    </MemoryRouter>,
  );
}

// The page has an "Inquire" tab and an "Inquire" action button; click the action.
function clickInquire() {
  const buttons = screen.getAllByRole('button', { name: 'Inquire' });
  fireEvent.click(buttons[buttons.length - 1]);
}

describe('App shell', () => {
  beforeEach(() => {
    vi.mocked(api.health).mockResolvedValue({ status: 'UP', service: 'genapp-backend' });
  });

  it('renders the dashboard with navigation', async () => {
    renderAt('/');
    expect(screen.getByText('GENAPP')).toBeInTheDocument();
    expect(screen.getAllByText('Customers').length).toBeGreaterThan(0);
    expect(await screen.findByText(/Backend UP/)).toBeInTheDocument();
  });

  it('inquires a customer and shows details', async () => {
    vi.mocked(api.getCustomer).mockResolvedValue({
      customerNumber: 1, firstName: 'Andrew', lastName: 'Pandy', dateOfBirth: '1950-07-11',
      houseName: '', houseNumber: '34', postcode: 'PI101OO', phoneHome: '01962 811234',
      phoneMobile: '07799 123456', emailAddress: 'A.Pandy@beebhouse.com', numPolicies: 0,
    });
    renderAt('/customers');
    clickInquire();
    expect(await screen.findByText('Andrew Pandy')).toBeInTheDocument();
    expect(api.getCustomer).toHaveBeenCalledWith(1);
  });

  it('shows the legacy return code when an inquire fails', async () => {
    vi.mocked(api.getCustomer).mockRejectedValue(new ApiError(404, '01', 'Customer 999 not found'));
    renderAt('/customers');
    fireEvent.change(screen.getByLabelText(/Customer number/), { target: { value: '999' } });
    clickInquire();
    expect(await screen.findByRole('alert')).toHaveTextContent('[RC 01]');
  });

  it('inquires a motor policy by keys', async () => {
    vi.mocked(api.getPolicy).mockResolvedValue({
      policyNumber: 1, customerNumber: 2, policyType: 'M', issueDate: '2011-05-11',
      expiryDate: '2016-05-10', lastChanged: '2011-05-11T17:12:17', brokerId: 0,
      brokersReference: '', payment: 0, make: 'FORD', model: 'KA', regNumber: 'LL60LOO',
    });
    renderAt('/policies/motor');
    clickInquire();
    expect(await screen.findByText('FORD')).toBeInTheDocument();
    expect(api.getPolicy).toHaveBeenCalledWith('motor', 2, 1);
  });
});
