import { LinkButton } from "@/components/atoms/Button";
import { EmptyState } from "@/components/molecules/EmptyState";
import { PageContainer } from "@/components/templates/PageContainer";
import { Copy } from "@/constants/copy";
import { Routes } from "@/constants/routes";

// Replaces Next's built-in 404, which follows the OS dark mode and ignores the
// design system. This one sits inside the normal header, in our tokens.
export default function NotFound() {
  return (
    <PageContainer>
      <EmptyState
        title={Copy.notFound.title}
        body={Copy.notFound.body}
        action={
          <LinkButton href={Routes.home} variant="primary" size="small">
            {Copy.notFound.backHome}
          </LinkButton>
        }
      />
    </PageContainer>
  );
}
