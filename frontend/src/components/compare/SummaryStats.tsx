import Box from "@mui/material/Box";
import Card from "@mui/material/Card";
import CardActionArea from "@mui/material/CardActionArea";
import CardContent from "@mui/material/CardContent";
import Stack from "@mui/material/Stack";
import Typography from "@mui/material/Typography";
import MergeTypeIcon from "@mui/icons-material/MergeType";
import { CONFLICT_SEVERITIES, type ConflictSeverity, type MavenSeveritySummary } from "@/types/api";
import { SEVERITY_STYLES } from "@/components/compare/severity";

interface SummaryStatsProps {
  summary: MavenSeveritySummary;
  total: number;
  activeSeverity: ConflictSeverity | null;
  onSelectSeverity: (severity: ConflictSeverity | null) => void;
}

const SUMMARY_KEY: Record<ConflictSeverity, "high" | "medium" | "low" | "unknown"> = {
  HIGH: "high",
  MEDIUM: "medium",
  LOW: "low",
  UNKNOWN: "unknown",
};

/** KPI tiles. Clicking a severity tile filters the conflict table. */
export function SummaryStats({ summary, total, activeSeverity, onSelectSeverity }: SummaryStatsProps) {
  return (
    <Stack spacing={1.5}>
      <Box
        sx={{
          display: "grid",
          gap: 2,
          gridTemplateColumns: { xs: "repeat(2, 1fr)", sm: "repeat(3, 1fr)", md: "repeat(5, 1fr)" },
        }}
      >
        <Card sx={{ gridColumn: { xs: "span 2", sm: "auto" }, bgcolor: "primary.main", color: "primary.contrastText" }}>
          <CardContent>
            <Typography variant="caption" sx={{ display: "flex", alignItems: "center", gap: 0.5, opacity: 0.9 }}>
              <MergeTypeIcon sx={{ fontSize: 16 }} /> Potential conflicts
            </Typography>
            <Typography sx={{ fontSize: 34, fontWeight: 600, lineHeight: 1.2 }}>{total}</Typography>
            <Typography variant="caption" sx={{ opacity: 0.85 }}>
              shared dependencies with different versions
            </Typography>
          </CardContent>
        </Card>

        {CONFLICT_SEVERITIES.map((severity) => {
          const style = SEVERITY_STYLES[severity];
          const count = summary[SUMMARY_KEY[severity]];
          const active = activeSeverity === severity;
          return (
            <Card
              key={severity}
              elevation={active ? 4 : 1}
              sx={{
                border: 2,
                borderColor: active ? style.fill : "transparent",
                transition: "box-shadow 150ms, border-color 150ms",
              }}
            >
              <CardActionArea
                onClick={() => onSelectSeverity(active ? null : severity)}
                aria-pressed={active}
                sx={{ height: "100%" }}
              >
                <CardContent>
                  <Typography
                    variant="caption"
                    color="text.secondary"
                    sx={{ display: "flex", alignItems: "center", gap: 0.75, fontWeight: 500 }}
                  >
                    <Box component="span" sx={{ width: 8, height: 8, borderRadius: "50%", bgcolor: style.fill }} />
                    {style.label}
                  </Typography>
                  <Typography sx={{ fontSize: 34, fontWeight: 600, lineHeight: 1.2 }}>{count}</Typography>
                  <Typography variant="caption" color="text.secondary">
                    {style.description}
                  </Typography>
                </CardContent>
              </CardActionArea>
            </Card>
          );
        })}
      </Box>

      {total > 0 && (
        <Box
          aria-hidden="true"
          sx={{ display: "flex", height: 8, borderRadius: 4, overflow: "hidden", bgcolor: "action.hover" }}
        >
          {CONFLICT_SEVERITIES.map((severity) => {
            const count = summary[SUMMARY_KEY[severity]];
            if (!count) return null;
            return (
              <Box
                key={severity}
                sx={{ bgcolor: SEVERITY_STYLES[severity].fill, width: `${(count / total) * 100}%` }}
              />
            );
          })}
        </Box>
      )}
    </Stack>
  );
}
