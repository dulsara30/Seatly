import type { Id } from "@/types/entities/primitives";

/** user/payload/UserSummaryDto.java — another person, as the public sees them. No email. */
export interface UserSummary {
  id: Id;
  name: string;
}

/** user/payload/UserResponseDto.java — your own account. */
export interface User {
  id: Id;
  name: string;
  email: string;
  bio: string | null;
}
