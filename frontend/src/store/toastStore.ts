import { create } from "zustand";

export const ToastTone = {
  SUCCESS: "SUCCESS",
  INFO: "INFO",
  ERROR: "ERROR",
} as const;
export type ToastTone = (typeof ToastTone)[keyof typeof ToastTone];

export interface ToastMessage {
  id: number;
  tone: ToastTone;
  message: string;
}

interface ToastState {
  toasts: ToastMessage[];
  show: (tone: ToastTone, message: string) => void;
  dismiss: (id: number) => void;
}

let nextToastId = 0;

// Client state (the server never sees a toast), so Zustand rather than a query.
export const useToastStore = create<ToastState>((set) => ({
  toasts: [],
  show: (tone, message) => {
    nextToastId += 1;
    const toast = { id: nextToastId, tone, message };
    set((state) => ({ toasts: [...state.toasts, toast] }));
  },
  dismiss: (id) =>
    set((state) => ({
      toasts: state.toasts.filter((toast) => toast.id !== id),
    })),
}));
