import type { BankAccount } from "@/app/types/account";
import { accountTypeLabel, formatMoney } from "@/app/components/account/formatters";

export default function AccountCard({ account, onClick }: { account: BankAccount; onClick: () => void }) {
  return (
    <button className="account-card" onClick={onClick}>
      <span className="account-card-top">
        <span className="account-icon" aria-hidden="true">{account.accountType === "SAVINGS" ? "◈" : "◉"}</span>
        <span className={`account-status ${account.status === "ACTIVE" ? "is-active" : ""}`}>
          {account.status === "ACTIVE" ? "Active" : "Inactive"}
        </span>
      </span>
      <span className="account-type">{accountTypeLabel(account.accountType)}</span>
      <span className="account-name">{account.accountName}</span>
      <span className="account-number">{account.accountNumber}</span>
      <span className="account-balance-label">Available balance</span>
      <span className="account-balance">{formatMoney(account.balance)}</span>
      <span className="account-card-link">View transactions <span aria-hidden="true">→</span></span>
    </button>
  );
}
