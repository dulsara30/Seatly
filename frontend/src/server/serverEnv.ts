import "server-only";

// Not NEXT_PUBLIC_: the browser never calls Spring. Read per call so builds don't need it.
export function getApiBaseUrl(): string {
  const apiBaseUrl = process.env.API_BASE_URL;
  if (!apiBaseUrl) {
    throw new Error(
      "API_BASE_URL is not set. Copy frontend/.env.example to .env.local.",
    );
  }
  return apiBaseUrl;
}
