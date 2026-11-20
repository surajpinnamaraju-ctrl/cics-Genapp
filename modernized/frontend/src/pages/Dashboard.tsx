import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { api } from '../api';

interface Health {
  status: string;
  service: string;
}

const SECTIONS = [
  { to: '/customers', title: 'Customers', desc: 'Inquire, add and update customer records (SSC1).' },
  { to: '/policies/motor', title: 'Motor Policies', desc: 'Inquire, add, update and delete motor policies (SSP1).' },
  { to: '/policies/endowment', title: 'Endowment Policies', desc: 'With-profits, equities and managed funds (SSP2).' },
  { to: '/policies/house', title: 'House Policies', desc: 'Property cover for houses, flats and farms (SSP3).' },
  { to: '/policies/commercial', title: 'Commercial Policies', desc: 'Search by policy, customer or postcode (SSP4).' },
];

export default function Dashboard() {
  const [health, setHealth] = useState<Health | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    api.health().then(setHealth).catch((e) => setError(String(e)));
  }, []);

  return (
    <div className="page">
      <h1 className="page-title">GenApp</h1>
      <p className="page-sub">
        General insurance administration, modernized from the IBM CICS/Db2 mainframe application.
      </p>
      {health && (
        <p className="page-sub">
          <span className="status-dot" /> Backend {health.status} — {health.service}
        </p>
      )}
      {error && <div className="error-box">Backend unreachable: {error}</div>}
      <div className="grid">
        {SECTIONS.map((s) => (
          <Link key={s.to} to={s.to} className="card">
            <h3>{s.title}</h3>
            <p>{s.desc}</p>
          </Link>
        ))}
      </div>
    </div>
  );
}
