import type { ButtonHTMLAttributes, ReactNode } from "react";

type ButtonProps = ButtonHTMLAttributes<HTMLButtonElement> & {
  variant?: "primary" | "secondary";
  children: ReactNode;
};

export default function Button({ variant = "primary", className = "", children, ...props }: ButtonProps) {
  return (
    <button className={`${variant === "primary" ? "primary-button" : "logout-button"} ${className}`.trim()} {...props}>
      {children}
    </button>
  );
}
