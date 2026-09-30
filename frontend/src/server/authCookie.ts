import "server-only";
import type { NextResponse } from "next/server";

// httpOnly: no page script can read it, so XSS can't steal the token.
export const AUTH_COOKIE_NAME = "seatly_session";

const SECONDS_TO_MS = 1000;
const JWT_PAYLOAD_SEGMENT = 1;

// Expires at the token's exp; SameSite=Lax is the CSRF defence; Secure only in prod (dev is http).
export function setAuthCookie(
  response: NextResponse,
  accessToken: string,
): void {
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

// Decoding, not verifying: Spring checks the signature on every request.
function tokenExpiry(accessToken: string): Date {
  const payload = accessToken.split(".")[JWT_PAYLOAD_SEGMENT];
  if (payload === undefined) {
    throw new Error("Backend returned a malformed access token");
  }
  const { exp } = JSON.parse(
    Buffer.from(payload, "base64url").toString("utf8"),
  ) as { exp: number };
  return new Date(exp * SECONDS_TO_MS);
}
