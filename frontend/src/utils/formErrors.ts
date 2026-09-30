import type { FormikHelpers } from "formik";
import { ApiError } from "@/api/utils/ApiError";
import { MESSAGES } from "@/constants/messages";

export function showSubmitError<Values>(
  error: unknown,
  helpers: Pick<FormikHelpers<Values>, "setErrors" | "setStatus">,
) {
  if (!(error instanceof ApiError)) {
    helpers.setStatus(MESSAGES.UNKNOWN_ERROR);
    return;
  }
  const fieldErrors = Object.fromEntries(
    error.details
      .filter((detail) => detail.field !== null)
      .map((detail) => [detail.field, detail.message]),
  );
  helpers.setErrors(fieldErrors);
  const formLevel = error.details.filter((detail) => detail.field === null);
  helpers.setStatus(
    formLevel.length > 0
      ? formLevel.map((detail) => detail.message).join(" ")
      : undefined,
  );
}
