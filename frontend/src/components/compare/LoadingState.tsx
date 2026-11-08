import { Card, CardContent, CardHeader } from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";
import { Spinner } from "@/components/Spinner";

/** Spinner banner plus skeletons shaped like the results, shown while the backend works. */
export function LoadingState() {
  return (
    <section aria-busy="true" aria-label="Loading comparison" className="flex flex-col gap-6">
      <div className="bg-card flex flex-col items-center gap-3 rounded-xl border px-6 py-8 text-center shadow-sm">
        <div className="bg-primary/10 text-primary flex size-14 items-center justify-center rounded-full">
          <Spinner className="size-7" label="Comparing repositories" />
        </div>
        <div>
          <p className="font-medium">Analyzing repositories…</p>
          <p className="text-muted-foreground text-sm">
            Fetching both <code className="font-mono">pom.xml</code> files from GitHub and comparing
            dependency versions.
          </p>
        </div>
      </div>

      <div className="grid gap-4 lg:grid-cols-2">
        {[0, 1].map((i) => (
          <Card key={i}>
            <CardHeader className="gap-3">
              <Skeleton className="h-4 w-24" />
              <Skeleton className="h-6 w-2/3" />
              <Skeleton className="h-4 w-full" />
            </CardHeader>
            <CardContent className="grid grid-cols-3 gap-3">
              <Skeleton className="h-14" />
              <Skeleton className="h-14" />
              <Skeleton className="h-14" />
            </CardContent>
          </Card>
        ))}
      </div>

      <div className="grid grid-cols-2 gap-4 sm:grid-cols-3 lg:grid-cols-5">
        {[0, 1, 2, 3, 4].map((i) => (
          <Skeleton key={i} className="h-24 rounded-xl" />
        ))}
      </div>

      <Card>
        <CardContent className="flex flex-col gap-3">
          {[0, 1, 2, 3, 4].map((i) => (
            <Skeleton key={i} className="h-9" />
          ))}
        </CardContent>
      </Card>
    </section>
  );
}
