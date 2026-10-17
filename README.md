# Finance App

A demo banking app built with a Next.js frontend and a Spring Boot API. Users can sign in, create accounts with a demo opening balance, view transactions, and transfer money between accounts.

## Requirements

- Node.js 20.9.0 or newer and npm
- Java 17
- Internet access for Auth0 sign-in

## Run locally

Start the backend in one terminal from the repository root:

```bash
cd backend
./gradlew bootRun
```

The API runs at `http://localhost:8080`. The Gradle wrapper downloads the required Gradle version if needed.

Start the frontend in another terminal from the repository root:

```bash
cd frontend
npm ci
npm run dev
```

Open `http://localhost:3000` in a browser. Keep the frontend on **`localhost:3000`**: the Auth0 application is configured to allow that origin as a callback and logout URL, and the frontend sends `window.location.origin` as its redirect URI. The backend also allows this origin through CORS. If port 3000 is occupied, free it before starting the frontend; Next.js may otherwise switch to another port that Auth0 does not allow. The frontend calls the API at `http://localhost:8080`.

## Demo sign-in

You can use either Auth0 demo account:

| Email | Password |
| --- | --- |
| `messifinance@mailinator.com` | `waseemtest12345!` |
| `cristianofinance@mailinator.com` | `waseemtest12345!` |

You can also create your own user through the Auth0 sign-in page. After sign-in, the frontend creates a customer record for a new Auth0 user and prompts for their name and phone number. New users can then create an account and choose a starting balance so they have funds to try transfers.

The backend uses an **in-memory H2 database**. On every backend start, Hibernate recreates the schema and [`data.sql`](backend/src/main/resources/data.sql) loads two customers, four accounts, transfers, and transaction history. Any accounts, transfers, or profile changes made while trying the app disappear when the backend restarts. Seeded customers are linked to the demo Auth0 users by the token's `sub` claim; their stored contact email addresses are placeholders and may differ from the login emails above.

## Authentication and authorization

Auth0 is the identity provider. It handles sign-up, sign-in, and password management; the backend stores no user passwords. The frontend uses the Auth0 React SDK to get an access token for this API and sends it as a bearer token. The backend validates the JWT signature, expiry, issuer, and API audience, then uses its `sub` claim to find the matching customer. The API requires authentication for every endpoint except CORS preflight requests. Account listings and transaction history are limited to the authenticated customer, and a transfer must originate from one of their accounts.

The current demo **does not enforce API scopes or roles**. The frontend requests the OpenID Connect scopes `openid email`; these provide identity information and are not endpoint permissions. Any user with a valid token for this API can call the authenticated endpoints, subject to the account ownership checks above.

For finer authorization, API permissions such as `transfers:create` could be defined in Auth0 and assigned to roles. New users would need a default role assigned during sign-up or provisioning. The frontend would request the API scope, and Auth0 would grant it according to its authorization settings. Spring Security normally maps a granted `scope` or `scp` value to an authority prefixed with `SCOPE_`. For example, the transfer route could be restricted in [`SecurityConfig`](backend/src/main/java/com/example/backend/security/SecurityConfig.java):

```java
.requestMatchers(HttpMethod.POST, "/transfers")
    .hasAuthority("SCOPE_transfers:create")
.anyRequest().authenticated()
```

Alternatively, after enabling method security with `@EnableMethodSecurity`, the transfer method could use `@PreAuthorize("hasAuthority('SCOPE_transfers:create')")`. If authorization uses Auth0's separate `permissions` claim instead of `scope`/`scp`, a JWT authority converter would be needed to map that claim for Spring Security. These are possible extensions; neither restriction is active in the current demo.

## API and tests

The endpoint contract is in [`backend/api-spec.yaml`](backend/api-spec.yaml). To run backend unit and integration tests:

```bash
cd backend
./gradlew test
```

The integration tests use H2 and test JWTs; they do not require an Auth0 account.
