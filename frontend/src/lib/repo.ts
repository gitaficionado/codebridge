/** Helpers for turning user input like "owner/repo" into what the backend expects. */

export interface RepoSlug {
  owner: string;
  repo: string;
}

// Same character rules as the backend's @Pattern on MavenComparisonRequest.
const SEGMENT = "[A-Za-z0-9_.-]+";
const SLUG_RE = new RegExp(`^(${SEGMENT})/(${SEGMENT})$`);

/**
 * Accepts "owner/repo", "github.com/owner/repo" or "https://github.com/owner/repo(.git)(/)".
 * Returns null when the value is not a valid repository reference.
 */
export function parseRepoSlug(input: string): RepoSlug | null {
  let value = input.trim();
  value = value.replace(/^https?:\/\//i, "");
  value = value.replace(/^(www\.)?github\.com\//i, "");
  value = value.replace(/\/+$/, "");
  value = value.replace(/\.git$/i, "");

  const match = SLUG_RE.exec(value);
  if (!match) return null;

  const [, owner, repo] = match;
  if (owner === "." || owner === ".." || repo === "." || repo === "..") return null;
  return { owner, repo };
}

export function toSlugString({ owner, repo }: RepoSlug): string {
  return `${owner}/${repo}`;
}

export function toGitHubUrl({ owner, repo }: RepoSlug): string {
  return `https://github.com/${owner}/${repo}`;
}

export function validateRepoInput(input: string): string | null {
  if (!input.trim()) return "Enter a repository as owner/repo";
  if (!parseRepoSlug(input)) return "Use the format owner/repo, e.g. spring-projects/spring-petclinic";
  return null;
}
