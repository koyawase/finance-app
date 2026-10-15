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

export type TransferInput = {
  toAccountNumber: string;
  amount: number;
  reference?: string;
};

export type TransferRequest = TransferInput & { fromAccountNumber: string };

export type AccountSummary = Pick<BankAccount, "id" | "accountName" | "accountNumber">;

export type Transfer = {
  id: number;
  fromAccount: AccountSummary;
  toAccount: AccountSummary;
  amount: number;
  reference: string | null;
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
