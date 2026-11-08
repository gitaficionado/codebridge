import { useMemo, useState } from "react";
import { ArrowRight, CheckCircle2, Search } from "lucide-react";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { SEVERITY_STYLES } from "@/components/compare/severity";
import { CONFLICT_SEVERITIES, type ConflictSeverity, type MavenDependencyConflict } from "@/types/api";
import { cn } from "@/lib/utils";

interface ConflictTableProps {
  conflicts: MavenDependencyConflict[];
  existingLabel: string;
  incomingLabel: string;
  severity: ConflictSeverity | null;
  onSeverityChange: (severity: ConflictSeverity | null) => void;
}

const SEVERITY_RANK: Record<ConflictSeverity, number> = { HIGH: 0, MEDIUM: 1, LOW: 2, UNKNOWN: 3 };

export function ConflictTable({
  conflicts,
  existingLabel,
  incomingLabel,
  severity,
  onSeverityChange,
}: ConflictTableProps) {
  const [query, setQuery] = useState("");

  const rows = useMemo(() => {
    const q = query.trim().toLowerCase();
    return conflicts
      .filter((c) => !severity || c.severity === severity)
      .filter((c) => !q || `${c.groupId}:${c.artifactId}`.toLowerCase().includes(q))
      .sort(
        (a, b) =>
          SEVERITY_RANK[a.severity] - SEVERITY_RANK[b.severity] ||
          a.groupId.localeCompare(b.groupId) ||
          a.artifactId.localeCompare(b.artifactId),
      );
  }, [conflicts, query, severity]);

  if (conflicts.length === 0) {
    return (
      <Card>
        <CardContent className="flex flex-col items-center gap-2 py-8 text-center">
          <CheckCircle2 className="size-10 text-emerald-500" aria-hidden="true" />
          <p className="font-medium">No version conflicts found</p>
          <p className="text-muted-foreground max-w-md text-sm">
            The two repositories don’t share any Maven dependencies with differing versions.
          </p>
        </CardContent>
      </Card>
    );
  }

  return (
    <Card className="gap-4">
      <CardHeader>
        <CardTitle>Dependency conflicts</CardTitle>
        <CardDescription>
          Showing {rows.length} of {conflicts.length}. Severity describes how far apart the versions
          are — not a confirmed incompatibility.
        </CardDescription>
      </CardHeader>

      <CardContent className="flex flex-col gap-4">
        <div className="flex flex-col gap-3 md:flex-row md:items-center md:justify-between">
          <div className="flex flex-wrap gap-1.5" role="group" aria-label="Filter by severity">
            <Button
              size="sm"
              variant={severity === null ? "secondary" : "ghost"}
              onClick={() => onSeverityChange(null)}
            >
              All
            </Button>
            {CONFLICT_SEVERITIES.map((s) => (
              <Button
                key={s}
                size="sm"
                variant={severity === s ? "secondary" : "ghost"}
                onClick={() => onSeverityChange(severity === s ? null : s)}
                aria-pressed={severity === s}
              >
                <span className={cn("size-2 rounded-full", SEVERITY_STYLES[s].dot)} aria-hidden="true" />
                {SEVERITY_STYLES[s].label}
              </Button>
            ))}
          </div>
          <div className="relative md:w-72">
            <Search
              className="text-muted-foreground pointer-events-none absolute top-1/2 left-3 size-4 -translate-y-1/2"
              aria-hidden="true"
            />
            <Input
              value={query}
              onChange={(e) => setQuery(e.target.value)}
              placeholder="Filter by groupId or artifactId"
              className="pl-9"
              aria-label="Filter dependencies"
            />
          </div>
        </div>

        {/* Desktop / tablet: table */}
        <div className="hidden rounded-lg border sm:block">
          <Table>
            <TableHeader>
              <TableRow className="hover:bg-transparent">
                <TableHead>Dependency</TableHead>
                <TableHead>
                  <span className="inline-flex items-center gap-1.5">
                    <span className="size-2 rounded-full bg-indigo-500" aria-hidden="true" />
                    {existingLabel}
                  </span>
                </TableHead>
                <TableHead className="w-6" aria-hidden="true" />
                <TableHead>
                  <span className="inline-flex items-center gap-1.5">
                    <span className="size-2 rounded-full bg-fuchsia-500" aria-hidden="true" />
                    {incomingLabel}
                  </span>
                </TableHead>
                <TableHead className="text-right">Severity</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {rows.map((c) => (
                <TableRow key={`${c.groupId}:${c.artifactId}`}>
                  <TableCell className="max-w-[22rem] whitespace-normal">
                    <div className="font-medium break-all">{c.artifactId}</div>
                    <div className="text-muted-foreground font-mono text-xs break-all">{c.groupId}</div>
                  </TableCell>
                  <TableCell className="font-mono text-xs">{c.existingVersion}</TableCell>
                  <TableCell className="text-muted-foreground px-0">
                    <ArrowRight className="size-3.5" aria-hidden="true" />
                  </TableCell>
                  <TableCell className="font-mono text-xs">{c.incomingVersion}</TableCell>
                  <TableCell className="text-right">
                    <SeverityBadge severity={c.severity} />
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </div>

        {/* Mobile: stacked cards */}
        <ul className="flex flex-col gap-2 sm:hidden">
          {rows.map((c) => (
            <li key={`${c.groupId}:${c.artifactId}`} className="rounded-lg border p-3">
              <div className="flex items-start justify-between gap-2">
                <div className="min-w-0">
                  <p className="font-medium break-all">{c.artifactId}</p>
                  <p className="text-muted-foreground font-mono text-xs break-all">{c.groupId}</p>
                </div>
                <SeverityBadge severity={c.severity} />
              </div>
              <div className="mt-2 flex items-center gap-2 font-mono text-xs">
                <span className="rounded bg-indigo-500/10 px-1.5 py-0.5">{c.existingVersion}</span>
                <ArrowRight className="text-muted-foreground size-3" aria-hidden="true" />
                <span className="rounded bg-fuchsia-500/10 px-1.5 py-0.5">{c.incomingVersion}</span>
              </div>
            </li>
          ))}
        </ul>

        {rows.length === 0 && (
          <p className="text-muted-foreground py-6 text-center text-sm">No conflicts match these filters.</p>
        )}
      </CardContent>
    </Card>
  );
}

function SeverityBadge({ severity }: { severity: ConflictSeverity }) {
  const style = SEVERITY_STYLES[severity];
  return (
    <Badge variant="outline" className={style.badge}>
      <span className={cn("size-1.5 rounded-full", style.dot)} aria-hidden="true" />
      {style.label}
    </Badge>
  );
}
