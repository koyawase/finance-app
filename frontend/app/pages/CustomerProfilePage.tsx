"use client";

import CustomerProfileForm from "@/app/components/CustomerProfileForm";
import BankingDashboard from "@/app/components/BankingDashboard";
import { useCustomerProfile } from "@/app/hooks/useCustomerProfile";

export default function CustomerProfilePage() {
  const customerProfile = useCustomerProfile();

  if (customerProfile.authLoading) return null;
  if (!customerProfile.isAuthenticated) {
    return customerProfile.authError ? (
      <main className="customer-profile-screen">
        <section className="profile-card profile-message" role="alert">
          <p className="eyebrow">Sign in</p>
          <h1>We couldn’t sign you in</h1>
          <p>{customerProfile.authError.message}</p>
        </section>
      </main>
    ) : null;
  }

  if (customerProfile.customer?.name?.trim()) {
    return <BankingDashboard customer={customerProfile.customer} onSignOut={customerProfile.signOut} />;
  }

  return (
    <main className="customer-profile-screen">
      {customerProfile.customer && !customerProfile.customer.name?.trim() && (
        <CustomerProfileForm
          phoneNumber={customerProfile.customer.phoneNumber}
          error={customerProfile.error}
          saving={customerProfile.saving}
          onSubmit={customerProfile.saveDetails}
          onSignOut={customerProfile.signOut}
        />
      )}
      {customerProfile.error && !customerProfile.customer && (
        <section className="profile-card profile-message" role="alert">
          <p className="eyebrow">Profile</p>
          <h1>We couldn’t load your profile</h1>
          <p>{customerProfile.error}</p>
        </section>
      )}
    </main>
  );
}

