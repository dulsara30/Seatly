/** A form-level error - one not tied to any single field, e.g. wrong credentials. */
export function FormStatus({ message }: { message: string | undefined }) {
  if (!message) {
    return null;
  }
  return (
    <p
      role="alert"
      className="rounded-md bg-red-50 px-3 py-2 text-body-s text-red-600">
      {message}
    </p>
  );
}
