import { fileURLToPath, URL } from "node:url";
import { defineConfig, loadEnv } from "vite";
import react from "@vitejs/plugin-react";

// https://vite.dev/config/
export default defineConfig(({ mode }) => {
  // Load ALL variables from .env files (not just VITE_*), so the backend
  // credentials stay on the dev server and are never bundled into the browser.
  const env = loadEnv(mode, process.cwd(), "");

  const backendUrl = env.BACKEND_URL || "http://localhost:8080";
  const username = env.BACKEND_USERNAME || "demo";
  const password = env.BACKEND_PASSWORD || "demo123";
  const basicAuth = Buffer.from(`${username}:${password}`).toString("base64");

  return {
    plugins: [react()],
    resolve: {
      alias: {
        "@": fileURLToPath(new URL("./src", import.meta.url)),
      },
    },
    server: {
      port: 5173,
      strictPort: true,
      proxy: {
        // Every request to /api/* is forwarded to Spring Boot.
        // The proxy adds the HTTP Basic header the backend requires.
        "/api": {
          target: backendUrl,
          changeOrigin: true,
          headers: {
            Authorization: `Basic ${basicAuth}`,
          },
        },
      },
    },
  };
});
