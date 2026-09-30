"use client";

import { Toast } from "@/components/molecules/Toast";
import { useToastStore } from "@/store/toastStore";

export function ToastViewport() {
  const toasts = useToastStore((state) => state.toasts);
  const dismiss = useToastStore((state) => state.dismiss);

  return (
    <div className="pointer-events-none fixed inset-x-4 bottom-4 z-20 flex flex-col items-center gap-2 sm:right-6 sm:left-auto sm:items-end">
      {toasts.map((toast) => (
        <div key={toast.id} className="pointer-events-auto w-full max-w-sm">
          <Toast {...toast} onDismiss={dismiss} />
        </div>
      ))}
    </div>
  );
}
