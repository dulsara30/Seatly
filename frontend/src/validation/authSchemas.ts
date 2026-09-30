import * as yup from "yup";
import { UserFieldLimits } from "@/constants/fieldLimits";
import { MESSAGES } from "@/constants/messages";

const utf8 = new TextEncoder();

/**
 * Mirrors auth/payload/RegisterRequestDto.java rule for rule, with the same
 * message keys, so the form rejects exactly what Spring would:
 *
 *  - name, email: trimmed first (the backend's global trimmer), then required
 *    and length-capped - "   " counts as missing, as @NotBlank does.
 *  - password: NOT trimmed (the backend opts it out of the trimmer), so it is
 *    @NotEmpty, not @NotBlank - a password of spaces is still a password.
 *  - password min: characters, like @Size. Java and JS both count UTF-16
 *    code units, so the two agree on every string.
 *  - password max: 72 BYTES in UTF-8 - BCrypt's limit - checked by the service,
 *    not by @Size. "日" is one character but three bytes, so 30 of them pass a
 *    character count and fail BCrypt. TextEncoder measures the real bytes.
 */
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

/**
 * Mirrors LoginRequestDto: present, nothing more. Login deliberately checks
 * no format or length - a failed attempt shouldn't hint at the password rules.
 */
export const signInSchema = yup.object({
  email: yup.string().trim().required(MESSAGES.AUTH_ERROR_EMAIL_REQUIRED),
  password: yup.string().required(MESSAGES.AUTH_ERROR_PASSWORD_REQUIRED),
});

export type SignUpFormValues = yup.InferType<typeof signUpSchema>;
export type SignInFormValues = yup.InferType<typeof signInSchema>;
