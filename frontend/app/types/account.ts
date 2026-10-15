export type BankAccount = {
  id: number;
  accountName: string;
  accountNumber: string;
  accountType: "CHECKING" | "SAVINGS";
  balance: number;
  status: "ACTIVE" | "INACTIVE";
};

export type CreateAccountInput = {
  accountName: string;
  accountType: BankAccount["accountType"];
  balance: number;
};

export type AccountSummary = Pick<BankAccount, "id" | "accountName" | "accountNumber">;

export type Transfer = {
  id: number;
  fromAccount: AccountSummary;
  toAccount: AccountSummary;
  amount: number;
  reference: string;
  createdAt: string;
};

export type BankTransaction = {
  id: number;
  type: "DEBIT" | "CREDIT";
  amount: number;
  balanceAfter: number;
  reference: string | null;
  createdAt: string;
  transfer: Transfer | null;
};
