import { GitCompareArrows, Layers, ShieldCheck, Zap } from "lucide-react";

const FEATURES = [
  { icon: Layers, title: "Side-by-side", text: "See both repositories and their dependency counts together." },
  { icon: Zap, title: "Version drift", text: "Spot shared libraries pinned to different versions." },
  { icon: ShieldCheck, title: "Severity", text: "Major, minor and patch differences ranked for review." },
];

export function EmptyState() {
  return (
    <section className="flex flex-col items-center gap-8 rounded-xl border border-dashed px-6 py-12 text-center">
      <div className="bg-primary/10 text-primary flex size-14 items-center justify-center rounded-2xl">
        <GitCompareArrows className="size-7" aria-hidden="true" />
      </div>
      <div className="max-w-lg">
        <h2 className="text-lg font-semibold">Pick two repositories to get started</h2>
        <p className="text-muted-foreground mt-1 text-sm">
          Results appear here: repository details side by side, a severity breakdown and every
          dependency whose version differs.
        </p>
      </div>
      <ul className="grid w-full max-w-3xl gap-4 sm:grid-cols-3">
        {FEATURES.map(({ icon: Icon, title, text }) => (
          <li key={title} className="bg-card rounded-lg border p-4 text-left">
            <Icon className="text-primary size-5" aria-hidden="true" />
            <p className="mt-2 text-sm font-medium">{title}</p>
            <p className="text-muted-foreground mt-1 text-xs">{text}</p>
          </li>
        ))}
      </ul>
    </section>
  );
}
