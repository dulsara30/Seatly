// Fails lint on Tailwind arbitrary values (x-[...]) and raw hex colours outside globals.css.

import { readdirSync, readFileSync, statSync } from "node:fs";
import { join, relative, sep } from "node:path";

const SOURCE_ROOT = "src";
const TOKEN_FILE = join("src", "app", "globals.css");
const SCANNED_EXTENSIONS = [".ts", ".tsx", ".css"];

const ARBITRARY_VALUE = /\b[a-z][a-z0-9-]*-\[[^\]\s]+\]/g;
const HEX_COLOUR = /#[0-9a-fA-F]{3,8}\b/g;

function sourceFiles(directory) {
  return readdirSync(directory).flatMap((entry) => {
    const path = join(directory, entry);
    if (statSync(path).isDirectory()) {
      return sourceFiles(path);
    }
    return SCANNED_EXTENSIONS.some((extension) => path.endsWith(extension)) ? [path] : [];
  });
}

const violations = [];

for (const file of sourceFiles(SOURCE_ROOT)) {
  const lines = readFileSync(file, "utf8").split("\n");
  lines.forEach((line, index) => {
    const found = [...line.matchAll(ARBITRARY_VALUE)].map((match) => match[0]);
    if (file !== TOKEN_FILE) {
      found.push(...[...line.matchAll(HEX_COLOUR)].map((match) => match[0]));
    }
    for (const value of found) {
      violations.push(`${relative(".", file).split(sep).join("/")}:${index + 1}  ${value}`);
    }
  });
}

if (violations.length > 0) {
  console.error("Design token violations — use a token from globals.css instead:\n");
  console.error(violations.map((violation) => `  ${violation}`).join("\n"));
  process.exit(1);
}

console.log("Design tokens: no arbitrary values or raw colours.");
