import * as yup from "yup";

export const createAccountSchema = yup.object({
  accountName: yup
    .string()
    .trim()
    .max(255, "Account name must be 255 characters or fewer.")
    .required("Enter an account name."),
  accountType: yup
    .mixed<"CHECKING" | "SAVINGS">()
    .oneOf(["CHECKING", "SAVINGS"], "Choose an account type.")
    .required("Choose an account type."),
  balance: yup
    .number()
    .transform((value, originalValue) => originalValue === "" ? undefined : value)
    .typeError("Enter a valid opening balance.")
    .required("Enter an opening balance.")
    .min(0, "Balance must be zero or more.")
    .test(
      "balance-precision",
      "Use up to 17 whole digits and 2 decimal places.",
      (_value, context) => {
        const originalValue = String(context.originalValue ?? "");
        return originalValue === "" || /^\d{1,17}(?:\.\d{1,2})?$/.test(originalValue);
      },
    ),
});
