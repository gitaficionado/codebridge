import axios, { AxiosError, type InternalAxiosRequestConfig } from "axios";
import type { ApiErrorResponse } from "@/types/api";

/**
 * Shared Axios instance. All calls use relative "/api/..." paths; in development
 * the Vite dev server proxies them to Spring Boot on :8080 and adds HTTP Basic auth.
 *
 * CSRF: Spring Security uses CookieCsrfTokenRepository, which stores the token in an
 * XSRF-TOKEN cookie. Axios copies that cookie into the X-XSRF-TOKEN header on every
 * request, so we only need to make sure the cookie exists before the first POST.
 */
export const api = axios.create({
  baseURL: "/api",
  timeout: 45_000, // GitHub calls can be slow
  withCredentials: true,
  withXSRFToken: true,
  xsrfCookieName: "XSRF-TOKEN",
  xsrfHeaderName: "X-XSRF-TOKEN",
  headers: {
    Accept: "application/json",
    // Stops Spring Security from sending "WWW-Authenticate" (and the browser login popup) on 401.
    "X-Requested-With": "XMLHttpRequest",
  },
});

const SAFE_METHODS = new Set(["get", "head", "options", "trace"]);

let csrfRequest: Promise<void> | null = null;

/** Calls GET /api/csrf once so the backend sets the XSRF-TOKEN cookie. */
export function ensureCsrfToken(): Promise<void> {
  if (!csrfRequest) {
    csrfRequest = api
      .get("/csrf")
      .then(() => undefined)
      .catch((error: unknown) => {
        csrfRequest = null; // allow a retry next time
        throw error;
      });
  }
  return csrfRequest;
}

function hasXsrfCookie(): boolean {
  return document.cookie.split("; ").some((c) => c.startsWith("XSRF-TOKEN="));
}

api.interceptors.request.use(async (config) => {
  const method = (config.method ?? "get").toLowerCase();
  if (!SAFE_METHODS.has(method) && !hasXsrfCookie()) {
    await ensureCsrfToken();
  }
  return config;
});

type RetriableConfig = InternalAxiosRequestConfig & { _csrfRetried?: boolean };

// If the token was rotated/expired the backend answers 403: refresh it and retry once.
api.interceptors.response.use(undefined, async (error: unknown) => {
  if (axios.isAxiosError(error) && error.response?.status === 403 && error.config) {
    const config = error.config as RetriableConfig;
    const method = (config.method ?? "get").toLowerCase();
    if (!config._csrfRetried && !SAFE_METHODS.has(method)) {
      config._csrfRetried = true;
      csrfRequest = null;
      await ensureCsrfToken();
      return api.request(config);
    }
  }
  return Promise.reject(error);
});

/** Turns any thrown value into a message suitable for showing in the UI. */
export function getErrorMessage(error: unknown): string {
  if (axios.isAxiosError(error)) {
    const axiosError = error as AxiosError<ApiErrorResponse | string>;

    if (axiosError.code === "ECONNABORTED") {
      return "The request timed out. GitHub may be slow — please try again.";
    }
    if (!axiosError.response) {
      return "Cannot reach the backend. Is Spring Boot running on http://localhost:8080?";
    }

    const { status, data } = axiosError.response;

    if (data && typeof data === "object") {
      if (data.errors && Object.keys(data.errors).length > 0) {
        return Object.values(data.errors).join(" · ");
      }
      if (data.error && data.message) return `${data.message}: ${data.error}`;
      if (data.message) return data.message;
    }

    switch (status) {
      case 401:
        return "The backend rejected the login. Check BACKEND_USERNAME / BACKEND_PASSWORD in frontend/.env.";
      case 403:
        return "Request blocked by the backend (CSRF or permissions).";
      case 404:
        return "Endpoint not found on the backend.";
      case 500:
      case 502:
      case 503:
      case 504:
        return `The backend returned an error (${status}).`;
      default:
        return `Request failed with status ${status}.`;
    }
  }

  if (error instanceof Error) return error.message;
  return "Something went wrong.";
}
