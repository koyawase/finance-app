export default function AccountSkeleton() {
  return (
    <div className="account-skeleton" aria-hidden="true">
      <span className="skeleton-short" />
      <span className="skeleton-medium" />
      <span className="skeleton-long" />
      <span className="skeleton-balance" />
    </div>
  );
}
