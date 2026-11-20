import type { ReactNode } from 'react';
import { ApiError } from './api';

export function ErrorBox({ error }: { error: unknown }) {
  if (!error) return null;
  const msg = error instanceof ApiError ? `[RC ${error.returnCode}] ${error.message}` : String(error);
  return <div className="error-box" role="alert">{msg}</div>;
}

export function SuccessBox({ message }: { message: string | null }) {
  if (!message) return null;
  return <div className="success-box" role="status">{message}</div>;
}

export function Field(props: {
  label: string;
  name: string;
  value: string | number;
  onChange: (value: string) => void;
  type?: string;
  required?: boolean;
  options?: { value: string; label: string }[];
  placeholder?: string;
}) {
  const { label, name, value, onChange, type = 'text', required, options, placeholder } = props;
  return (
    <div className="field">
      <label htmlFor={name}>{label}{required ? ' *' : ''}</label>
      {options ? (
        <select id={name} name={name} value={value} onChange={(e) => onChange(e.target.value)}>
          {options.map((o) => (
            <option key={o.value} value={o.value}>{o.label}</option>
          ))}
        </select>
      ) : (
        <input
          id={name}
          name={name}
          type={type}
          value={value}
          placeholder={placeholder}
          onChange={(e) => onChange(e.target.value)}
        />
      )}
    </div>
  );
}

export function Panel({ title, children }: { title: string; children: ReactNode }) {
  return (
    <section className="panel">
      <h2>{title}</h2>
      {children}
    </section>
  );
}

export function KV({ rows }: { rows: [string, ReactNode][] }) {
  return (
    <table className="data">
      <tbody>
        {rows.map(([k, v]) => (
          <tr key={k}>
            <td className="muted" style={{ width: '38%' }}>{k}</td>
            <td>{v ?? '—'}</td>
          </tr>
        ))}
      </tbody>
    </table>
  );
}
