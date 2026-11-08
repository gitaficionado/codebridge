import { GitMerge } from "lucide-react";
import { Card } from "@/components/ui/card";
import { CONFLICT_SEVERITIES, type ConflictSeverity, type MavenSeveritySummary } from "@/types/api";
import { SEVERITY_STYLES } from "@/components/compare/severity";
import { cn } from "@/lib/utils";

interface SummaryStatsProps {
  summary: MavenSeveritySummary;
  total: number;
  activeSeverity: ConflictSeverity | null;
  onSelectSeverity: (severity: ConflictSeverity | null) => void;
}

const SUMMARY_KEY: Record<ConflictSeverity, keyof Omit<MavenSeveritySummary, "total">> = {
  HIGH: "high",
  MEDIUM: "medium",
  LOW: "low",
  UNKNOWN: "unknown",
};

/** KPI tiles. Clicking a severity tile filters the conflict table. */
export function SummaryStats({ summary, total, activeSeverity, onSelectSeverity }: SummaryStatsProps) {
  return (
    <div className="flex flex-col gap-3">
      <div className="grid grid-cols-2 gap-3 sm:grid-cols-3 lg:grid-cols-5">
        <Card className="col-span-2 gap-1 px-5 py-4 sm:col-span-1">
          <p className="text-muted-foreground flex items-center gap-1.5 text-xs font-medium">
            <GitMerge className="size-3.5" aria-hidden="true" /> Potential conflicts
          </p>
          <p className="text-3xl font-semibold tabular-nums">{total}</p>
          <p className="text-muted-foreground text-xs">shared dependencies with different versions</p>
        </Card>

        {CONFLICT_SEVERITIES.map((severity) => {
          const style = SEVERITY_STYLES[severity];
          const count = summary[SUMMARY_KEY[severity]];
          const active = activeSeverity === severity;
          return (
            <button
              key={severity}
              type="button"
              onClick={() => onSelectSeverity(active ? null : severity)}
              aria-pressed={active}
              className={cn(
                "bg-card hover:border-ring/60 flex flex-col gap-1 rounded-xl border px-5 py-4 text-left shadow-sm transition-all",
                active && "ring-ring/50 border-ring ring-[3px]",
              )}
            >
              <span className="text-muted-foreground flex items-center gap-1.5 text-xs font-medium">
                <span className={cn("size-2 rounded-full", style.dot)} aria-hidden="true" />
                {style.label}
              </span>
              <span className="text-3xl font-semibold tabular-nums">{count}</span>
              <span className="text-muted-foreground text-xs">{style.description}</span>
            </button>
          );
        })}
      </div>

      {total > 0 && (
        <div className="bg-muted flex h-2 w-full overflow-hidden rounded-full" aria-hidden="true">
          {CONFLICT_SEVERITIES.map((severity) => {
            const count = summary[SUMMARY_KEY[severity]];
            if (!count) return null;
            return (
              <div
                key={severity}
                className={cn("h-full", SEVERITY_STYLES[severity].bar)}
                style={{ width: `${(count / total) * 100}%` }}
              />
            );
          })}
        </div>
      )}
    </div>
  );
}
