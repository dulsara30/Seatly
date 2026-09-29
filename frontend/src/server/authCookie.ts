import "server-only";
import type { NextResponse } from "next/server";

/** The session cookie. httpOnly: no script in the page can read it, so XSS can't steal it. */
export const AUTH_COOKIE_NAME = "seatly_session";

const SECONDS_TO_MS = 1000;
const JWT_PAYLOAD_SEGMENT = 1;

/**
 * The cookie expires when the token does, read from the token's own `exp`
 * claim — so the backend's TTL (seatly.jwt.access-token-ttl) stays the
 * single source of truth instead of being copied here.
 *
 * SameSite=Lax is the CSRF defence: the browser won't attach this cookie to
 * a POST/PATCH/DELETE started by another site. The proxy route adds an
 * Origin check on top. Secure only in production, because local dev is http.
 */
export function setAuthCookie(response: NextResponse, accessToken: string): void {
  response.cookies.set({
    name: AUTH_COOKIE_NAME,
    value: accessToken,
    httpOnly: true,
    secure: process.env.NODE_ENV === "production",
    sameSite: "lax",
    path: "/",
    expires: tokenExpiry(accessToken),
  });
}

export function clearAuthCookie(response: NextResponse): void {
  response.cookies.delete(AUTH_COOKIE_NAME);
}

// Decoding, not verifying: this only reads when the cookie should expire.
// Spring verifies the signature on every request — that is the real check.
function tokenExpiry(accessToken: string): Date {
  const payload = accessToken.split(".")[JWT_PAYLOAD_SEGMENT];
  if (payload === undefined) {
    throw new Error("Backend returned a malformed access token");
  }
  const { exp } = JSON.parse(Buffer.from(payload, "base64url").toString("utf8")) as { exp: number };
  return new Date(exp * SECONDS_TO_MS);
}
