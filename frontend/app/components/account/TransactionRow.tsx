import type { BankTransaction } from "@/app/types/account";
import { formatDateTime, formatMoney } from "@/app/components/account/formatters";

export default function TransactionRow({ transaction }: { transaction: BankTransaction }) {
  const isCredit = transaction.type === "CREDIT";
  const otherAccount = transaction.transfer && (isCredit
    ? transaction.transfer.fromAccount
    : transaction.transfer.toAccount);
  const description = transaction.reference || (isCredit ? "Money in" : "Money out");

  return (
    <article className="transaction-row">
      <span className={`transaction-icon ${isCredit ? "is-credit" : "is-debit"}`} aria-hidden="true">
        {isCredit ? "↓" : "↑"}
      </span>
      <div className="transaction-description">
        <p>
          {otherAccount ? (
            <>
              {isCredit ? "From" : "To"} {otherAccount.accountName}{" "}
              <span className="transaction-other-account-number">({otherAccount.accountNumber})</span>
            </>
          ) : description}
        </p>
        <time dateTime={transaction.createdAt}>{formatDateTime(transaction.createdAt)}</time>
      </div>
      <div className="transaction-amounts">
        <p className={isCredit ? "credit-amount" : ""}>
          {isCredit ? "+" : "−"}{formatMoney(transaction.amount)}
        </p>
        <span>Balance {formatMoney(transaction.balanceAfter)}</span>
      </div>
    </article>
  );
}
