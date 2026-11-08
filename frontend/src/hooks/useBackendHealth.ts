import { useCallback, useEffect, useState } from "react";
import { codebridgeApi } from "@/api/codebridge";

export type HealthStatus = "checking" | "online" | "offline";

/** Polls the public GET /api/health endpoint so the header can show backend status. */
export function useBackendHealth(intervalMs = 15_000) {
  const [status, setStatus] = useState<HealthStatus>("checking");

  const check = useCallback(async () => {
    try {
      await codebridgeApi.health();
      setStatus("online");
    } catch {
      setStatus("offline");
    }
  }, []);

  useEffect(() => {
    void check();
    const id = window.setInterval(() => void check(), intervalMs);
    return () => window.clearInterval(id);
  }, [check, intervalMs]);

  return { status, recheck: check };
}
