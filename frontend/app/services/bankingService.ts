import type { BankAccount, BankTransaction, CreateAccountInput } from "@/app/types/account";

const API_URL = "http://localhost:8080";

async function bankingRequest<T>(token: string, path: string, init: RequestInit = {}): Promise<T> {
  const response = await fetch(`${API_URL}${path}`, {
    ...init,
    headers: {
      Authorization: `Bearer ${token}`,
      ...init.headers,
    },
  });

  if (!response.ok) {
    throw new Error(`Backend returned ${response.status}: ${await response.text()}`);
  }

  return response.json() as Promise<T>;
}

export function getAccounts(token: string) {
  return bankingRequest<BankAccount[]>(token, "/accounts");
}

export function getAccountTransactions(token: string, accountId: number) {
  return bankingRequest<BankTransaction[]>(token, `/accounts/${accountId}/transactions`);
}

export function createAccount(token: string, account: CreateAccountInput) {
  return bankingRequest<BankAccount>(token, "/accounts", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(account),
  });
}
