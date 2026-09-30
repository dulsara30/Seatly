import * as yup from "yup";
import { UserFieldLimits } from "@/constants/fieldLimits";
import { MESSAGES } from "@/constants/messages";

const utf8 = new TextEncoder();

// Mirrors auth/payload/RegisterRequestDto.java rule for rule, with the same message keys.
export const signUpSchema = yup.object({
  name: yup
    .string()
    .trim()
    .required(MESSAGES.AUTH_ERROR_NAME_REQUIRED)
    .max(UserFieldLimits.NAME_MAX_LENGTH, MESSAGES.AUTH_ERROR_NAME_TOO_LONG),
  email: yup
    .string()
    .trim()
    .required(MESSAGES.AUTH_ERROR_EMAIL_REQUIRED)
    .email(MESSAGES.AUTH_ERROR_EMAIL_INVALID)
    .max(UserFieldLimits.EMAIL_MAX_LENGTH, MESSAGES.AUTH_ERROR_EMAIL_TOO_LONG),
  // Not trimmed (the backend skips it); max is 72 UTF-8 BYTES for BCrypt, so use TextEncoder.
  password: yup
    .string()
    .required(MESSAGES.AUTH_ERROR_PASSWORD_REQUIRED)
    .min(
      UserFieldLimits.PASSWORD_MIN_LENGTH,
      MESSAGES.AUTH_ERROR_PASSWORD_TOO_SHORT,
    )
    .test(
      "fits-bcrypt",
      MESSAGES.AUTH_ERROR_PASSWORD_TOO_LONG,
      (password) =>
        password === undefined ||
        utf8.encode(password).length <= UserFieldLimits.PASSWORD_MAX_BYTES,
    ),
  bio: yup
    .string()
    .trim()
    .max(UserFieldLimits.BIO_MAX_LENGTH, MESSAGES.AUTH_ERROR_BIO_TOO_LONG),
});

// Presence only, so a failed login doesn't hint at the password rules.
export const signInSchema = yup.object({
  email: yup.string().trim().required(MESSAGES.AUTH_ERROR_EMAIL_REQUIRED),
  password: yup.string().required(MESSAGES.AUTH_ERROR_PASSWORD_REQUIRED),
});

export type SignUpFormValues = yup.InferType<typeof signUpSchema>;
export type SignInFormValues = yup.InferType<typeof signInSchema>;
