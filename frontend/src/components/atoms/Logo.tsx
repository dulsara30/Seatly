import { Copy } from "@/constants/copy";

export function Logo() {
  return (
    <span className="text-heading-m text-black">
      {Copy.brand.name}
      <span className="text-yellow-500">{Copy.brand.mark}</span>
    </span>
  );
}
