import {
  useId,
  type InputHTMLAttributes,
  type TextareaHTMLAttributes,
} from "react";
import { classNames } from "@/utils/classNames";

interface FieldProps {
  label: string;
  /** Shown only when set - the form decides when (e.g. after the field is touched). */
  error?: string;
  hint?: string;
}

type InputProps = FieldProps &
  (
    | ({ multiline?: false } & InputHTMLAttributes<HTMLInputElement>)
    | ({ multiline: true } & TextareaHTMLAttributes<HTMLTextAreaElement>)
  );

const CONTROL_CLASSES =
  "w-full rounded-md border bg-white px-3 text-body-m text-black placeholder:text-gray-400 " +
  "transition-colors duration-150 focus:border-black focus:outline-none disabled:bg-gray-50 disabled:text-gray-400";

/**
 * Label, control and error in one - so every field gets the same spacing,
 * and the error is always wired to the control for screen readers.
 * Spread Formik's getFieldProps(name) straight into it.
 */
export function Input(props: InputProps) {
  const generatedId = useId();
  const {
    label,
    error,
    hint,
    multiline,
    id = generatedId,
    className,
    ...controlProps
  } = props;
  const messageId = `${id}-message`;
  const describedBy = error || hint ? messageId : undefined;
  const controlClasses = classNames(
    CONTROL_CLASSES,
    error ? "border-red-300" : "border-gray-200",
    multiline ? "min-h-28 py-2" : "h-10",
    className,
  );

  return (
    <div className="flex flex-col gap-1.5">
      <label htmlFor={id} className="text-label-m text-black">
        {label}
      </label>
      {multiline ? (
        <textarea
          id={id}
          aria-invalid={Boolean(error)}
          aria-describedby={describedBy}
          className={controlClasses}
          {...(controlProps as TextareaHTMLAttributes<HTMLTextAreaElement>)}
        />
      ) : (
        <input
          id={id}
          aria-invalid={Boolean(error)}
          aria-describedby={describedBy}
          className={controlClasses}
          {...(controlProps as InputHTMLAttributes<HTMLInputElement>)}
        />
      )}
      {(error || hint) && (
        <p
          id={messageId}
          className={classNames(
            "text-caption",
            error ? "text-red-600" : "text-gray-500",
          )}>
          {error ?? hint}
        </p>
      )}
    </div>
  );
}
