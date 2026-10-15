"use client";

import { Formik } from "formik";
import { useEffect, useState } from "react";
import Button from "@/app/components/Button";
import { createAccountSchema } from "@/app/schemas/createAccountSchema";
import type { CreateAccountInput } from "@/app/types/account";

type CreateAccountModalProps = {
  onClose: () => void;
  onSubmit: (account: CreateAccountInput) => Promise<void>;
};

type CreateAccountFormValues = Omit<CreateAccountInput, "balance"> & { balance: string };

export default function CreateAccountModal({ onClose, onSubmit }: CreateAccountModalProps) {
  const [submitError, setSubmitError] = useState<string | null>(null);

  useEffect(() => {
    function handleKeyDown(event: KeyboardEvent) {
      if (event.key === "Escape") onClose();
    }

    window.addEventListener("keydown", handleKeyDown);
    return () => window.removeEventListener("keydown", handleKeyDown);
  }, [onClose]);

  return (
    <div className="modal-backdrop" onMouseDown={(event) => {
      if (event.target === event.currentTarget) onClose();
    }}>
      <section className="create-account-modal" role="dialog" aria-modal="true" aria-labelledby="create-account-title">
        <button className="modal-close" type="button" aria-label="Close" onClick={onClose}>×</button>
        <p className="eyebrow">New account</p>
        <h2 id="create-account-title">Open an account</h2>
        <p className="modal-intro">Choose the account details. We’ll create an account number for you.</p>

        <Formik<CreateAccountFormValues>
          initialValues={{ accountName: "", accountType: "CHECKING", balance: "" }}
          validationSchema={createAccountSchema}
          onSubmit={async (values) => {
            setSubmitError(null);
            try {
              await onSubmit({
                accountName: values.accountName.trim(),
                accountType: values.accountType,
                balance: Number(values.balance),
              });
              onClose();
            } catch (cause) {
              setSubmitError(cause instanceof Error ? cause.message : "Could not create account.");
            }
          }}
        >
          {({ values, errors, touched, handleChange, handleBlur, handleSubmit, isSubmitting }) => (
            <form onSubmit={handleSubmit} noValidate>
              <label htmlFor="accountName">Account name</label>
              <input
                id="accountName"
                name="accountName"
                autoComplete="off"
                autoFocus
                maxLength={255}
                placeholder="e.g. Holiday savings"
                value={values.accountName}
                onChange={handleChange}
                onBlur={handleBlur}
                aria-invalid={Boolean(touched.accountName && errors.accountName)}
                aria-describedby={touched.accountName && errors.accountName ? "account-name-error" : undefined}
              />
              {touched.accountName && errors.accountName && (
                <span id="account-name-error" className="field-error">{errors.accountName}</span>
              )}

              <label htmlFor="accountType">Account type</label>
              <select
                id="accountType"
                name="accountType"
                value={values.accountType}
                onChange={handleChange}
                onBlur={handleBlur}
                aria-invalid={Boolean(touched.accountType && errors.accountType)}
                aria-describedby={touched.accountType && errors.accountType ? "account-type-error" : undefined}
              >
                <option value="CHECKING">Checking</option>
                <option value="SAVINGS">Savings</option>
              </select>
              {touched.accountType && errors.accountType && (
                <span id="account-type-error" className="field-error">{errors.accountType}</span>
              )}

              <label htmlFor="balance">Opening balance</label>
              <input
                id="balance"
                name="balance"
                type="text"
                inputMode="decimal"
                placeholder="0.00"
                value={values.balance}
                onChange={handleChange}
                onBlur={handleBlur}
                aria-invalid={Boolean(touched.balance && errors.balance)}
                aria-describedby={touched.balance && errors.balance ? "balance-error" : "balance-help"}
              />
              {touched.balance && errors.balance ? (
                <span id="balance-error" className="field-error">{errors.balance}</span>
              ) : (
                <span id="balance-help" className="modal-help">Enter an amount with up to two decimal places.</span>
              )}

              {submitError && <p className="form-error" role="alert">{submitError}</p>}
              <div className="modal-actions">
                <Button variant="secondary" type="button" onClick={onClose}>Cancel</Button>
                <Button type="submit" disabled={isSubmitting}>
                  {isSubmitting ? "Creating…" : "Create account"}
                </Button>
              </div>
            </form>
          )}
        </Formik>
      </section>
    </div>
  );
}
