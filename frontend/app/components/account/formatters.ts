import type { BankAccount } from "@/app/types/account";

const money = new Intl.NumberFormat("en-NZ", { style: "currency", currency: "NZD" });
const dateTime = new Intl.DateTimeFormat("en-NZ", {
  day: "numeric",
  month: "short",
  year: "numeric",
  hour: "numeric",
  minute: "2-digit",
});

export function formatMoney(amount: number) {
  return money.format(amount);
}

export function formatDateTime(value: string) {
  return dateTime.format(new Date(value));
}

export function accountTypeLabel(type: BankAccount["accountType"]) {
  return type === "SAVINGS" ? "Savings" : "Checking";
}
