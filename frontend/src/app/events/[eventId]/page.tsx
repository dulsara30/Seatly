import { notFound } from "next/navigation";
import { EventDetailView } from "@/components/organisms/EventDetailView";
import { PageContainer } from "@/components/templates/PageContainer";
import { parseId } from "@/utils/parseId";

export default async function EventPage({ params }: PageProps<"/events/[eventId]">) {
  const eventId = parseId((await params).eventId);
  if (eventId === null) {
    notFound();
  }
  return (
    <PageContainer>
      <EventDetailView eventId={eventId} />
    </PageContainer>
  );
}
