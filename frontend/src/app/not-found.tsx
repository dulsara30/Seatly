import { LinkButton } from "@/components/atoms/Button";
import { EmptyState } from "@/components/molecules/EmptyState";
import { PageContainer } from "@/components/templates/PageContainer";
import { Copy } from "@/constants/copy";
import { Routes } from "@/constants/routes";

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
