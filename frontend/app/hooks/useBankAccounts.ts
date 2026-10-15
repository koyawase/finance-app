"use client";

import { useAuth0 } from "@auth0/auth0-react";
import { useEffect, useState } from "react";
import { getAccountTransactions, getAccounts } from "@/app/services/bankingService";
import type { BankAccount, BankTransaction } from "@/app/types/account";

export function useBankAccounts() {
  const { getAccessTokenSilently } = useAuth0();
  const [accounts, setAccounts] = useState<BankAccount[]>([]);
  const [selectedAccount, setSelectedAccount] = useState<BankAccount | null>(null);
  const [transactions, setTransactions] = useState<BankTransaction[]>([]);
  const [loading, setLoading] = useState(true);
  const [transactionsLoading, setTransactionsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [transactionsError, setTransactionsError] = useState<string | null>(null);

  function openAccount(account: BankAccount) {
    setTransactions([]);
    setTransactionsError(null);
    setTransactionsLoading(true);
    setSelectedAccount(account);
  }

  function closeAccount() {
    setSelectedAccount(null);
    setTransactionsLoading(false);
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

  useEffect(() => {
    if (!selectedAccount) return;

    let cancelled = false;
    setTransactions([]);
    setTransactionsError(null);
    setTransactionsLoading(true);

    async function loadTransactions() {
      try {
        const token = await getAccessTokenSilently();
        const loadedTransactions = await getAccountTransactions(token, selectedAccount.id);
        if (!cancelled) setTransactions(loadedTransactions);
      } catch (cause) {
        if (!cancelled) setTransactionsError(cause instanceof Error ? cause.message : "Could not load transactions");
      } finally {
        if (!cancelled) setTransactionsLoading(false);
      }
    }

    void loadTransactions();
    return () => { cancelled = true; };
  }, [getAccessTokenSilently, selectedAccount]);

  return {
    accounts,
    selectedAccount,
    openAccount,
    closeAccount,
    transactions,
    loading,
    transactionsLoading,
    error,
    transactionsError,
  };
}
