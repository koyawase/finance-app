"use client";

import { Formik } from "formik";
import Button from "@/app/components/Button";
import { customerProfileSchema } from "@/app/schemas/customerProfileSchema";
import type { CustomerDetails } from "@/app/types/customer";

type CustomerProfileFormProps = {
  phoneNumber: string | null;
  error: string | null;
  saving: boolean;
  onSubmit: (details: CustomerDetails) => Promise<void>;
  onSignOut: () => void;
};

export default function CustomerProfileForm({ phoneNumber, error, saving, onSubmit, onSignOut }: CustomerProfileFormProps) {
  return (
    <section className="profile-card">
      <p className="eyebrow">One last step</p>
      <h1>Complete your profile</h1>
      <p className="profile-intro">Add your name and phone number to finish setting up your customer profile.</p>
      <Formik
        initialValues={{ name: "", phoneNumber: phoneNumber ?? "" }}
        validationSchema={customerProfileSchema}
        onSubmit={onSubmit}
      >
        {({ values, errors, touched, handleChange, handleBlur, handleSubmit, isSubmitting }) => (
          <form onSubmit={handleSubmit} noValidate>
            <label htmlFor="name">Full name</label>
            <input id="name" name="name" autoComplete="name" value={values.name} onChange={handleChange} onBlur={handleBlur} aria-invalid={Boolean(touched.name && errors.name)} aria-describedby={touched.name && errors.name ? "name-error" : undefined} />
            {touched.name && errors.name && <span id="name-error" className="field-error">{errors.name}</span>}

            <label htmlFor="phoneNumber">Phone number</label>
            <input id="phoneNumber" name="phoneNumber" type="tel" autoComplete="tel" value={values.phoneNumber} onChange={handleChange} onBlur={handleBlur} aria-invalid={Boolean(touched.phoneNumber && errors.phoneNumber)} aria-describedby={touched.phoneNumber && errors.phoneNumber ? "phone-error" : undefined} />
            {touched.phoneNumber && errors.phoneNumber && <span id="phone-error" className="field-error">{errors.phoneNumber}</span>}

            {error && <p className="form-error" role="alert">{error}</p>}
            <Button type="submit" disabled={isSubmitting || saving}>{saving ? "Saving…" : "Save and continue"}</Button>
          </form>
        )}
      </Formik>
      <Button variant="secondary" onClick={onSignOut}>Log out</Button>
    </section>
  );
}
