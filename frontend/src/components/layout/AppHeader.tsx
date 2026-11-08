import { GitCompareArrows, Moon, RefreshCw, Sun } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { useBackendHealth, type HealthStatus } from "@/hooks/useBackendHealth";
import { useTheme } from "@/hooks/useTheme";
import { USE_MOCK } from "@/api/codebridge";
import { cn } from "@/lib/utils";

const HEALTH_LABEL: Record<HealthStatus, string> = {
  checking: "Checking backend…",
  online: "Backend online",
  offline: "Backend offline",
};

const HEALTH_DOT: Record<HealthStatus, string> = {
  checking: "bg-amber-400 animate-pulse",
  online: "bg-emerald-500",
  offline: "bg-red-500",
};

export function AppHeader() {
  const { status, recheck } = useBackendHealth();
  const { theme, toggle } = useTheme();

  return (
    <header className="bg-background/80 sticky top-0 z-30 border-b backdrop-blur supports-[backdrop-filter]:bg-background/60">
      <div className="mx-auto flex h-16 max-w-7xl items-center gap-3 px-4 sm:px-6 lg:px-8">
        <div className="bg-primary text-primary-foreground flex size-9 items-center justify-center rounded-lg shadow-sm">
          <GitCompareArrows className="size-5" aria-hidden="true" />
        </div>
        <div className="min-w-0">
          <p className="truncate text-base leading-tight font-semibold tracking-tight">CodeBridge</p>
          <p className="text-muted-foreground hidden truncate text-xs sm:block">
            Repository dependency comparison
          </p>
        </div>

        <div className="ml-auto flex items-center gap-2">
          {USE_MOCK && (
            <Badge variant="outline" className="border-amber-500/40 text-amber-700 dark:text-amber-300">
              Mock data
            </Badge>
          )}

          <button
            type="button"
            onClick={() => void recheck()}
            className="hover:bg-accent inline-flex items-center gap-2 rounded-full border px-3 py-1 text-xs font-medium transition-colors"
            title="Re-check GET /api/health"
          >
            <span className={cn("size-2 rounded-full", HEALTH_DOT[status])} aria-hidden="true" />
            <span className="hidden sm:inline">{HEALTH_LABEL[status]}</span>
            <span className="sr-only sm:hidden">{HEALTH_LABEL[status]}</span>
            <RefreshCw className="text-muted-foreground size-3" aria-hidden="true" />
          </button>

          <Button
            variant="ghost"
            size="icon"
            onClick={toggle}
            aria-label={theme === "dark" ? "Switch to light mode" : "Switch to dark mode"}
          >
            {theme === "dark" ? <Sun /> : <Moon />}
          </Button>
        </div>
      </div>
    </header>
  );
}
