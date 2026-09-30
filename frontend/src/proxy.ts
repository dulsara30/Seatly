import { NextResponse, type NextRequest } from "next/server";
import { RETURN_TO_PARAM, Routes } from "@/constants/routes";
import { AUTH_COOKIE_NAME } from "@/server/authCookie";

// Optimistic cookie-exists check only; Spring verifies the token on every API call.
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

// Must be literals (Next reads the matcher at build time): Routes.dashboard and Routes.myRsvps.
export const config = {
  matcher: ["/dashboard/:path*", "/my-rsvps/:path*"],
};
