import * as yup from "yup";

export function createTransferSchema(fromAccountNumber: string, availableBalance: number) {
  return yup.object({
    toAccountNumber: yup
      .string()
      .trim()
      .required("Enter the recipient account number.")
      .notOneOf([fromAccountNumber], "Choose a different account."),
    amount: yup
      .number()
      .transform((value, originalValue) => originalValue === "" ? undefined : value)
      .typeError("Enter a valid amount.")
      .required("Enter an amount.")
      .min(0.01, "Transfer at least $0.01.")
      .max(availableBalance, "You can't transfer more than your available balance.")
      .test(
        "amount-precision",
        "Use up to two decimal places.",
        (_value, context) => {
          const originalValue = String(context.originalValue ?? "");
          return originalValue === "" || /^\d+(?:\.\d{1,2})?$/.test(originalValue);
        },
      ),
    reference: yup
      .string()
      .trim()
      .max(20, "Reference must be 20 characters or fewer.")
      .optional(),
  });
}
