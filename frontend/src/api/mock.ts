import type {
  GitHubRepositoryMetadata,
  MavenComparisonRequest,
  MavenComparisonResponse,
  MavenDependencyConflict,
  Project,
  ProjectRequest,
} from "@/types/api";
import type { RepoSlug } from "@/lib/repo";

/** Fake backend used by `npm run dev:mock` — handy for UI work and demos without Java. */

function delay<T>(value: T, ms: number, signal?: AbortSignal): Promise<T> {
  return new Promise((resolve, reject) => {
    const timer = setTimeout(() => resolve(value), ms);
    signal?.addEventListener("abort", () => {
      clearTimeout(timer);
      reject(new DOMException("Aborted", "AbortError"));
    });
  });
}

function slugFromUrl(url: string): string {
  return url.replace(/^https:\/\/github\.com\//, "").replace(/\/$/, "");
}

const SAMPLE_CONFLICTS: MavenDependencyConflict[] = [
  { groupId: "org.springframework.boot", artifactId: "spring-boot-starter-web", existingVersion: "2.7.18", incomingVersion: "3.3.4", severity: "HIGH" },
  { groupId: "com.fasterxml.jackson.core", artifactId: "jackson-databind", existingVersion: "2.13.5", incomingVersion: "2.17.2", severity: "MEDIUM" },
  { groupId: "org.junit.jupiter", artifactId: "junit-jupiter", existingVersion: "5.9.3", incomingVersion: "5.10.2", severity: "MEDIUM" },
  { groupId: "org.slf4j", artifactId: "slf4j-api", existingVersion: "1.7.36", incomingVersion: "2.0.13", severity: "HIGH" },
  { groupId: "org.apache.commons", artifactId: "commons-lang3", existingVersion: "3.14.0", incomingVersion: "3.14.1", severity: "LOW" },
  { groupId: "com.h2database", artifactId: "h2", existingVersion: "${h2.version}", incomingVersion: "2.2.224", severity: "UNKNOWN" },
];

let projects: Project[] = [];

export const mockApi = {
  async health(): Promise<string> {
    return delay("CodeBridge Backend is Running! (mock)", 150);
  },

  async compareMaven(
    request: MavenComparisonRequest,
    signal?: AbortSignal,
  ): Promise<MavenComparisonResponse> {
    const conflicts = SAMPLE_CONFLICTS;
    const count = (s: MavenDependencyConflict["severity"]) =>
      conflicts.filter((c) => c.severity === s).length;

    return delay(
      {
        existingRepository: slugFromUrl(request.existingRepositoryUrl),
        incomingRepository: slugFromUrl(request.incomingRepositoryUrl),
        existingDependencyCount: 18,
        incomingDependencyCount: 24,
        potentialConflictCount: conflicts.length,
        conflicts,
        severitySummary: {
          high: count("HIGH"),
          medium: count("MEDIUM"),
          low: count("LOW"),
          unknown: count("UNKNOWN"),
        },
      },
      1400,
      signal,
    );
  },

  async getRepository(slug: RepoSlug, signal?: AbortSignal): Promise<GitHubRepositoryMetadata> {
    return delay(
      {
        fullName: `${slug.owner}/${slug.repo}`,
        description: "Sample repository description (mock data).",
        language: "Java",
        defaultBranch: "main",
        isPrivate: false,
        htmlUrl: `https://github.com/${slug.owner}/${slug.repo}`,
      },
      700,
      signal,
    );
  },

  async listProjects(): Promise<Project[]> {
    return delay(projects, 200);
  },

  async createProject(request: ProjectRequest): Promise<Project> {
    const project: Project = { id: projects.length + 1, status: "PENDING", ...request };
    projects = [...projects, project];
    return delay(project, 300);
  },
};
