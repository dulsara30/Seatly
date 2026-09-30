import { Button } from "@/components/atoms/Button";
import { Copy } from "@/constants/copy";

interface ErrorStateProps {
  title: string;
  // Already user copy - an ApiError's message, never a raw key.
  message: string;
  onRetry?: () => void;
}

export function ErrorState({ title, message, onRetry }: ErrorStateProps) {
  return (
    <div
      role="alert"
      className="flex flex-col items-center gap-3 rounded-lg bg-red-50 px-6 py-12 text-center">
      <p className="text-heading-s text-red-600">{title}</p>
      <p className="max-w-sm text-body-m text-red-600">{message}</p>
      {onRetry && (
        <Button variant="secondary" size="small" onClick={onRetry}>
          {Copy.common.retry}
        </Button>
      )}
    </div>
  );
}
