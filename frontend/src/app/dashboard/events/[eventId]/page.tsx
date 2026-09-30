import { notFound } from "next/navigation";
import { ManageEventView } from "@/components/organisms/ManageEventView";
import { PageContainer } from "@/components/templates/PageContainer";
import { parseId } from "@/utils/parseId";

export default async function ManageEventPage({ params }: PageProps<"/dashboard/events/[eventId]">) {
  const eventId = parseId((await params).eventId);
  if (eventId === null) {
    notFound();
  }
  return (
    <PageContainer>
      <ManageEventView eventId={eventId} />
    </PageContainer>
  );
}
