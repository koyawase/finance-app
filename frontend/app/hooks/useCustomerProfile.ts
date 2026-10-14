"use client";

import { useAuth0 } from "@auth0/auth0-react";
import { useEffect, useRef, useState } from "react";
import { getOrCreateCustomer, updateCustomerDetails } from "@/app/services/customerProfileService";
import type { Customer, CustomerDetails } from "@/app/types/customer";

export function useCustomerProfile() {
  const {
    isLoading: authLoading,
    isAuthenticated,
    error: authError,
    loginWithRedirect,
    logout,
    user,
    getAccessTokenSilently,
  } = useAuth0();
  const loginStarted = useRef(false);
  const [customer, setCustomer] = useState<Customer | null>(null);
  const [loading, setLoading] = useState(false);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!authLoading && !isAuthenticated && !authError && !loginStarted.current) {
      loginStarted.current = true;
      void loginWithRedirect();
    }
  }, [authError, authLoading, isAuthenticated, loginWithRedirect]);

  useEffect(() => {
    const email = user?.email;
    if (!isAuthenticated || !email) return;

    let cancelled = false;
    setLoading(true);
    setError(null);

    async function loadCustomer() {
      try {
        const token = await getAccessTokenSilently();
        const loadedCustomer = await getOrCreateCustomer(token, email);
        if (!cancelled) setCustomer(loadedCustomer);
      } catch (cause) {
        if (!cancelled) setError(cause instanceof Error ? cause.message : "Could not load customer");
      } finally {
        if (!cancelled) setLoading(false);
      }
    }

    void loadCustomer();

    return () => {
      cancelled = true;
    };
  }, [getAccessTokenSilently, isAuthenticated, user?.email]);

  async function saveDetails(details: CustomerDetails) {
    setSaving(true);
    setError(null);
    try {
      const token = await getAccessTokenSilently();
      const updatedCustomer = await updateCustomerDetails(token, {
        name: details.name.trim(),
        phoneNumber: details.phoneNumber.trim(),
      });
      setCustomer(updatedCustomer);
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : "Could not save your details");
    } finally {
      setSaving(false);
    }
  }

  function signOut() {
    void logout({ logoutParams: { returnTo: window.location.origin } });
  }

  return {
    authLoading,
    isAuthenticated,
    authError,
    customer,
    loading,
    saving,
    error,
    saveDetails,
    signOut,
  };
}
