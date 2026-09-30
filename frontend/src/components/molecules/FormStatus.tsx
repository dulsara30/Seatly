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
