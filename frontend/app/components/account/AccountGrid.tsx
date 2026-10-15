"use client";

import { useState } from "react";
import AccountCard from "@/app/components/account/AccountCard";
import AccountSkeleton from "@/app/components/account/AccountSkeleton";
import CreateAccountModal from "@/app/components/account/CreateAccountModal";
import type { BankAccount, CreateAccountInput } from "@/app/types/account";

type AccountGridProps = {
  accounts: BankAccount[];
  loading: boolean;
  error: string | null;
  onSelect: (account: BankAccount) => void;
  onCreate: (account: CreateAccountInput) => Promise<void>;
};

export default function AccountGrid({ accounts, loading, error, onSelect, onCreate }: AccountGridProps) {
  const [createModalOpen, setCreateModalOpen] = useState(false);

  return (
    <section>
      <div className="accounts-heading">
        <div>
          <h2>Your accounts</h2>
          <p>Choose an account to see its activity.</p>
        </div>
      </div>
      {error && <p className="dashboard-error" role="alert">{error}</p>}
      <div className="account-grid">
        {loading && (
          <div className="account-grid-loading" role="status" aria-label="Loading accounts" aria-busy="true">
            <AccountSkeleton />
            <AccountSkeleton />
          </div>
        )}
        {!loading && accounts.map((account) => (
          <AccountCard key={account.id} account={account} onClick={() => onSelect(account)} />
        ))}
        <button className="create-account-card" type="button" disabled={loading} onClick={() => setCreateModalOpen(true)}>
          <span className="create-account-plus" aria-hidden="true">+</span>
          <span className="create-account-title">Open a new account</span>
          <span className="create-account-description">Choose a name, type, and opening balance.</span>
        </button>
      </div>
      {createModalOpen && (
        <CreateAccountModal onClose={() => setCreateModalOpen(false)} onSubmit={onCreate} />
      )}
    </section>
  );
}
