"use client";

import { useFormik } from "formik";
import { useRouter } from "next/navigation";
import { useLogin } from "@/api/AuthApi";
import { Button } from "@/components/atoms/Button";
import { Input } from "@/components/atoms/Input";
import { FormStatus } from "@/components/molecules/FormStatus";
import { Copy } from "@/constants/copy";
import { showSubmitError } from "@/utils/formErrors";
import { safeReturnTo } from "@/utils/safeReturnTo";
import { signInSchema, type SignInFormValues } from "@/validation/authSchemas";

const INITIAL_VALUES: SignInFormValues = { email: "", password: "" };

// Goes via the Next route handler, which sets the httpOnly cookie; the token never reaches here.
export function SignInForm({ returnTo }: { returnTo: string | undefined }) {
  const router = useRouter();
  const login = useLogin();

  const formik = useFormik<SignInFormValues>({
    initialValues: INITIAL_VALUES,
    validationSchema: signInSchema,
    onSubmit: async (values, helpers) => {
      helpers.setStatus(undefined);
      try {
        await login.mutateAsync(values);
        router.replace(safeReturnTo(returnTo));
      } catch (error) {
        showSubmitError(error, helpers);
      }
    },
  });

  const fieldError = (field: keyof SignInFormValues) =>
    formik.touched[field] ? formik.errors[field] : undefined;

  return (
    <form noValidate onSubmit={formik.handleSubmit} className="flex flex-col gap-4">
      <FormStatus message={formik.status} />
      <Input
        label={Copy.auth.email}
        type="email"
        autoComplete="email"
        placeholder={Copy.auth.emailPlaceholder}
        error={fieldError("email")}
        {...formik.getFieldProps("email")}
      />
      <Input
        label={Copy.auth.password}
        type="password"
        autoComplete="current-password"
        error={fieldError("password")}
        {...formik.getFieldProps("password")}
      />
      <Button type="submit" variant="accent" fullWidth disabled={formik.isSubmitting}>
        {Copy.auth.signInAction}
      </Button>
    </form>
  );
}
