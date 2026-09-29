/** auth/payload/LoginRequestDto.java */
export interface LoginRequest {
  email: string;
  password: string;
}

/** auth/payload/RegisterRequestDto.java */
export interface RegisterRequest {
  name: string;
  email: string;
  password: string;
  bio: string | null;
}
