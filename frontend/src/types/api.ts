/**
 * TypeScript mirrors of the Spring Boot DTOs in backend/src/main/java/com/codebridge/backend.
 * Keep these in sync when the Java records change.
 */

// ---- com.codebridge.backend.maven -----------------------------------------

/** maven/ConflictSeverity.java */
export type ConflictSeverity = "HIGH" | "MEDIUM" | "LOW" | "UNKNOWN";

export const CONFLICT_SEVERITIES: readonly ConflictSeverity[] = [
  "HIGH",
  "MEDIUM",
  "LOW",
  "UNKNOWN",
] as const;

/** maven/MavenDependencyConflict.java */
export interface MavenDependencyConflict {
  groupId: string;
  artifactId: string;
  existingVersion: string;
  incomingVersion: string;
  severity: ConflictSeverity;
}

/** maven/MavenDependency.java */
export interface MavenDependency {
  groupId: string;
  artifactId: string;
  version: string | null;
  scope: string | null;
}

// ---- com.codebridge.backend.dto -------------------------------------------

/** dto/MavenComparisonRequest.java — body of POST /api/maven/compare */
export interface MavenComparisonRequest {
  /** Full URL, e.g. https://github.com/owner/repo */
  existingRepositoryUrl: string;
  /** Full URL, e.g. https://github.com/owner/repo */
  incomingRepositoryUrl: string;
}

/** dto/MavenSeveritySummary.java */
export interface MavenSeveritySummary {
  high: number;
  medium: number;
  low: number;
  unknown: number;
  /** total() is a method on the record; it may not be serialized, so compute it client-side. */
  total?: number;
}

/** dto/MavenComparisonResponse.java — response of POST /api/maven/compare */
export interface MavenComparisonResponse {
  existingRepository: string;
  incomingRepository: string;
  existingDependencyCount: number;
  incomingDependencyCount: number;
  potentialConflictCount: number;
  conflicts: MavenDependencyConflict[];
  severitySummary: MavenSeveritySummary;
}

/** dto/ProjectRequest.java — body of POST /api/projects */
export interface ProjectRequest {
  name: string;
  existingRepoUrl: string;
  incomingRepoUrl: string;
}

// ---- com.codebridge.backend.entity ----------------------------------------

/** entity/ProjectStatus.java */
export type ProjectStatus = "PENDING" | "ANALYZING" | "COMPLETED" | "FAILED";

/** entity/Project.java — returned by /api/projects */
export interface Project {
  id: number;
  name: string;
  existingRepoUrl: string;
  incomingRepoUrl: string;
  status: ProjectStatus;
}

// ---- com.codebridge.backend.github ----------------------------------------

/** github/GitHubRepositoryMetadata.java — GET /api/github/repositories/{owner}/{repo} */
export interface GitHubRepositoryMetadata {
  fullName: string | null;
  description: string | null;
  language: string | null;
  defaultBranch: string | null;
  /** Java record component `isPrivate`. */
  isPrivate: boolean;
  htmlUrl: string | null;
}

// ---- com.codebridge.backend.exception.GlobalExceptionHandler --------------

/** Error body produced by GlobalExceptionHandler */
export interface ApiErrorResponse {
  status: number;
  message: string;
  /** Present for IllegalArgumentException */
  error?: string;
  /** Present for validation errors: field name -> message */
  errors?: Record<string, string>;
}

/** GET /api/csrf — Spring Security CsrfToken */
export interface CsrfTokenResponse {
  token: string;
  headerName: string;
  parameterName: string;
}
