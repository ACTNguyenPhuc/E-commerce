import type { Category } from "@/types/catalog";

export function flattenCategories(
  nodes: Category[],
  depth = 0
): { id: number; name: string; depth: number }[] {
  const rows: { id: number; name: string; depth: number }[] = [];
  for (const n of nodes) {
    rows.push({ id: n.id, name: n.name, depth });
    if (n.children?.length) {
      rows.push(...flattenCategories(n.children, depth + 1));
    }
  }
  return rows;
}

export function categoryOptions(cats: Category[]): { value: number; label: string }[] {
  return flattenCategories(cats).map((r) => ({
    value: r.id,
    label: `${"— ".repeat(r.depth)}${r.name}`,
  }));
}
