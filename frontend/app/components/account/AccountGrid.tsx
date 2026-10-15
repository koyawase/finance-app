import AccountCard from "@/app/components/account/AccountCard";
import AccountSkeleton from "@/app/components/account/AccountSkeleton";
import type { BankAccount } from "@/app/types/account";

type AccountGridProps = {
  accounts: BankAccount[];
  loading: boolean;
  error: string | null;
  onSelect: (account: BankAccount) => void;
};

export default function AccountGrid({ accounts, loading, error, onSelect }: AccountGridProps) {
  return (
    <section>
      <div className="accounts-heading">
        <div>
          <h2>Your accounts</h2>
          <p>Choose an account to see its activity.</p>
        </div>
      </div>
      {loading && (
        <div className="account-grid" role="status" aria-label="Loading accounts" aria-busy="true">
          <AccountSkeleton />
          <AccountSkeleton />
        </div>
      )}
      {error && <p className="dashboard-error" role="alert">{error}</p>}
      {!loading && !error && accounts.length === 0 && (
        <p className="dashboard-message">No accounts to show yet.</p>
      )}
      {!loading && accounts.length > 0 && (
        <div className="account-grid">
          {accounts.map((account) => (
            <AccountCard key={account.id} account={account} onClick={() => onSelect(account)} />
          ))}
        </div>
      )}
    </section>
  );
}
