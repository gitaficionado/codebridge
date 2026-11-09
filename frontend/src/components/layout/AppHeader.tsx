import AppBar from "@mui/material/AppBar";
import Box from "@mui/material/Box";
import Chip from "@mui/material/Chip";
import IconButton from "@mui/material/IconButton";
import Stack from "@mui/material/Stack";
import Toolbar from "@mui/material/Toolbar";
import Tooltip from "@mui/material/Tooltip";
import Typography from "@mui/material/Typography";
import { useColorScheme } from "@mui/material/styles";
import DarkModeIcon from "@mui/icons-material/DarkMode";
import LightModeIcon from "@mui/icons-material/LightMode";
import RefreshIcon from "@mui/icons-material/Refresh";
import { useBackendHealth, type HealthStatus } from "@/hooks/useBackendHealth";
import { USE_MOCK } from "@/api/codebridge";
import logoMark from "@/assets/codebridge-mark.png";

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
        <Box
          sx={{
            bgcolor: "#fff",
            borderRadius: 2,
            height: 40,
            px: 0.75,
            display: "flex",
            alignItems: "center",
            flexShrink: 0,
            boxShadow: 1,
          }}
        >
          <Box component="img" src={logoMark} alt="CodeBridge logo" sx={{ height: 26, display: "block" }} />
        </Box>
        <Box sx={{ minWidth: 0 }}>
          <Typography variant="h6" component="h1" noWrap sx={{ lineHeight: 1.2, fontWeight: 700 }}>
            Code
            <Box component="span" sx={{ color: "#81d4fa" }}>
              Bridge
            </Box>
          </Typography>
          <Typography
            variant="caption"
            noWrap
            sx={{ opacity: 0.85, display: { xs: "none", sm: "block" } }}
          >
            Bridge the Data, Compare the Code
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
