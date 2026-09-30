import { Routes } from "@/constants/routes";

// "//x" and "/\x" start with "/" but browsers treat them as another host (open redirect).
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
