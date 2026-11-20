import { useState } from 'react';
import { api, ApiError, type Customer } from '../api';
import { ErrorBox, Field, KV, Panel, SuccessBox } from '../components';

type Tab = 'inquire' | 'add' | 'update';

const EMPTY: Partial<Customer> = {
  firstName: '', lastName: '', dateOfBirth: '', houseName: '', houseNumber: '',
  postcode: '', phoneHome: '', phoneMobile: '', emailAddress: '',
};

export default function Customers() {
  const [tab, setTab] = useState<Tab>('inquire');
  const [number, setNumber] = useState('1');
  const [customer, setCustomer] = useState<Customer | null>(null);
  const [form, setForm] = useState<Partial<Customer>>(EMPTY);
  const [error, setError] = useState<unknown>(null);
  const [success, setSuccess] = useState<string | null>(null);

  const set = (k: keyof Customer) => (v: string) => setForm((f) => ({ ...f, [k]: v }));

  async function inquire() {
    setError(null); setSuccess(null);
    try {
      const c = await api.getCustomer(Number(number));
      setCustomer(c);
      setForm({ ...EMPTY, ...c });
    } catch (e) {
      setCustomer(null);
      setError(e);
    }
  }

  async function save(isUpdate: boolean) {
    setError(null); setSuccess(null);
    const body = { ...form, customerNumber: isUpdate ? Number(number) : undefined };
    try {
      const c = isUpdate
        ? await api.updateCustomer(Number(number), body)
        : await api.addCustomer(body);
      setCustomer(c);
      setForm({ ...EMPTY, ...c });
      setNumber(String(c.customerNumber));
      setSuccess(`${isUpdate ? 'Updated' : 'Added'} customer ${c.customerNumber}`);
    } catch (e) {
      setError(e);
    }
  }

  return (
    <div className="page">
      <h1 className="page-title">Customers</h1>
      <p className="page-sub">Customer maintenance — inquire, add and update (no delete in the base application).</p>

      <div className="tabs">
        {(['inquire', 'add', 'update'] as Tab[]).map((t) => (
          <button key={t} className={tab === t ? 'active' : ''} onClick={() => setTab(t)}>
            {t[0].toUpperCase() + t.slice(1)}
          </button>
        ))}
      </div>

      <ErrorBox error={error} />
      <SuccessBox message={success} />

      {(tab === 'inquire' || tab === 'update') && (
        <Panel title="Customer number">
          <div className="form-grid">
            <Field label="Customer number" name="customerNumber" value={number}
              onChange={setNumber} type="number" required />
          </div>
          <div className="actions">
            <button className="btn" onClick={inquire}>Inquire</button>
            {tab === 'update' && <button className="btn secondary" onClick={inquire}>Load for update</button>}
          </div>
        </Panel>
      )}

      {(tab === 'add' || tab === 'update') && (
        <Panel title={tab === 'add' ? 'New customer' : 'Customer details'}>
          <div className="form-grid">
            <Field label="First name" name="firstName" value={form.firstName ?? ''} onChange={set('firstName')} required />
            <Field label="Last name" name="lastName" value={form.lastName ?? ''} onChange={set('lastName')} required />
            <Field label="Date of birth" name="dateOfBirth" value={form.dateOfBirth ?? ''} onChange={set('dateOfBirth')} type="date" />
            <Field label="House name" name="houseName" value={form.houseName ?? ''} onChange={set('houseName')} />
            <Field label="House number" name="houseNumber" value={form.houseNumber ?? ''} onChange={set('houseNumber')} />
            <Field label="Postcode" name="postcode" value={form.postcode ?? ''} onChange={set('postcode')} />
            <Field label="Phone (home)" name="phoneHome" value={form.phoneHome ?? ''} onChange={set('phoneHome')} />
            <Field label="Phone (mobile)" name="phoneMobile" value={form.phoneMobile ?? ''} onChange={set('phoneMobile')} />
            <Field label="Email" name="emailAddress" value={form.emailAddress ?? ''} onChange={set('emailAddress')} />
          </div>
          <div className="actions">
            <button className="btn" onClick={() => save(tab === 'update')}>
              {tab === 'add' ? 'Add customer' : 'Update customer'}
            </button>
          </div>
        </Panel>
      )}

      {tab === 'inquire' && customer && (
        <Panel title={`Customer ${customer.customerNumber}`}>
          <KV rows={[
            ['Name', `${customer.firstName} ${customer.lastName}`],
            ['Date of birth', customer.dateOfBirth],
            ['Address', [customer.houseName, customer.houseNumber, customer.postcode].filter(Boolean).join(', ')],
            ['Phone home', customer.phoneHome],
            ['Phone mobile', customer.phoneMobile],
            ['Email', customer.emailAddress],
          ]} />
        </Panel>
      )}
      {tab === 'inquire' && error instanceof ApiError && error.returnCode === '01' && (
        <p className="muted">Customer {number} not found.</p>
      )}
    </div>
  );
}
