import { useState, type FormEvent } from "react";
import { ArrowLeftRight, GitBranch, Search, X } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Spinner } from "@/components/Spinner";
import { parseRepoSlug, validateRepoInput, type RepoSlug } from "@/lib/repo";
import { cn } from "@/lib/utils";

interface CompareFormProps {
  loading: boolean;
  onCompare: (existing: RepoSlug, incoming: RepoSlug) => void;
  onCancel: () => void;
}

/** Repos that have a pom.xml at the root, so the backend can analyse them. */
const EXAMPLES: ReadonlyArray<{ existing: string; incoming: string }> = [
  { existing: "spring-projects/spring-petclinic", incoming: "spring-petclinic/spring-petclinic-rest" },
  { existing: "apache/commons-lang", incoming: "apache/commons-text" },
];

type Field = "existing" | "incoming";

export function CompareForm({ loading, onCompare, onCancel }: CompareFormProps) {
  const [values, setValues] = useState<Record<Field, string>>({ existing: "", incoming: "" });
  const [touched, setTouched] = useState<Record<Field, boolean>>({ existing: false, incoming: false });
  const [submitted, setSubmitted] = useState(false);

  const errors: Record<Field, string | null> = {
    existing: validateRepoInput(values.existing),
    incoming: validateRepoInput(values.incoming),
  };
  const sameRepo =
    !errors.existing &&
    !errors.incoming &&
    values.existing.trim().toLowerCase() === values.incoming.trim().toLowerCase();

  const showError = (field: Field) => (touched[field] || submitted) && errors[field];

  const handleSubmit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setSubmitted(true);
    const existing = parseRepoSlug(values.existing);
    const incoming = parseRepoSlug(values.incoming);
    if (!existing || !incoming || sameRepo) return;
    onCompare(existing, incoming);
  };

  const swap = () => setValues((v) => ({ existing: v.incoming, incoming: v.existing }));

  const fillExample = (example: (typeof EXAMPLES)[number]) => {
    setValues({ existing: example.existing, incoming: example.incoming });
    setSubmitted(false);
    setTouched({ existing: false, incoming: false });
  };

  const renderField = (field: Field, label: string, hint: string, accent: string) => {
    const id = `repo-${field}`;
    const errorId = `${id}-error`;
    const error = showError(field);
    return (
      <div className="flex min-w-0 flex-1 flex-col gap-2">
        <Label htmlFor={id} className="gap-2">
          <span className={cn("size-2 rounded-full", accent)} aria-hidden="true" />
          {label}
          <span className="text-muted-foreground font-normal">{hint}</span>
        </Label>
        <div className="relative">
          <GitBranch
            className="text-muted-foreground pointer-events-none absolute top-1/2 left-3 size-4 -translate-y-1/2"
            aria-hidden="true"
          />
          <Input
            id={id}
            value={values[field]}
            onChange={(e) => setValues((v) => ({ ...v, [field]: e.target.value }))}
            onBlur={() => setTouched((t) => ({ ...t, [field]: true }))}
            placeholder="owner/repo"
            autoComplete="off"
            spellCheck={false}
            disabled={loading}
            aria-invalid={Boolean(error)}
            aria-describedby={error ? errorId : undefined}
            className="h-11 pl-9 font-mono text-sm"
          />
        </div>
        <p id={errorId} className={cn("min-h-4 text-xs", error ? "text-destructive" : "text-transparent")}>
          {error || " "}
        </p>
      </div>
    );
  };

  return (
    <Card className="gap-4">
      <CardHeader>
        <CardTitle className="text-lg">Compare two repositories</CardTitle>
        <CardDescription>
          Enter two public GitHub repositories with a root <code className="font-mono">pom.xml</code>.
          CodeBridge compares their Maven dependencies side by side.
        </CardDescription>
      </CardHeader>
      <CardContent>
        <form onSubmit={handleSubmit} noValidate className="flex flex-col gap-4">
          <div className="flex flex-col gap-2 md:flex-row md:items-start md:gap-3">
            {renderField("existing", "Repository A", "existing", "bg-indigo-500")}

            <div className="flex justify-center md:pt-7">
              <Button
                type="button"
                variant="outline"
                size="icon"
                onClick={swap}
                disabled={loading}
                aria-label="Swap repositories"
                title="Swap repositories"
                className="h-11 w-11 rotate-90 rounded-full md:rotate-0"
              >
                <ArrowLeftRight />
              </Button>
            </div>

            {renderField("incoming", "Repository B", "incoming", "bg-fuchsia-500")}
          </div>

          {submitted && sameRepo && (
            <p className="text-destructive -mt-2 text-xs">Pick two different repositories to compare.</p>
          )}

          <div className="flex flex-col-reverse gap-3 sm:flex-row sm:items-center sm:justify-between">
            <div className="flex flex-wrap items-center gap-2 text-xs">
              <span className="text-muted-foreground">Try:</span>
              {EXAMPLES.map((example) => (
                <button
                  key={example.existing}
                  type="button"
                  disabled={loading}
                  onClick={() => fillExample(example)}
                  className="bg-secondary hover:bg-accent hover:text-accent-foreground rounded-full px-2.5 py-1 font-mono transition-colors disabled:opacity-50"
                >
                  {example.existing.split("/")[1]} ↔ {example.incoming.split("/")[1]}
                </button>
              ))}
            </div>

            <div className="flex gap-2 sm:shrink-0">
              {loading && (
                <Button type="button" variant="ghost" onClick={onCancel}>
                  <X /> Cancel
                </Button>
              )}
              <Button type="submit" size="lg" disabled={loading} className="min-w-36 flex-1 sm:flex-none">
                {loading ? (
                  <>
                    <Spinner label="Comparing repositories" /> Comparing…
                  </>
                ) : (
                  <>
                    <Search /> Compare
                  </>
                )}
              </Button>
            </div>
          </div>
        </form>
      </CardContent>
    </Card>
  );
}
