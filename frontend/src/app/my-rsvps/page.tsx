import { MyRsvpsView } from "@/components/organisms/MyRsvpsView";
import { PageContainer } from "@/components/templates/PageContainer";

// Protected by proxy.ts - without a session cookie this never renders.
export default function MyRsvpsPage() {
  return (
    <PageContainer>
      <MyRsvpsView />
    </PageContainer>
  );
}
