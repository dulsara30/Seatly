import type { ReactNode } from "react";
import { SiteHeader } from "@/components/organisms/SiteHeader";
import { ToastViewport } from "@/components/organisms/ToastViewport";

export function AppShell({ children }: { children: ReactNode }) {
  return (
    <>
      <SiteHeader />
      <main className="flex-1">{children}</main>
      <ToastViewport />
    </>
  );
}
