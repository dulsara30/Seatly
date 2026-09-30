import { NextResponse, type NextRequest } from "next/server";
import { RETURN_TO_PARAM, Routes } from "@/constants/routes";
import { AUTH_COOKIE_NAME } from "@/server/authCookie";

/**
 * Route guard for pages that need a session (Next 16's `proxy`, formerly
 * `middleware`). It runs on the server before the page renders, so a
 * logged-out visitor is redirected before any protected UI is sent - no flash
 * of a page they can't use.
 *
 * It only checks that the session cookie EXISTS. That's an optimistic check,
 * as Next's docs recommend for proxy: the cookie expires with the token, and
 * Spring verifies the token's signature on every API call - the real
 * authority. A forged cookie gets past this guard and then gets 401s.
 */
export function proxy(request: NextRequest) {
  if (request.cookies.has(AUTH_COOKIE_NAME)) {
    return NextResponse.next();
  }
  const signInUrl = new URL(Routes.signIn, request.url);
  signInUrl.searchParams.set(
    RETURN_TO_PARAM,
    `${request.nextUrl.pathname}${request.nextUrl.search}`,
  );
  return NextResponse.redirect(signInUrl);
}

// Must be literals - Next analyses the matcher at build time. These are
// Routes.dashboard and Routes.myRsvps, and everything under them.
export const config = {
  matcher: ["/dashboard/:path*", "/my-rsvps/:path*"],
};
