import { useState } from "react";
import { Info } from "lucide-react";
import { RepoCard } from "@/components/compare/RepoCard";
import { SummaryStats } from "@/components/compare/SummaryStats";
import { ConflictTable } from "@/components/compare/ConflictTable";
import { toSlugString } from "@/lib/repo";
import type { ComparisonResult } from "@/hooks/useRepoComparison";
import type { ConflictSeverity } from "@/types/api";

interface ComparisonResultsProps {
  result: ComparisonResult;
}

export function ComparisonResults({ result }: ComparisonResultsProps) {
  const [severity, setSeverity] = useState<ConflictSeverity | null>(null);
  const { maven, existing, incoming } = result;

  const summary = maven.severitySummary;
  const total =
    summary.total ?? summary.high + summary.medium + summary.low + summary.unknown;

  return (
    <section aria-label="Comparison results" className="animate-in fade-in-0 slide-in-from-bottom-2 flex flex-col gap-6 duration-300">
      {/* Side-by-side entities */}
      <div className="relative grid gap-4 lg:grid-cols-2">
        <RepoCard side={existing} role="A" label="Existing" dependencyCount={maven.existingDependencyCount} />
        <div
          className="bg-background text-muted-foreground absolute top-1/2 left-1/2 z-10 hidden size-10 -translate-x-1/2 -translate-y-1/2 items-center justify-center rounded-full border text-xs font-semibold shadow-sm lg:flex"
          aria-hidden="true"
        >
          VS
        </div>
        <RepoCard side={incoming} role="B" label="Incoming" dependencyCount={maven.incomingDependencyCount} />
      </div>

      <SummaryStats
        summary={summary}
        total={total}
        activeSeverity={severity}
        onSelectSeverity={setSeverity}
      />

      <ConflictTable
        conflicts={maven.conflicts}
        existingLabel={toSlugString(existing.slug)}
        incomingLabel={toSlugString(incoming.slug)}
        severity={severity}
        onSeverityChange={setSeverity}
      />

      <p className="text-muted-foreground flex items-center gap-2 text-xs">
        <Info className="size-3.5 shrink-0" aria-hidden="true" />
        Compared {result.completedAt.toLocaleTimeString()} · only dependencies declared in each
        repository’s root pom.xml are included.
      </p>
    </section>
  );
}
