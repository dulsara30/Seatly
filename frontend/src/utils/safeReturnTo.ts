import { Routes } from "@/constants/routes";

/**
 * Where to go after login. returnTo arrives in the URL, so anyone can craft
 * it - only a path on THIS site is accepted. "//evil.example" and
 * "/\evil.example" both start with "/" but browsers treat them as another
 * host, so they're rejected too; otherwise login would be an open redirect.
 */
export function safeReturnTo(returnTo: string | undefined): string {
  if (
    !returnTo ||
    !returnTo.startsWith("/") ||
    returnTo.startsWith("//") ||
    returnTo.startsWith("/\\")
  ) {
    return Routes.home;
  }
  return returnTo;
}
