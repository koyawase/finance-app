"use client";

import { Formik } from "formik";
import { useEffect, useRef, useState } from "react";
import Button from "@/app/components/Button";
import { createTransferSchema } from "@/app/schemas/transferSchema";
import { formatMoney } from "@/app/components/account/formatters";
import type { BankAccount, TransferInput } from "@/app/types/account";

type TransferModalProps = {
  account: BankAccount;
  onClose: () => void;
  onSubmit: (transfer: TransferInput) => Promise<void>;
};

type TransferFormValues = {
  toAccountNumber: string;
  amount: string;
  reference: string;
};

export default function TransferModal({ account, onClose, onSubmit }: TransferModalProps) {
  const [submitError, setSubmitError] = useState<string | null>(null);
  const submittingRef = useRef(false);

  useEffect(() => {
    function handleKeyDown(event: KeyboardEvent) {
      if (event.key === "Escape" && !submittingRef.current) onClose();
    }

    window.addEventListener("keydown", handleKeyDown);
    return () => window.removeEventListener("keydown", handleKeyDown);
  }, [onClose]);

  return (
    <div
      className="modal-backdrop"
      onMouseDown={(event) => {
        if (event.target === event.currentTarget && !submittingRef.current) onClose();
      }}
    >
      <section className="create-account-modal" role="dialog" aria-modal="true" aria-labelledby="transfer-title">
        <button
          className="modal-close"
          type="button"
          aria-label="Close"
          onClick={() => { if (!submittingRef.current) onClose(); }}
        >×</button>
        <p className="eyebrow">Transfer money</p>
        <h2 id="transfer-title">Send from {account.accountName}</h2>
        <p className="modal-intro">
          Available balance: {formatMoney(account.balance)} · Account {account.accountNumber}
        </p>

        <Formik<TransferFormValues>
          initialValues={{ toAccountNumber: "", amount: "", reference: "" }}
          validationSchema={createTransferSchema(account.accountNumber, account.balance)}
          onSubmit={async (values) => {
            setSubmitError(null);
            try {
              await onSubmit({
                toAccountNumber: values.toAccountNumber.trim(),
                amount: Number(values.amount),
                reference: values.reference.trim() || undefined,
              });
              onClose();
            } catch (cause) {
              setSubmitError(cause instanceof Error ? cause.message : "Could not send transfer.");
            }
          }}
        >
          {({ values, errors, touched, handleChange, handleBlur, handleSubmit, isSubmitting }) => {
            submittingRef.current = isSubmitting;

            return (
              <form onSubmit={handleSubmit} noValidate>
                <label htmlFor="toAccountNumber">Recipient account number</label>
                <input
                  id="toAccountNumber"
                  name="toAccountNumber"
                  autoComplete="off"
                  autoFocus
                  value={values.toAccountNumber}
                  onChange={handleChange}
                  onBlur={handleBlur}
                  aria-invalid={Boolean(touched.toAccountNumber && errors.toAccountNumber)}
                  aria-describedby={touched.toAccountNumber && errors.toAccountNumber ? "recipient-error" : undefined}
                />
                {touched.toAccountNumber && errors.toAccountNumber && (
                  <span id="recipient-error" className="field-error">{errors.toAccountNumber}</span>
                )}

                <label htmlFor="amount">Amount</label>
                <input
                  id="amount"
                  name="amount"
                  type="text"
                  inputMode="decimal"
                  placeholder="0.00"
                  value={values.amount}
                  onChange={handleChange}
                  onBlur={handleBlur}
                  aria-invalid={Boolean(touched.amount && errors.amount)}
                  aria-describedby={touched.amount && errors.amount ? "amount-error" : "amount-help"}
                />
                {touched.amount && errors.amount ? (
                  <span id="amount-error" className="field-error">{errors.amount}</span>
                ) : (
                  <span id="amount-help" className="modal-help">Transfer up to your available balance.</span>
                )}

                <label htmlFor="reference">Reference <span className="optional-label">(optional)</span></label>
                <input
                  id="reference"
                  name="reference"
                  autoComplete="off"
                  maxLength={20}
                  value={values.reference}
                  onChange={handleChange}
                  onBlur={handleBlur}
                  aria-invalid={Boolean(touched.reference && errors.reference)}
                  aria-describedby={touched.reference && errors.reference ? "reference-error" : undefined}
                />
                {touched.reference && errors.reference && (
                  <span id="reference-error" className="field-error">{errors.reference}</span>
                )}

                {submitError && <p className="form-error" role="alert">{submitError}</p>}
                <div className="modal-actions">
                  <Button variant="secondary" type="button" disabled={isSubmitting} onClick={onClose}>Cancel</Button>
                  <Button type="submit" disabled={isSubmitting}>
                    {isSubmitting ? "Sending transfer…" : "Send transfer"}
                  </Button>
                </div>
              </form>
            );
          }}
        </Formik>
      </section>
    </div>
  );
}
