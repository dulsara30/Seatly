import type { NextConfig } from "next";
import { RouteAliases, Routes } from "./src/constants/routes";

const nextConfig: NextConfig = {
  // Common alternative spellings of the auth pages redirect to the one real
  // URL. Permanent (308), and Next passes the query string through, so
  // /login?returnTo=... keeps its returnTo.
  async redirects() {
    return [
      ...RouteAliases.signIn.map((source) => ({ source, destination: Routes.signIn, permanent: true })),
      ...RouteAliases.signUp.map((source) => ({ source, destination: Routes.signUp, permanent: true })),
    ];
  },
};

export default nextConfig;
