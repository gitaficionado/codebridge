import type { ConflictSeverity } from "@/types/api";

interface SeverityStyle {
  label: string;
  description: string;
  /** Badge / pill classes */
  badge: string;
  /** Small dot */
  dot: string;
  /** Bar fill */
  bar: string;
}

export const SEVERITY_STYLES: Record<ConflictSeverity, SeverityStyle> = {
  HIGH: {
    label: "High",
    description: "Major version differs",
    badge: "bg-red-500/10 text-red-700 border-red-500/20 dark:text-red-300",
    dot: "bg-red-500",
    bar: "bg-red-500",
  },
  MEDIUM: {
    label: "Medium",
    description: "Minor version differs",
    badge: "bg-amber-500/10 text-amber-700 border-amber-500/25 dark:text-amber-300",
    dot: "bg-amber-500",
    bar: "bg-amber-500",
  },
  LOW: {
    label: "Low",
    description: "Patch version differs",
    badge: "bg-sky-500/10 text-sky-700 border-sky-500/20 dark:text-sky-300",
    dot: "bg-sky-500",
    bar: "bg-sky-500",
  },
  UNKNOWN: {
    label: "Unknown",
    description: "Version could not be compared",
    badge: "bg-slate-500/10 text-slate-700 border-slate-500/20 dark:text-slate-300",
    dot: "bg-slate-400",
    bar: "bg-slate-400",
  },
};
