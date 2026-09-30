"use client";

import { useFormik } from "formik";
import { useRouter } from "next/navigation";
import { useLogin, useRegister } from "@/api/AuthApi";
import { Button } from "@/components/atoms/Button";
import { Input } from "@/components/atoms/Input";
import { FormStatus } from "@/components/molecules/FormStatus";
import { Copy } from "@/constants/copy";
import { showSubmitError } from "@/utils/formErrors";
import { safeReturnTo } from "@/utils/safeReturnTo";
import { signUpSchema, type SignUpFormValues } from "@/validation/authSchemas";

const INITIAL_VALUES: SignUpFormValues = {
  name: "",
  email: "",
  password: "",
  bio: "",
};

// Registration returns no token, so the new account logs straight in with the same credentials.
export function SignUpForm({ returnTo }: { returnTo: string | undefined }) {
  const router = useRouter();
  const register = useRegister();
  const login = useLogin();

  const formik = useFormik<SignUpFormValues>({
    initialValues: INITIAL_VALUES,
    validationSchema: signUpSchema,
    onSubmit: async (values, helpers) => {
      helpers.setStatus(undefined);
      try {
        // An empty bio is "no bio": the backend field is nullable, not blank.
        await register.mutateAsync({
          ...values,
          bio: values.bio ? values.bio : null,
        });
        await login.mutateAsync({
          email: values.email,
          password: values.password,
        });
        router.replace(safeReturnTo(returnTo));
      } catch (error) {
        showSubmitError(error, helpers);
      }
    },
  });

  const fieldError = (field: keyof SignUpFormValues) =>
    formik.touched[field] ? formik.errors[field] : undefined;

  return (
    <form
      noValidate
      onSubmit={formik.handleSubmit}
      className="flex flex-col gap-4">
      <FormStatus message={formik.status} />
      <Input
        label={Copy.auth.name}
        autoComplete="name"
        placeholder={Copy.auth.namePlaceholder}
        error={fieldError("name")}
        {...formik.getFieldProps("name")}
      />
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
        autoComplete="new-password"
        hint={Copy.auth.passwordHint}
        error={fieldError("password")}
        {...formik.getFieldProps("password")}
      />
      <Input
        label={Copy.auth.bio}
        multiline
        placeholder={Copy.auth.bioPlaceholder}
        error={fieldError("bio")}
        {...formik.getFieldProps("bio")}
      />
      <Button
        type="submit"
        variant="accent"
        fullWidth
        disabled={formik.isSubmitting}>
        {Copy.auth.signUpAction}
      </Button>
    </form>
  );
}
