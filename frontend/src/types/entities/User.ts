import type { Id } from "@/types/entities/primitives";

// user/payload/UserSummaryDto.java - the public view, so no email.
export interface UserSummary {
  id: Id;
  name: string;
}

// user/payload/UserResponseDto.java
export interface User {
  id: Id;
  name: string;
  email: string;
  bio: string | null;
}
