import type { BankAccount, BankTransaction, CreateAccountInput, Transfer, TransferRequest } from "@/app/types/account";

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
    throw new Error(await responseError(response));
  }

  return response.json() as Promise<T>;
}

async function responseError(response: Response) {
  const body = await response.text();
  let message = body;

  try {
    const payload: unknown = JSON.parse(body);
    if (payload && typeof payload === "object") {
      if ("message" in payload && typeof payload.message === "string") message = payload.message;
      else if ("detail" in payload && typeof payload.detail === "string") message = payload.detail;
    }
  } catch {
    // The backend may return a plain-text error body.
  }

  return message ? `Request failed (${response.status}): ${message}` : `Request failed (${response.status}).`;
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

export function createTransfer(token: string, transfer: TransferRequest) {
  return bankingRequest<Transfer>(token, "/transfers", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(transfer),
  });
}
