import Box from "@mui/material/Box";
import Paper from "@mui/material/Paper";
import Typography from "@mui/material/Typography";
import BoltIcon from "@mui/icons-material/Bolt";
import LayersIcon from "@mui/icons-material/Layers";
import VerifiedUserIcon from "@mui/icons-material/VerifiedUser";
import logoMark from "@/assets/codebridge-mark.png";

const FEATURES = [
  { icon: <LayersIcon />, title: "Side-by-side", text: "See both repositories and their dependency counts together." },
  { icon: <BoltIcon />, title: "Version drift", text: "Spot shared libraries pinned to different versions." },
  { icon: <VerifiedUserIcon />, title: "Severity", text: "Major, minor and patch differences ranked for review." },
];

export function EmptyState() {
  return (
    <Paper
      variant="outlined"
      sx={{ borderStyle: "dashed", py: 6, px: 3, textAlign: "center", bgcolor: "transparent" }}
    >
      <Box component="img" src={logoMark} alt="" sx={{ height: 72, display: "block", mx: "auto" }} />
      <Typography variant="h6" sx={{ mt: 2 }}>
        Pick two repositories to get started
      </Typography>
      <Typography variant="body2" color="text.secondary" sx={{ maxWidth: 520, mx: "auto", mt: 0.5 }}>
        Results appear here: repository details side by side, a severity breakdown and every
        dependency whose version differs.
      </Typography>

      <Box
        component="ul"
        sx={{
          listStyle: "none",
          p: 0,
          m: 0,
          mt: 4,
          mx: "auto",
          maxWidth: 840,
          display: "grid",
          gap: 2,
          gridTemplateColumns: { xs: "1fr", sm: "repeat(3, 1fr)" },
        }}
      >
        {FEATURES.map(({ icon, title, text }) => (
          <Paper component="li" key={title} elevation={1} sx={{ p: 2.5, textAlign: "left" }}>
            <Box sx={{ color: "primary.main", display: "flex" }}>{icon}</Box>
            <Typography variant="subtitle2" sx={{ mt: 1 }}>
              {title}
            </Typography>
            <Typography variant="caption" color="text.secondary">
              {text}
            </Typography>
          </Paper>
        ))}
      </Box>
    </Paper>
  );
}
