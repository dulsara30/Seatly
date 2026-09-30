import { classNames } from "@/utils/classNames";

export interface TabOption<Id extends string> {
  id: Id;
  label: string;
}

interface TabsProps<Id extends string> {
  label: string;
  tabs: readonly TabOption<Id>[];
  active: Id;
  onChange: (id: Id) => void;
}

export function Tabs<Id extends string>({
  label,
  tabs,
  active,
  onChange,
}: TabsProps<Id>) {
  return (
    <div
      role="tablist"
      aria-label={label}
      className="flex gap-6 border-b border-gray-200">
      {tabs.map((tab) => {
        const selected = tab.id === active;
        return (
          <button
            key={tab.id}
            type="button"
            role="tab"
            aria-selected={selected}
            onClick={() => onChange(tab.id)}
            className={classNames(
              "-mb-px border-b-2 pb-3 text-label-m transition-colors duration-150",
              "focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-black",
              selected
                ? "border-black text-black"
                : "border-transparent text-gray-500 hover:text-black",
            )}>
            {tab.label}
          </button>
        );
      })}
    </div>
  );
}
