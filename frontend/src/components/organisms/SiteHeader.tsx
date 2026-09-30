"use client";

import { usePathname, useRouter } from "next/navigation";
import { useLogout, useSession } from "@/api/AuthApi";
import { HeaderBar } from "@/components/molecules/HeaderBar";
import { Routes } from "@/constants/routes";

export function SiteHeader() {
  const session = useSession();
  const signOut = useLogout();
  const router = useRouter();
  const pathname = usePathname();

  return (
    <HeaderBar
      // No session data or a failed check: "not known to be signed in" is signed out.
      user={session.data?.user ?? null}
      isLoading={session.isPending}
      currentPath={pathname}
      isSigningOut={signOut.isPending}
      onSignOut={() =>
        signOut.mutate(undefined, {
          onSuccess: () => router.replace(Routes.home),
        })
      }
    />
  );
}
