import { SignInForm } from "@/components/organisms/SignInForm";
import { AuthFooterLink, AuthLayout } from "@/components/templates/AuthLayout";
import { Copy } from "@/constants/copy";
import { RETURN_TO_PARAM, Routes, withReturnTo } from "@/constants/routes";
import { firstParam } from "@/utils/searchParams";

// returnTo is read on the server so the form needs no useSearchParams or Suspense.
export default async function SignInPage({
  searchParams,
}: PageProps<"/sign-in">) {
  const returnTo = firstParam((await searchParams)[RETURN_TO_PARAM]);
  const signUpHref = withReturnTo(Routes.signUp, returnTo);

  return (
    <AuthLayout
      title={Copy.auth.signInTitle}
      subtitle={Copy.auth.signInSubtitle}
      footer={
        <AuthFooterLink
          prompt={Copy.auth.noAccount}
          href={signUpHref}
          label={Copy.nav.signUp}
        />
      }>
      <SignInForm returnTo={returnTo} />
    </AuthLayout>
  );
}
