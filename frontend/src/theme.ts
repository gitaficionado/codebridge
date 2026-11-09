import { createTheme } from "@mui/material/styles";

/**
 * Material Design theme (MUI). Indigo primary / pink secondary, Roboto,
 * rounded Material 3-style shapes, with light and dark color schemes.
 * Repository A uses `primary`, repository B uses `secondary` throughout the UI.
 */
export const theme = createTheme({
  cssVariables: { colorSchemeSelector: "class" },
  colorSchemes: {
    light: {
      palette: {
        primary: { main: "#3f51b5" },
        secondary: { main: "#d81b60" },
        background: { default: "#f5f6fb", paper: "#ffffff" },
      },
    },
    dark: {
      palette: {
        primary: { main: "#9fa8da" },
        secondary: { main: "#f48fb1" },
        background: { default: "#0f1220", paper: "#171b2e" },
      },
    },
  },
  shape: { borderRadius: 12 },
  typography: {
    fontFamily: '"Roboto", "Segoe UI", system-ui, -apple-system, sans-serif',
    h5: { fontWeight: 600 },
    h6: { fontWeight: 600 },
    button: { textTransform: "none", fontWeight: 600 },
  },
  components: {
    MuiCard: {
      defaultProps: { elevation: 1 },
      styleOverrides: { root: { borderRadius: 16 } },
    },
    MuiPaper: {
      styleOverrides: { rounded: { borderRadius: 16 } },
    },
    MuiButton: {
      defaultProps: { disableElevation: true },
      styleOverrides: { root: { borderRadius: 999, paddingInline: 20 } },
    },
    MuiChip: {
      styleOverrides: { root: { fontWeight: 500 } },
    },
    MuiTableCell: {
      styleOverrides: {
        head: { fontWeight: 600, fontSize: 12, textTransform: "uppercase", letterSpacing: 0.5 },
      },
    },
    MuiToggleButton: {
      styleOverrides: { root: { textTransform: "none", paddingInline: 14 } },
    },
  },
});

/** Monospace stack for repo slugs, versions and coordinates. */
export const MONO_FONT = 'ui-monospace, "Cascadia Code", "JetBrains Mono", Consolas, monospace';
