import { AlertCircle, RotateCcw } from "lucide-react";
import { AppHeader } from "@/components/layout/AppHeader";
import { CompareForm } from "@/components/compare/CompareForm";
import { ComparisonResults } from "@/components/compare/ComparisonResults";
import { EmptyState } from "@/components/compare/EmptyState";
import { LoadingState } from "@/components/compare/LoadingState";
import { Alert, AlertDescription, AlertTitle } from "@/components/ui/alert";
import { Button } from "@/components/ui/button";
import { useRepoComparison } from "@/hooks/useRepoComparison";

export default function App() {
  const { status, result, error, compare, cancel, reset } = useRepoComparison();

  return (
    <div className="min-h-svh">
      <AppHeader />

      <main className="mx-auto flex max-w-7xl flex-col gap-6 px-4 py-6 sm:px-6 sm:py-8 lg:px-8">
        <CompareForm loading={status === "loading"} onCompare={compare} onCancel={cancel} />

        {status === "error" && error && (
          <Alert variant="destructive">
            <AlertCircle />
            <AlertTitle>Comparison failed</AlertTitle>
            <AlertDescription>
              <p>{error}</p>
              <Button variant="outline" size="sm" className="mt-2" onClick={reset}>
                <RotateCcw /> Start over
              </Button>
            </AlertDescription>
          </Alert>
        )}

        {status === "loading" && <LoadingState />}
        {status === "success" && result && <ComparisonResults key={result.completedAt.getTime()} result={result} />}
        {status === "idle" && <EmptyState />}
      </main>
    </div>
  );
}
