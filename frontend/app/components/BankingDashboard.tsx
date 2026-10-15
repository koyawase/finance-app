"use client";

import Button from "@/app/components/Button";
import { useBankAccounts } from "@/app/hooks/useBankAccounts";
import AccountGrid from "@/app/components/account/AccountGrid";
import TransactionHistory from "@/app/components/account/TransactionHistory";
import type { Customer } from "@/app/types/customer";

export default function BankingDashboard({ customer, onSignOut }: { customer: Customer; onSignOut: () => void }) {
  const bank = useBankAccounts();

  return (
    <main className="banking-screen">
      <div className="banking-dashboard">
        <header className="banking-header">
          <div>
            <p className="eyebrow">BigMoney</p>
            <h1>Welcome, {customer.name}!</h1>
          </div>
          <Button variant="secondary" onClick={onSignOut}>Log out</Button>
        </header>

        {bank.selectedAccount ? (
          <TransactionHistory
            account={bank.selectedAccount}
            transactions={bank.transactions}
            loading={bank.transactionsLoading}
            error={bank.transactionsError}
            onBack={bank.closeAccount}
          />
        ) : (
          <AccountGrid
            accounts={bank.accounts}
            loading={bank.loading}
            error={bank.error}
            onSelect={bank.openAccount}
            onCreate={bank.createAccount}
          />
        )}
      </div>
    </main>
  );
}

