import type { ReactNode } from "react";
import { AlertTriangle, Code2, ExternalLink, GitBranch, Package } from "lucide-react";
import { Badge } from "@/components/ui/badge";
import { Card, CardAction, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { toGitHubUrl, toSlugString } from "@/lib/repo";
import type { RepoSide } from "@/hooks/useRepoComparison";
import { cn } from "@/lib/utils";

interface RepoCardProps {
  side: RepoSide;
  role: "A" | "B";
  label: string;
  dependencyCount: number;
}

const ROLE_STYLES = {
  A: { stripe: "bg-indigo-500", chip: "bg-indigo-500/10 text-indigo-700 dark:text-indigo-300" },
  B: { stripe: "bg-fuchsia-500", chip: "bg-fuchsia-500/10 text-fuchsia-700 dark:text-fuchsia-300" },
} as const;

export function RepoCard({ side, role, label, dependencyCount }: RepoCardProps) {
  const { slug, metadata, metadataError } = side;
  const styles = ROLE_STYLES[role];
  const name = metadata?.fullName ?? toSlugString(slug);
  const url = metadata?.htmlUrl ?? toGitHubUrl(slug);

  return (
    <Card className="relative gap-5 overflow-hidden">
      <div className={cn("absolute inset-x-0 top-0 h-1", styles.stripe)} aria-hidden="true" />
      <CardHeader>
        <div className="flex items-center gap-2">
          <span className={cn("rounded-md px-2 py-0.5 text-xs font-semibold", styles.chip)}>
            {role} · {label}
          </span>
        </div>
        <CardTitle className="mt-2 font-mono text-lg break-all">{name}</CardTitle>
        <CardDescription className="line-clamp-2">
          {metadata?.description ||
            (metadataError ? "Repository details unavailable." : "No description provided.")}
        </CardDescription>
        <CardAction>
          <a
            href={url}
            target="_blank"
            rel="noreferrer"
            className="text-muted-foreground hover:text-foreground inline-flex items-center gap-1 text-xs"
          >
            GitHub <ExternalLink className="size-3" aria-hidden="true" />
          </a>
        </CardAction>
      </CardHeader>

      <CardContent className="flex flex-col gap-4">
        <dl className="grid grid-cols-3 gap-3">
          <Stat icon={<Package />} label="Dependencies" value={String(dependencyCount)} emphasis />
          <Stat icon={<Code2 />} label="Language" value={metadata?.language ?? "—"} />
          <Stat icon={<GitBranch />} label="Branch" value={metadata?.defaultBranch ?? "—"} mono />
        </dl>

        {metadataError && (
          <p className="flex items-start gap-2 text-xs text-amber-700 dark:text-amber-300">
            <AlertTriangle className="mt-0.5 size-3.5 shrink-0" aria-hidden="true" />
            Couldn’t load repository details: {metadataError}
          </p>
        )}
        {metadata?.isPrivate && <Badge variant="destructive">Private repository</Badge>}
      </CardContent>
    </Card>
  );
}

interface StatProps {
  icon: ReactNode;
  label: string;
  value: string;
  emphasis?: boolean;
  mono?: boolean;
}

function Stat({ icon, label, value, emphasis, mono }: StatProps) {
  return (
    <div className="bg-muted/50 min-w-0 rounded-lg px-3 py-2.5">
      <dt className="text-muted-foreground flex items-center gap-1.5 text-xs [&_svg]:size-3.5">
        {icon}
        {label}
      </dt>
      <dd
        className={cn(
          "mt-1 truncate",
          emphasis ? "text-2xl font-semibold tabular-nums" : "text-sm font-medium",
          mono && "font-mono",
        )}
        title={value}
      >
        {value}
      </dd>
    </div>
  );
}
