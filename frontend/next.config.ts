import type { NextConfig } from "next";
import { RouteAliases, Routes } from "./src/constants/routes";

const nextConfig: NextConfig = {
  // Permanent (308); Next passes the query string through, so /login?returnTo=... keeps it.
  async redirects() {
    return [
      ...RouteAliases.signIn.map((source) => ({ source, destination: Routes.signIn, permanent: true })),
      ...RouteAliases.signUp.map((source) => ({ source, destination: Routes.signUp, permanent: true })),
    ];
  },
};

export default nextConfig;
