import type { Metadata } from "next";
import { Inter } from "next/font/google";
import { Providers } from "@/app/Providers";
import { AppShell } from "@/components/templates/AppShell";
import "./globals.css";

const inter = Inter({
  variable: "--font-inter",
  subsets: ["latin"],
});

export const metadata: Metadata = {
  title: "Seatly",
  description:
    "RSVP to events with limited seats - and never lose your place in line.",
};

export default function RootLayout({ children }: LayoutProps<"/">) {
  return (
    <html lang="en" className={inter.variable}>
      <body className="flex min-h-dvh flex-col">
        <Providers>
          <AppShell>{children}</AppShell>
        </Providers>
      </body>
    </html>
  );
}
