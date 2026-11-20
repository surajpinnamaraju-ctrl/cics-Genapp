export interface ApiErrorBody {
  returnCode: string;
  message: string;
}

export class ApiError extends Error {
  constructor(public status: number, public returnCode: string, message: string) {
    super(message);
  }
}

async function request<T>(path: string, options?: RequestInit): Promise<T> {
  const res = await fetch(path, {
    headers: { 'Content-Type': 'application/json' },
    ...options,
  });
  if (!res.ok) {
    let body: ApiErrorBody = { returnCode: '90', message: res.statusText };
    try {
      body = await res.json();
    } catch {
      /* non-JSON error body */
    }
    throw new ApiError(res.status, body.returnCode, body.message);
  }
  if (res.status === 204) return undefined as T;
  return res.json() as Promise<T>;
}

export const api = {
  health: () => request<{ status: string; service: string }>('/api/health'),

  getCustomer: (n: number) => request<Customer>(`/api/customers/${n}`),
  addCustomer: (body: Partial<Customer>) =>
    request<Customer>('/api/customers', { method: 'POST', body: JSON.stringify(body) }),
  updateCustomer: (n: number, body: Partial<Customer>) =>
    request<Customer>(`/api/customers/${n}`, { method: 'PUT', body: JSON.stringify(body) }),

  getPolicy: (type: string, customer: number, policy: number) =>
    request<Policy>(`/api/policies/${type}/${customer}/${policy}`),
  addPolicy: (type: string, body: Partial<Policy>) =>
    request<Policy>(`/api/policies/${type}`, { method: 'POST', body: JSON.stringify(body) }),
  updatePolicy: (type: string, customer: number, policy: number, body: Partial<Policy>) =>
    request<Policy>(`/api/policies/${type}/${customer}/${policy}`, {
      method: 'PUT',
      body: JSON.stringify(body),
    }),
  deletePolicy: (type: string, customer: number, policy: number) =>
    request<void>(`/api/policies/${type}/${customer}/${policy}`, { method: 'DELETE' }),
  searchCommercial: (params: { policyNumber?: number; customer?: string; postcode?: string }) => {
    const q = new URLSearchParams();
    if (params.policyNumber) q.set('policyNumber', String(params.policyNumber));
    if (params.customer) q.set('customer', params.customer);
    if (params.postcode) q.set('postcode', params.postcode);
    return request<Policy[]>(`/api/policies/commercial/search?${q}`);
  },
};

export interface Customer {
  customerNumber: number;
  firstName: string;
  lastName: string;
  dateOfBirth: string;
  houseName: string;
  houseNumber: string;
  postcode: string;
  phoneHome: string;
  phoneMobile: string;
  emailAddress: string;
  numPolicies: number;
}

/** Mirrors the backend PolicyResponse union; only the policy-type block is set. */
export interface Policy {
  policyNumber: number;
  customerNumber: number;
  policyType: string;
  issueDate: string;
  expiryDate: string;
  lastChanged: string;
  brokerId: number;
  brokersReference: string;
  payment: number;
  make?: string;
  model?: string;
  value?: number;
  regNumber?: string;
  colour?: string;
  cc?: number;
  yearOfManufacture?: string;
  premium?: number;
  accidents?: number;
  withProfits?: string;
  equities?: string;
  managedFund?: string;
  fundName?: string;
  term?: number;
  sumAssured?: number;
  lifeAssured?: string;
  paddingData?: string;
  propertyType?: string;
  bedrooms?: number;
  houseName?: string;
  houseNumber?: string;
  housePostcode?: string;
  requestDate?: string;
  startDate?: string;
  renewalDate?: string;
  address?: string;
  zipcode?: string;
  latitude?: string;
  longitude?: string;
  customer?: string;
  firePeril?: number;
  firePremium?: number;
  crimePeril?: number;
  crimePremium?: number;
  floodPeril?: number;
  floodPremium?: number;
  weatherPeril?: number;
  weatherPremium?: number;
  status?: number;
  rejectionReason?: string;
}
