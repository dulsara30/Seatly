import { SignUpForm } from "@/components/organisms/SignUpForm";
import { AuthFooterLink, AuthLayout } from "@/components/templates/AuthLayout";
import { Copy } from "@/constants/copy";
import { RETURN_TO_PARAM, Routes, withReturnTo } from "@/constants/routes";
import { firstParam } from "@/utils/searchParams";

export default async function SignUpPage({ searchParams }: PageProps<"/sign-up">) {
  const returnTo = firstParam((await searchParams)[RETURN_TO_PARAM]);
  const signInHref = withReturnTo(Routes.signIn, returnTo);

  return (
    <AuthLayout
      title={Copy.auth.signUpTitle}
      subtitle={Copy.auth.signUpSubtitle}
      footer={<AuthFooterLink prompt={Copy.auth.haveAccount} href={signInHref} label={Copy.nav.signIn} />}
    >
      <SignUpForm returnTo={returnTo} />
    </AuthLayout>
  );
}
