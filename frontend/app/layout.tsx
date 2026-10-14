import type { Metadata } from "next";
import { Geist, Geist_Mono } from "next/font/google";
import Auth0Provider from "./providers";
import "./globals.css";

const geistSans = Geist({
  variable: "--font-geist-sans",
  subsets: ["latin"],
});

const geistMono = Geist_Mono({
  variable: "--font-geist-mono",
  subsets: ["latin"],
});

export const metadata: Metadata = {
  title: "BigMoney",
  description: "How to make big money: log in, look confident, and hope for the best.",
};

export default function RootLayout({ children }: LayoutProps<"/">) {
  return (
    <html
      lang="en"
      className={`${geistSans.variable} ${geistMono.variable} h-full antialiased`}
    >
      <body className="min-h-full flex flex-col">
        <Auth0Provider>{children}</Auth0Provider>
      </body>
    </html>
  );
}
