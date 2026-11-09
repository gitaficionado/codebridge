import AppBar from "@mui/material/AppBar";
import Avatar from "@mui/material/Avatar";
import Box from "@mui/material/Box";
import Chip from "@mui/material/Chip";
import IconButton from "@mui/material/IconButton";
import Stack from "@mui/material/Stack";
import Toolbar from "@mui/material/Toolbar";
import Tooltip from "@mui/material/Tooltip";
import Typography from "@mui/material/Typography";
import { useColorScheme } from "@mui/material/styles";
import CompareArrowsIcon from "@mui/icons-material/CompareArrows";
import DarkModeIcon from "@mui/icons-material/DarkMode";
import LightModeIcon from "@mui/icons-material/LightMode";
import RefreshIcon from "@mui/icons-material/Refresh";
import { useBackendHealth, type HealthStatus } from "@/hooks/useBackendHealth";
import { USE_MOCK } from "@/api/codebridge";

const HEALTH_LABEL: Record<HealthStatus, string> = {
  checking: "Checking backend…",
  online: "Backend online",
  offline: "Backend offline",
};

const HEALTH_COLOR: Record<HealthStatus, string> = {
  checking: "warning.light",
  online: "success.light",
  offline: "error.light",
};

export function AppHeader() {
  const { status, recheck } = useBackendHealth();
  const { mode, systemMode, setMode } = useColorScheme();
  const resolvedMode = mode === "system" ? systemMode : mode;
  const isDark = resolvedMode === "dark";

  return (
    <AppBar position="sticky" color="primary" enableColorOnDark elevation={2}>
      <Toolbar sx={{ gap: 1.5 }}>
        <Avatar sx={{ bgcolor: "rgba(255,255,255,0.18)", color: "inherit", width: 40, height: 40 }}>
          <CompareArrowsIcon />
        </Avatar>
        <Box sx={{ minWidth: 0 }}>
          <Typography variant="h6" component="h1" noWrap sx={{ lineHeight: 1.2 }}>
            CodeBridge
          </Typography>
          <Typography
            variant="caption"
            noWrap
            sx={{ opacity: 0.8, display: { xs: "none", sm: "block" } }}
          >
            Repository dependency comparison
          </Typography>
        </Box>

        <Stack direction="row" spacing={1} sx={{ ml: "auto", alignItems: "center" }}>
          {USE_MOCK && <Chip label="Mock data" color="warning" size="small" />}

          <Tooltip title="Re-check GET /api/health">
            <Chip
              size="small"
              variant="outlined"
              onClick={() => void recheck()}
              aria-label={HEALTH_LABEL[status]}
              icon={
                <Box
                  component="span"
                  sx={{ width: 8, height: 8, borderRadius: "50%", bgcolor: HEALTH_COLOR[status], ml: "8px !important" }}
                />
              }
              label={
                <Stack direction="row" spacing={0.5} sx={{ alignItems: "center" }}>
                  <Box component="span" sx={{ display: { xs: "none", sm: "inline" } }}>
                    {HEALTH_LABEL[status]}
                  </Box>
                  <RefreshIcon sx={{ fontSize: 14, opacity: 0.8 }} />
                </Stack>
              }
              sx={{ color: "inherit", borderColor: "rgba(255,255,255,0.5)" }}
            />
          </Tooltip>

          <Tooltip title={isDark ? "Switch to light mode" : "Switch to dark mode"}>
            <IconButton
              color="inherit"
              onClick={() => setMode(isDark ? "light" : "dark")}
              aria-label={isDark ? "Switch to light mode" : "Switch to dark mode"}
            >
              {isDark ? <LightModeIcon /> : <DarkModeIcon />}
            </IconButton>
          </Tooltip>
        </Stack>
      </Toolbar>
    </AppBar>
  );
}
