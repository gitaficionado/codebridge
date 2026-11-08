import { useCallback, useEffect, useRef, useState } from "react";
import axios from "axios";
import { codebridgeApi } from "@/api/codebridge";
import { getErrorMessage } from "@/api/client";
import { toGitHubUrl, type RepoSlug } from "@/lib/repo";
import type { GitHubRepositoryMetadata, MavenComparisonResponse } from "@/types/api";

export type ComparisonStatus = "idle" | "loading" | "success" | "error";

export interface RepoSide {
  slug: RepoSlug;
  /** null when the metadata lookup failed — the comparison can still succeed. */
  metadata: GitHubRepositoryMetadata | null;
  metadataError: string | null;
}

export interface ComparisonResult {
  existing: RepoSide;
  incoming: RepoSide;
  maven: MavenComparisonResponse;
  completedAt: Date;
}

export interface ComparisonState {
  status: ComparisonStatus;
  result: ComparisonResult | null;
  error: string | null;
}

function isAbort(error: unknown): boolean {
  return (
    axios.isCancel(error) ||
    (error instanceof DOMException && error.name === "AbortError")
  );
}

/**
 * Runs the Maven comparison plus both GitHub metadata lookups in parallel.
 * Metadata failures are non-fatal; a comparison failure is shown as the error.
 */
export function useRepoComparison() {
  const [state, setState] = useState<ComparisonState>({
    status: "idle",
    result: null,
    error: null,
  });
  const controllerRef = useRef<AbortController | null>(null);

  useEffect(() => () => controllerRef.current?.abort(), []);

  const compare = useCallback(async (existing: RepoSlug, incoming: RepoSlug) => {
    controllerRef.current?.abort();
    const controller = new AbortController();
    controllerRef.current = controller;
    const { signal } = controller;

    setState((prev) => ({ ...prev, status: "loading", error: null }));

    const [mavenResult, existingMeta, incomingMeta] = await Promise.allSettled([
      codebridgeApi.compareMaven(
        {
          existingRepositoryUrl: toGitHubUrl(existing),
          incomingRepositoryUrl: toGitHubUrl(incoming),
        },
        signal,
      ),
      codebridgeApi.getRepository(existing, signal),
      codebridgeApi.getRepository(incoming, signal),
    ]);

    if (signal.aborted) return;

    if (mavenResult.status === "rejected") {
      if (isAbort(mavenResult.reason)) return;
      setState({ status: "error", result: null, error: getErrorMessage(mavenResult.reason) });
      return;
    }

    const side = (
      slug: RepoSlug,
      meta: PromiseSettledResult<GitHubRepositoryMetadata>,
    ): RepoSide => ({
      slug,
      metadata: meta.status === "fulfilled" ? meta.value : null,
      metadataError: meta.status === "rejected" ? getErrorMessage(meta.reason) : null,
    });

    setState({
      status: "success",
      error: null,
      result: {
        existing: side(existing, existingMeta),
        incoming: side(incoming, incomingMeta),
        maven: mavenResult.value,
        completedAt: new Date(),
      },
    });
  }, []);

  const cancel = useCallback(() => {
    controllerRef.current?.abort();
    setState((prev) => ({ ...prev, status: prev.result ? "success" : "idle" }));
  }, []);

  const reset = useCallback(() => {
    controllerRef.current?.abort();
    setState({ status: "idle", result: null, error: null });
  }, []);

  return { ...state, compare, cancel, reset };
}
