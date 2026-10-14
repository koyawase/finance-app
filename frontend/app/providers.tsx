"use client";

import { Auth0Provider as Provider } from "@auth0/auth0-react";
import { useSyncExternalStore, type ReactNode } from "react";

const subscribe = () => () => {};
const getOrigin = () => window.location.origin;
const getServerOrigin = () => null;

export default function Auth0Provider({ children }: { children: ReactNode }) {
  const redirectUri = useSyncExternalStore(subscribe, getOrigin, getServerOrigin);

  if (!redirectUri) return null;

  return (
    <Provider
      domain="dev-aqy6ixj4yun11ld1.us.auth0.com"
      clientId="4LCMKFgl8zeN8O54wAGRg47ddjmK7hPw"
      authorizationParams={{
        redirect_uri: redirectUri,
        audience: "https://waseem.api.finance-app",
        scope: "openid profile email",
      }}
    >
      {children}
    </Provider>
  );
}
