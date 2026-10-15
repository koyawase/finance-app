import type { BankAccount, BankTransaction } from "@/app/types/account";

const API_URL = "http://localhost:8080";

async function bankingRequest<T>(token: string, path: string): Promise<T> {
  const response = await fetch(`${API_URL}${path}`, {
    headers: { Authorization: `Bearer ${token}` },
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
