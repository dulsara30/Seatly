import { useEffect, useId, useRef, type ReactNode } from "react";

interface ModalProps {
  open: boolean;
  title: string;
  onClose: () => void;
  /** The action buttons, primary last. */
  actions: ReactNode;
  children: ReactNode;
}

/**
 * The native <dialog> in modal mode: the browser supplies the focus trap,
 * Escape to close and an inert page behind it - no library, nothing
 * hand-rolled to get subtly wrong.
 */
export function Modal({ open, title, onClose, actions, children }: ModalProps) {
  const dialogRef = useRef<HTMLDialogElement>(null);
  const titleId = useId();

  useEffect(() => {
    const dialog = dialogRef.current;
    if (!dialog) {
      return;
    }
    if (open && !dialog.open) {
      dialog.showModal();
    }
    if (!open && dialog.open) {
      dialog.close();
    }
  }, [open]);

  return (
    <dialog
      ref={dialogRef}
      onClose={onClose}
      aria-labelledby={titleId}
      className="m-auto w-full max-w-md rounded-lg border border-gray-200 bg-white p-6 backdrop:bg-black/40">
      <div className="flex flex-col gap-3">
        <h2 id={titleId} className="text-heading-m text-black">
          {title}
        </h2>
        <div className="text-body-m text-gray-500">{children}</div>
        <div className="mt-3 flex flex-col-reverse gap-2 sm:flex-row sm:justify-end">
          {actions}
        </div>
      </div>
    </dialog>
  );
}
