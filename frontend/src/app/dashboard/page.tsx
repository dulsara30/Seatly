import { OrganiserDashboard } from "@/components/organisms/OrganiserDashboard";
import { PageContainer } from "@/components/templates/PageContainer";

// Protected by proxy.ts - without a session cookie this never renders.
export default function DashboardPage() {
  return (
    <PageContainer>
      <OrganiserDashboard />
    </PageContainer>
  );
}
