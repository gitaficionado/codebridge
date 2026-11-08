import { api } from "@/api/client";
import { mockApi } from "@/api/mock";
import type {
  GitHubRepositoryMetadata,
  MavenComparisonRequest,
  MavenComparisonResponse,
  Project,
  ProjectRequest,
} from "@/types/api";
import type { RepoSlug } from "@/lib/repo";

/** `npm run dev:mock` runs the UI on fake data so it works without the backend. */
export const USE_MOCK = import.meta.env.MODE === "mock";

const realApi = {
  /** GET /api/health (public) */
  async health(): Promise<string> {
    const { data } = await api.get<string>("/health", { responseType: "text" });
    return data;
  },

  /** POST /api/maven/compare */
  async compareMaven(
    request: MavenComparisonRequest,
    signal?: AbortSignal,
  ): Promise<MavenComparisonResponse> {
    const { data } = await api.post<MavenComparisonResponse>("/maven/compare", request, { signal });
    return data;
  },

  /** GET /api/github/repositories/{owner}/{repository} */
  async getRepository(slug: RepoSlug, signal?: AbortSignal): Promise<GitHubRepositoryMetadata> {
    const { data } = await api.get<GitHubRepositoryMetadata>(
      `/github/repositories/${encodeURIComponent(slug.owner)}/${encodeURIComponent(slug.repo)}`,
      { signal },
    );
    return data;
  },

  /** GET /api/projects */
  async listProjects(): Promise<Project[]> {
    const { data } = await api.get<Project[]>("/projects");
    return data;
  },

  /** POST /api/projects */
  async createProject(request: ProjectRequest): Promise<Project> {
    const { data } = await api.post<Project>("/projects", request);
    return data;
  },
};

export type CodeBridgeApi = typeof realApi;

export const codebridgeApi: CodeBridgeApi = USE_MOCK ? mockApi : realApi;
