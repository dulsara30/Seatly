"use client";

import { useRouter } from "next/navigation";
import { useCancelEvent } from "@/api/EventApi";
import { Button } from "@/components/atoms/Button";
import { Modal } from "@/components/molecules/Modal";
import { Copy } from "@/constants/copy";
import { Routes } from "@/constants/routes";
import { ToastTone, useToastStore } from "@/store/toastStore";
import type { Id } from "@/types/entities/primitives";

interface CancelEventDialogProps {
  eventId: Id;
  confirmedCount: number;
  open: boolean;
  onClose: () => void;
}

// Irreversible and it emails people, so "Keep event" gets the default focus.
export function CancelEventDialog({ eventId, confirmedCount, open, onClose }: CancelEventDialogProps) {
  const router = useRouter();
  const cancelEvent = useCancelEvent();
  const showToast = useToastStore((state) => state.show);

  const confirm = () =>
    cancelEvent.mutate(eventId, {
      onSuccess: () => {
        showToast(ToastTone.SUCCESS, Copy.toast.eventCancelled);
        onClose();
        router.push(Routes.dashboard);
      },
      onError: (error) => showToast(ToastTone.ERROR, error.message),
    });

  return (
    <Modal
      open={open}
      title={Copy.cancelModal.title}
      onClose={onClose}
      actions={
        <>
          <Button variant="secondary" autoFocus onClick={onClose}>
            {Copy.cancelModal.keep}
          </Button>
          <Button variant="danger" disabled={cancelEvent.isPending} onClick={confirm}>
            {Copy.cancelModal.confirm}
          </Button>
        </>
      }
    >
      <p>{Copy.cancelModal.body(confirmedCount)}</p>
    </Modal>
  );
}
