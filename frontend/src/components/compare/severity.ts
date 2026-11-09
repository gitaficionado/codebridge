import type { ConflictSeverity } from "@/types/api";

type ChipColor = "error" | "warning" | "info" | "default";

interface SeverityStyle {
  label: string;
  description: string;
  /** MUI color name for Chips / ToggleButtons */
  color: ChipColor;
  /** Palette path for solid fills (dots, bars) */
  fill: string;
}

export const SEVERITY_STYLES: Record<ConflictSeverity, SeverityStyle> = {
  HIGH: { label: "High", description: "Major version differs", color: "error", fill: "error.main" },
  MEDIUM: { label: "Medium", description: "Minor version differs", color: "warning", fill: "warning.main" },
  LOW: { label: "Low", description: "Patch version differs", color: "info", fill: "info.main" },
  UNKNOWN: { label: "Unknown", description: "Version could not be compared", color: "default", fill: "grey.500" },
};
