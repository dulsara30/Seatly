import { useEffect } from "react";
import { IconButton } from "@/components/atoms/IconButton";
import { Copy } from "@/constants/copy";
import { TOAST_DURATION_MS } from "@/constants/timing";
import { ToastTone, type ToastMessage } from "@/store/toastStore";
import { classNames } from "@/utils/classNames";

const TONE_CLASSES: Record<ToastTone, string> = {
  SUCCESS: "bg-black text-white",
  INFO: "border border-yellow-500 bg-yellow-50 text-yellow-700",
  ERROR: "border border-red-300 bg-red-50 text-red-600",
};

interface ToastProps extends ToastMessage {
  /** Takes the id, so the parent can pass one stable function for every toast. */
  onDismiss: (id: number) => void;
}

/**
 * Dismisses itself after TOAST_DURATION_MS. The timer depends only on stable
 * values (the id and the store's dismiss), so a new toast arriving doesn't
 * restart the countdown of the ones already showing.
 */
export function Toast({ id, tone, message, onDismiss }: ToastProps) {
  useEffect(() => {
    const timer = setTimeout(() => onDismiss(id), TOAST_DURATION_MS);
    return () => clearTimeout(timer);
  }, [id, onDismiss]);

  return (
    <div
      role={tone === ToastTone.ERROR ? "alert" : "status"}
      className={classNames("flex items-center gap-3 rounded-md py-1 pr-1 pl-4 text-label-m", TONE_CLASSES[tone])}
    >
      <span className="flex-1">{message}</span>
      <IconButton
        icon="close"
        label={Copy.common.close}
        variant="ghost"
        onClick={() => onDismiss(id)}
        className="text-current hover:bg-transparent"
      />
    </div>
  );
}
