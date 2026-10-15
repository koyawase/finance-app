"use client";

import { useAuth0 } from "@auth0/auth0-react";
import { useEffect, useRef, useState } from "react";
import {
  createAccount as createAccountRequest,
  createTransfer as createTransferRequest,
  getAccountTransactions,
  getAccounts,
} from "@/app/services/bankingService";
import type { BankAccount, BankTransaction, CreateAccountInput, TransferInput } from "@/app/types/account";

export function useBankAccounts() {
  const { getAccessTokenSilently } = useAuth0();
  const [accounts, setAccounts] = useState<BankAccount[]>([]);
  const [selectedAccount, setSelectedAccount] = useState<BankAccount | null>(null);
  const [transactions, setTransactions] = useState<BankTransaction[]>([]);
  const [loading, setLoading] = useState(true);
  const [transactionsLoading, setTransactionsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [transactionsError, setTransactionsError] = useState<string | null>(null);
  const transactionRequestId = useRef(0);

  function openAccount(account: BankAccount) {
    setTransactions([]);
    setTransactionsError(null);
    setTransactionsLoading(true);
    setSelectedAccount(account);
  }

  function closeAccount() {
    transactionRequestId.current += 1;
    setSelectedAccount(null);
    setTransactionsLoading(false);
  }

  async function createAccount(input: CreateAccountInput) {
    const token = await getAccessTokenSilently();
    const account = await createAccountRequest(token, input);
    setError(null);
    setAccounts((currentAccounts) => [...currentAccounts, account]);
  }

  async function transfer(input: TransferInput) {
    if (!selectedAccount) throw new Error("Select an account to transfer from.");

    const sourceAccount = selectedAccount;
    const token = await getAccessTokenSilently();
    await createTransferRequest(token, {
      ...input,
      fromAccountNumber: sourceAccount.accountNumber,
    });

    setTransactionsError(null);
    setTransactionsLoading(true);
    const requestId = ++transactionRequestId.current;
    const refreshErrors: string[] = [];

    try {
      const updatedAccounts = await getAccounts(token);
      setAccounts(updatedAccounts);
      const updatedSourceAccount = updatedAccounts.find((account) => account.id === sourceAccount.id);
      if (updatedSourceAccount && selectedAccount?.id === sourceAccount.id) setSelectedAccount(updatedSourceAccount);
    } catch (cause) {
      refreshErrors.push(`Could not refresh account balances: ${errorMessage(cause)}`);
    }

    try {
      const updatedTransactions = await getAccountTransactions(token, sourceAccount.id);
      if (transactionRequestId.current === requestId) setTransactions(updatedTransactions);
    } catch (cause) {
      if (transactionRequestId.current === requestId) {
        refreshErrors.push(`Could not refresh transaction history: ${errorMessage(cause)}`);
      }
    }

    if (transactionRequestId.current === requestId) {
      setTransactionsError(refreshErrors.length ? `Transfer completed. ${refreshErrors.join(" ")}` : null);
      setTransactionsLoading(false);
    }
  }

  useEffect(() => {
    let cancelled = false;

    async function loadAccounts() {
      try {
        const token = await getAccessTokenSilently();
        const loadedAccounts = await getAccounts(token);
        if (!cancelled) setAccounts(loadedAccounts);
      } catch (cause) {
        if (!cancelled) setError(cause instanceof Error ? cause.message : "Could not load accounts");
      } finally {
        if (!cancelled) setLoading(false);
      }
    }

    void loadAccounts();
    return () => { cancelled = true; };
  }, [getAccessTokenSilently]);

  const selectedAccountId = selectedAccount?.id;

  useEffect(() => {
    if (selectedAccountId == null) return;

    let cancelled = false;
    const requestId = ++transactionRequestId.current;
    setTransactions([]);
    setTransactionsError(null);
    setTransactionsLoading(true);

    async function loadTransactions() {
      try {
        const token = await getAccessTokenSilently();
        const loadedTransactions = await getAccountTransactions(token, selectedAccountId);
        if (!cancelled && transactionRequestId.current === requestId) setTransactions(loadedTransactions);
      } catch (cause) {
        if (!cancelled && transactionRequestId.current === requestId) {
          setTransactionsError(cause instanceof Error ? cause.message : "Could not load transactions");
        }
      } finally {
        if (!cancelled && transactionRequestId.current === requestId) setTransactionsLoading(false);
      }
    }

    void loadTransactions();
    return () => { cancelled = true; };
  }, [getAccessTokenSilently, selectedAccountId]);

  return {
    accounts,
    selectedAccount,
    openAccount,
    closeAccount,
    createAccount,
    transfer,
    transactions,
    loading,
    transactionsLoading,
    error,
    transactionsError,
  };
}

function errorMessage(cause: unknown) {
  return cause instanceof Error ? cause.message : "an unexpected error occurred.";
}
