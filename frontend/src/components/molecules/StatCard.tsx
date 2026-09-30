import { classNames } from "@/utils/classNames";

interface StatCardProps {
  value: number;
  label: string;
  /** One card per row may be accented - a tint and a border, never a yellow fill. */
  accent?: boolean;
}

export function StatCard({ value, label, accent = false }: StatCardProps) {
  return (
    <div
      className={classNames(
        "flex flex-col gap-1 rounded-lg border p-5",
        accent ? "border-yellow-500 bg-yellow-50" : "border-gray-200 bg-white",
      )}>
      <span className="text-heading-xl text-black">{value}</span>
      <span className="text-caption text-gray-500">{label}</span>
    </div>
  );
}
