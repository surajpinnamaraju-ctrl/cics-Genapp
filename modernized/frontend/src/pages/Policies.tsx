import { useMemo, useState } from 'react';
import { useParams } from 'react-router-dom';
import { api, type Policy } from '../api';
import { ErrorBox, Field, KV, Panel, SuccessBox } from '../components';

interface FieldDef {
  name: string;
  label: string;
  kind?: 'text' | 'number' | 'date';
  options?: { value: string; label: string }[];
}

interface TypeConfig {
  title: string;
  sub: string;
  path: string;
  fields: FieldDef[];
  supportsUpdate: boolean;
}

const YN = [
  { value: 'Y', label: 'Yes' },
  { value: 'N', label: 'No' },
];

const CONFIGS: Record<string, TypeConfig> = {
  motor: {
    title: 'Motor Policies',
    sub: 'Car and vehicle cover — inquire by customer and policy number, add, update or delete.',
    path: 'motor',
    supportsUpdate: true,
    fields: [
      { name: 'make', label: 'Make' },
      { name: 'model', label: 'Model' },
      { name: 'value', label: 'Value', kind: 'number' },
      { name: 'regNumber', label: 'Registration' },
      { name: 'colour', label: 'Colour' },
      { name: 'cc', label: 'Engine (cc)', kind: 'number' },
      { name: 'yearOfManufacture', label: 'Year of manufacture', kind: 'date' },
      { name: 'premium', label: 'Premium', kind: 'number' },
      { name: 'accidents', label: 'Accidents', kind: 'number' },
    ],
  },
  endowment: {
    title: 'Endowment Policies',
    sub: 'Life assurance and investment bonds.',
    path: 'endowment',
    supportsUpdate: true,
    fields: [
      { name: 'withProfits', label: 'With profits', options: YN },
      { name: 'equities', label: 'Equities', options: YN },
      { name: 'managedFund', label: 'Managed fund', options: YN },
      { name: 'fundName', label: 'Fund name' },
      { name: 'term', label: 'Term (years)', kind: 'number' },
      { name: 'sumAssured', label: 'Sum assured', kind: 'number' },
      { name: 'lifeAssured', label: 'Life assured' },
    ],
  },
  house: {
    title: 'House Policies',
    sub: 'Buildings and contents cover for houses, flats and farms.',
    path: 'house',
    supportsUpdate: true,
    fields: [
      { name: 'propertyType', label: 'Property type', options: [
        { value: 'HOUSE', label: 'House' }, { value: 'FLAT', label: 'Flat' }, { value: 'FARM', label: 'Farm' }] },
      { name: 'bedrooms', label: 'Bedrooms', kind: 'number' },
      { name: 'value', label: 'Value', kind: 'number' },
      { name: 'houseName', label: 'House name' },
      { name: 'houseNumber', label: 'House number' },
      { name: 'postcode', label: 'Postcode' },
    ],
  },
  commercial: {
    title: 'Commercial Policies',
    sub: 'Commercial property cover — search by policy number, customer or postcode.',
    path: 'commercial',
    supportsUpdate: false,
    fields: [
      { name: 'customer', label: 'Customer (name)' },
      { name: 'address', label: 'Address' },
      { name: 'zipcode', label: 'Zipcode' },
      { name: 'latitude', label: 'Latitude' },
      { name: 'longitude', label: 'Longitude' },
      { name: 'propertyType', label: 'Property type' },
      { name: 'status', label: 'Status (0=pending 1=active)', kind: 'number' },
      { name: 'rejectionReason', label: 'Rejection reason' },
      { name: 'firePeril', label: 'Fire peril', kind: 'number' },
      { name: 'firePremium', label: 'Fire premium', kind: 'number' },
      { name: 'crimePeril', label: 'Crime peril', kind: 'number' },
      { name: 'crimePremium', label: 'Crime premium', kind: 'number' },
      { name: 'floodPeril', label: 'Flood peril', kind: 'number' },
      { name: 'floodPremium', label: 'Flood premium', kind: 'number' },
      { name: 'weatherPeril', label: 'Weather peril', kind: 'number' },
      { name: 'weatherPremium', label: 'Weather premium', kind: 'number' },
    ],
  },
};

type Tab = 'inquire' | 'add' | 'update' | 'delete' | 'search';

const NUMERIC = new Set([
  'value', 'cc', 'yearOfManufacture', 'premium', 'accidents', 'term', 'sumAssured',
  'bedrooms', 'status', 'firePeril', 'firePremium', 'crimePeril', 'crimePremium',
  'floodPeril', 'floodPremium', 'weatherPeril', 'weatherPremium', 'payment', 'brokerId',
]);

export default function Policies() {
  const { type = 'motor' } = useParams();
  const cfg = CONFIGS[type] ?? CONFIGS.motor;

  const [tab, setTab] = useState<Tab>('inquire');
  const [customerNumber, setCustomerNumber] = useState('2');
  const [policyNumber, setPolicyNumber] = useState('1');
  const [form, setForm] = useState<Record<string, string>>({});
  const [policy, setPolicy] = useState<Policy | null>(null);
  const [results, setResults] = useState<Policy[] | null>(null);
  const [searchForm, setSearchForm] = useState({ policyNumber: '', customer: '', postcode: '' });
  const [error, setError] = useState<unknown>(null);
  const [success, setSuccess] = useState<string | null>(null);

  const tabs = useMemo<Tab[]>(() => {
    const t: Tab[] = ['inquire', 'add'];
    if (cfg.supportsUpdate) t.push('update');
    t.push('delete');
    if (cfg.path === 'commercial') t.push('search');
    return t;
  }, [cfg]);

  const set = (k: string) => (v: string) => setForm((f) => ({ ...f, [k]: v }));
  const setSearch = (k: keyof typeof searchForm) => (v: string) =>
    setSearchForm((s) => ({ ...s, [k]: v }));

  function policyToForm(p: Policy): Record<string, string> {
    const f: Record<string, string> = {
      customerNumber: String(p.customerNumber),
      issueDate: p.issueDate ?? '',
      expiryDate: p.expiryDate ?? '',
      lastChanged: p.lastChanged ?? '',
    };
    for (const fd of cfg.fields) {
      const v = (p as unknown as Record<string, unknown>)[fd.name];
      f[fd.name] = v == null ? '' : String(v);
    }
    // commercial search fields live on the response too
    if (cfg.path === 'commercial') {
      f.zipcode = p.zipcode ?? '';
    }
    return f;
  }

  function formToBody(): Record<string, unknown> {
    const body: Record<string, unknown> = {};
    for (const [k, v] of Object.entries(form)) {
      if (v === '' || v == null) continue;
      if (NUMERIC.has(k)) {
        body[k] = Number(v);
      } else {
        body[k] = v;
      }
    }
    return body;
  }

  async function inquire() {
    setError(null); setSuccess(null); setResults(null);
    try {
      const p = await api.getPolicy(cfg.path, Number(customerNumber), Number(policyNumber));
      setPolicy(p);
      setForm(policyToForm(p));
    } catch (e) {
      setPolicy(null);
      setError(e);
    }
  }

  async function save(isUpdate: boolean) {
    setError(null); setSuccess(null);
    const body = formToBody();
    if (!isUpdate) body.customerNumber = Number(customerNumber);
    try {
      const p = isUpdate
        ? await api.updatePolicy(cfg.path, Number(customerNumber), Number(policyNumber), body)
        : await api.addPolicy(cfg.path, body);
      setPolicy(p);
      setForm(policyToForm(p));
      setSuccess(`${isUpdate ? 'Updated' : 'Added'} ${cfg.path} policy ${p.policyNumber}`);
      if (!isUpdate) setPolicyNumber(String(p.policyNumber));
    } catch (e) {
      setError(e);
    }
  }

  async function remove() {
    setError(null); setSuccess(null);
    try {
      await api.deletePolicy(cfg.path, Number(customerNumber), Number(policyNumber));
      setPolicy(null);
      setSuccess(`Deleted ${cfg.path} policy ${policyNumber}`);
    } catch (e) {
      setError(e);
    }
  }

  async function search() {
    setError(null); setSuccess(null); setPolicy(null);
    try {
      const res = await api.searchCommercial({
        policyNumber: searchForm.policyNumber ? Number(searchForm.policyNumber) : undefined,
        customer: searchForm.customer || undefined,
        postcode: searchForm.postcode || undefined,
      });
      setResults(res);
    } catch (e) {
      setResults(null);
      setError(e);
    }
  }

  return (
    <div className="page">
      <h1 className="page-title">{cfg.title}</h1>
      <p className="page-sub">{cfg.sub}</p>

      <div className="tabs">
        {tabs.map((t) => (
          <button key={t} className={tab === t ? 'active' : ''} onClick={() => setTab(t)}>
            {t[0].toUpperCase() + t.slice(1)}
          </button>
        ))}
      </div>

      <ErrorBox error={error} />
      <SuccessBox message={success} />

      {tab === 'search' && (
        <Panel title="Search commercial policies">
          <div className="form-grid">
            <Field label="Policy number" name="sPolicyNumber" value={searchForm.policyNumber}
              onChange={setSearch('policyNumber')} type="number" />
            <Field label="Customer" name="sCustomer" value={searchForm.customer}
              onChange={setSearch('customer')} />
            <Field label="Postcode" name="sPostcode" value={searchForm.postcode}
              onChange={setSearch('postcode')} />
          </div>
          <div className="actions">
            <button className="btn" onClick={search}>Search</button>
          </div>
          {results && (
            <table className="data" style={{ marginTop: 16 }}>
              <thead>
                <tr>
                  <th>Policy</th><th>Customer no.</th><th>Customer</th><th>Address</th>
                  <th>Zipcode</th><th>Status</th><th>Last changed</th>
                </tr>
              </thead>
              <tbody>
                {results.map((p) => (
                  <tr key={p.policyNumber}>
                    <td><span className="badge">{p.policyNumber}</span></td>
                    <td>{p.customerNumber}</td>
                    <td>{p.customer}</td>
                    <td>{p.address}</td>
                    <td>{p.zipcode}</td>
                    <td>{p.status === 1 ? 'Active' : 'Pending'}</td>
                    <td>{p.lastChanged}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </Panel>
      )}

      {tab !== 'search' && (
        <Panel title="Policy keys">
          <div className="form-grid">
            <Field label="Customer number" name="customerNumber" value={customerNumber}
              onChange={setCustomerNumber} type="number" required />
            <Field label="Policy number" name="policyNumber" value={policyNumber}
              onChange={setPolicyNumber} type="number" required />
          </div>
          <div className="actions">
            {(tab === 'inquire' || tab === 'update') && (
              <button className="btn" onClick={inquire}>Inquire</button>
            )}
            {tab === 'delete' && (
              <button className="btn danger" onClick={remove}>Delete policy</button>
            )}
          </div>
        </Panel>
      )}

      {(tab === 'add' || tab === 'update') && (
        <Panel title={tab === 'add' ? 'New policy' : 'Policy details (update)'}>
          <div className="form-grid">
            {cfg.fields.map((fd) => (
              <Field
                key={fd.name}
                label={fd.label}
                name={fd.name}
                value={form[fd.name] ?? ''}
                onChange={set(fd.name)}
                type={fd.kind === 'number' ? 'number' : fd.kind === 'date' ? 'date' : 'text'}
                options={fd.options}
              />
            ))}
            <Field label="Issue date" name="issueDate" value={form.issueDate ?? ''}
              onChange={set('issueDate')} type="date" />
            <Field label="Expiry date" name="expiryDate" value={form.expiryDate ?? ''}
              onChange={set('expiryDate')} type="date" />
          </div>
          {tab === 'update' && (
            <p className="muted" style={{ fontSize: 12 }}>
              Optimistic concurrency: the last-changed timestamp from the last inquire is sent with the update.
            </p>
          )}
          <div className="actions">
            <button className="btn" onClick={() => save(tab === 'update')}>
              {tab === 'add' ? 'Add policy' : 'Update policy'}
            </button>
          </div>
        </Panel>
      )}

      {tab === 'inquire' && policy && (
        <Panel title={`Policy ${policy.policyNumber} details`}>
          <KV rows={[
            ['Type', <span className="badge dim">{policy.policyType}</span>],
            ['Customer number', policy.customerNumber],
            ['Issue date', policy.issueDate],
            ['Expiry date', policy.expiryDate],
            ['Last changed', policy.lastChanged],
            ...cfg.fields
              .filter((fd) => {
                const v = (policy as unknown as Record<string, unknown>)[fd.name];
                return v !== null && v !== undefined && v !== '';
              })
              .map((fd) => [fd.label, String((policy as unknown as Record<string, unknown>)[fd.name])] as [string, string]),
          ]} />
        </Panel>
      )}
    </div>
  );
}
