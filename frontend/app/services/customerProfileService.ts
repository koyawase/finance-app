import type { Customer, CustomerDetails } from "@/app/types/customer";

const CUSTOMER_URL = "http://localhost:8080/customer";

async function customerRequest<T>(token: string, init: RequestInit): Promise<T> {
  const response = await fetch(CUSTOMER_URL, {
    ...init,
    headers: {
      Authorization: `Bearer ${token}`,
      "Content-Type": "application/json",
      ...init.headers,
    },
  });

  if (!response.ok) {
    throw new Error(`Backend returned ${response.status}: ${await response.text()}`);
  }

  return response.json() as Promise<T>;
}

export function getOrCreateCustomer(token: string, email: string) {
  return customerRequest<Customer>(token, {
    method: "POST",
    body: JSON.stringify({ email }),
  });
}

export function updateCustomerDetails(token: string, details: CustomerDetails) {
  return customerRequest<Customer>(token, {
    method: "PATCH",
    body: JSON.stringify(details),
  });
}
