import TransactionRow from "@/app/components/account/TransactionRow";
import { accountTypeLabel, formatMoney } from "@/app/components/account/formatters";
import type { BankAccount, BankTransaction } from "@/app/types/account";

type TransactionHistoryProps = {
  account: BankAccount;
  transactions: BankTransaction[];
  loading: boolean;
  error: string | null;
  onBack: () => void;
};

export default function TransactionHistory({ account, transactions, loading, error, onBack }: TransactionHistoryProps) {
  return (
    <section className="transaction-view">
      <button className="back-button" onClick={onBack}>
        <span aria-hidden="true">←</span> All accounts
      </button>
      <div className="history-heading">
        <div>
          <p className="eyebrow">{accountTypeLabel(account.accountType)}</p>
          <h2>{account.accountName}</h2>
          <p className="account-number">Account number {account.accountNumber}</p>
        </div>
        <p className="history-balance">{formatMoney(account.balance)}</p>
      </div>

      <h3 className="section-title">Transaction history</h3>
      {loading && (
        <div className="transaction-skeleton-list" role="status" aria-label="Loading transactions" aria-busy="true">
          <span /><span /><span />
        </div>
      )}
      {error && <p className="dashboard-error" role="alert">{error}</p>}
      {!loading && !error && transactions.length === 0 && (
        <p className="dashboard-message">No transactions yet.</p>
      )}
      {!loading && !error && transactions.length > 0 && (
        <div className="transaction-list">
          {transactions.map((transaction) => (
            <TransactionRow key={transaction.id} transaction={transaction} />
          ))}
        </div>
      )}
    </section>
  );
}
