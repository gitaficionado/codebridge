import { useMemo, useState } from "react";
import Box from "@mui/material/Box";
import Card from "@mui/material/Card";
import CardContent from "@mui/material/CardContent";
import Chip from "@mui/material/Chip";
import InputAdornment from "@mui/material/InputAdornment";
import Paper from "@mui/material/Paper";
import Stack from "@mui/material/Stack";
import Table from "@mui/material/Table";
import TableBody from "@mui/material/TableBody";
import TableCell from "@mui/material/TableCell";
import TableContainer from "@mui/material/TableContainer";
import TableHead from "@mui/material/TableHead";
import TableRow from "@mui/material/TableRow";
import TextField from "@mui/material/TextField";
import ToggleButton from "@mui/material/ToggleButton";
import ToggleButtonGroup from "@mui/material/ToggleButtonGroup";
import Typography from "@mui/material/Typography";
import ArrowForwardIcon from "@mui/icons-material/ArrowForward";
import CheckCircleIcon from "@mui/icons-material/CheckCircle";
import SearchIcon from "@mui/icons-material/Search";
import { SEVERITY_STYLES } from "@/components/compare/severity";
import { CONFLICT_SEVERITIES, type ConflictSeverity, type MavenDependencyConflict } from "@/types/api";
import { MONO_FONT } from "@/theme";

interface ConflictTableProps {
  conflicts: MavenDependencyConflict[];
  existingLabel: string;
  incomingLabel: string;
  severity: ConflictSeverity | null;
  onSeverityChange: (severity: ConflictSeverity | null) => void;
}

const SEVERITY_RANK: Record<ConflictSeverity, number> = { HIGH: 0, MEDIUM: 1, LOW: 2, UNKNOWN: 3 };
const ALL = "ALL";

export function ConflictTable({
  conflicts,
  existingLabel,
  incomingLabel,
  severity,
  onSeverityChange,
}: ConflictTableProps) {
  const [query, setQuery] = useState("");

  const rows = useMemo(() => {
    const q = query.trim().toLowerCase();
    return conflicts
      .filter((c) => !severity || c.severity === severity)
      .filter((c) => !q || `${c.groupId}:${c.artifactId}`.toLowerCase().includes(q))
      .sort(
        (a, b) =>
          SEVERITY_RANK[a.severity] - SEVERITY_RANK[b.severity] ||
          a.groupId.localeCompare(b.groupId) ||
          a.artifactId.localeCompare(b.artifactId),
      );
  }, [conflicts, query, severity]);

  if (conflicts.length === 0) {
    return (
      <Card>
        <CardContent sx={{ py: 5, textAlign: "center" }}>
          <CheckCircleIcon color="success" sx={{ fontSize: 48 }} />
          <Typography variant="h6" sx={{ mt: 1 }}>
            No version conflicts found
          </Typography>
          <Typography variant="body2" color="text.secondary">
            The two repositories don’t share any Maven dependencies with differing versions.
          </Typography>
        </CardContent>
      </Card>
    );
  }

  return (
    <Card>
      <CardContent sx={{ p: { xs: 2, sm: 3 } }}>
        <Typography variant="h6" component="h3">
          Dependency conflicts
        </Typography>
        <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
          Showing {rows.length} of {conflicts.length}. Severity describes how far apart the versions
          are — not a confirmed incompatibility.
        </Typography>

        <Stack
          direction={{ xs: "column", md: "row" }}
          spacing={2}
          sx={{ mb: 2, justifyContent: "space-between", alignItems: { md: "center" } }}
        >
          <ToggleButtonGroup
            size="small"
            exclusive
            value={severity ?? ALL}
            onChange={(_, value: string | null) =>
              onSeverityChange(value === null || value === ALL ? null : (value as ConflictSeverity))
            }
            aria-label="Filter by severity"
            sx={{ flexWrap: "wrap" }}
          >
            <ToggleButton value={ALL}>All</ToggleButton>
            {CONFLICT_SEVERITIES.map((s) => (
              <ToggleButton key={s} value={s} sx={{ gap: 0.75 }}>
                <Box component="span" sx={{ width: 8, height: 8, borderRadius: "50%", bgcolor: SEVERITY_STYLES[s].fill }} />
                {SEVERITY_STYLES[s].label}
              </ToggleButton>
            ))}
          </ToggleButtonGroup>

          <TextField
            size="small"
            value={query}
            onChange={(e) => setQuery(e.target.value)}
            placeholder="Filter by groupId or artifactId"
            sx={{ width: { xs: "100%", md: 300 } }}
            slotProps={{
              input: {
                startAdornment: (
                  <InputAdornment position="start">
                    <SearchIcon fontSize="small" />
                  </InputAdornment>
                ),
              },
              htmlInput: { "aria-label": "Filter dependencies" },
            }}
          />
        </Stack>

        {/* Tablet / desktop: table */}
        <TableContainer
          sx={{ display: { xs: "none", sm: "block" }, border: 1, borderColor: "divider", borderRadius: 3 }}
        >
          <Table size="small">
            <TableHead>
              <TableRow>
                <TableCell>Dependency</TableCell>
                <TableCell>
                  <SideLabel color="primary.main" text={existingLabel} />
                </TableCell>
                <TableCell padding="none" aria-hidden="true" />
                <TableCell>
                  <SideLabel color="secondary.main" text={incomingLabel} />
                </TableCell>
                <TableCell align="right">Severity</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {rows.map((c) => (
                <TableRow key={`${c.groupId}:${c.artifactId}`} hover>
                  <TableCell sx={{ maxWidth: 360 }}>
                    <Typography variant="body2" sx={{ fontWeight: 500, wordBreak: "break-all" }}>
                      {c.artifactId}
                    </Typography>
                    <Typography
                      variant="caption"
                      color="text.secondary"
                      sx={{ fontFamily: MONO_FONT, wordBreak: "break-all" }}
                    >
                      {c.groupId}
                    </Typography>
                  </TableCell>
                  <TableCell sx={{ fontFamily: MONO_FONT, fontSize: 13 }}>{c.existingVersion}</TableCell>
                  <TableCell padding="none">
                    <ArrowForwardIcon sx={{ fontSize: 16, color: "text.disabled", display: "block" }} />
                  </TableCell>
                  <TableCell sx={{ fontFamily: MONO_FONT, fontSize: 13 }}>{c.incomingVersion}</TableCell>
                  <TableCell align="right">
                    <SeverityChip severity={c.severity} />
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </TableContainer>

        {/* Mobile: stacked cards */}
        <Stack spacing={1.5} sx={{ display: { xs: "flex", sm: "none" } }}>
          {rows.map((c) => (
            <Paper key={`${c.groupId}:${c.artifactId}`} variant="outlined" sx={{ p: 1.5, borderRadius: 3 }}>
              <Stack direction="row" spacing={1} sx={{ justifyContent: "space-between", alignItems: "flex-start" }}>
                <Box sx={{ minWidth: 0 }}>
                  <Typography variant="body2" sx={{ fontWeight: 500, wordBreak: "break-all" }}>
                    {c.artifactId}
                  </Typography>
                  <Typography variant="caption" color="text.secondary" sx={{ fontFamily: MONO_FONT, wordBreak: "break-all" }}>
                    {c.groupId}
                  </Typography>
                </Box>
                <SeverityChip severity={c.severity} />
              </Stack>
              <Stack direction="row" spacing={1} sx={{ mt: 1, alignItems: "center" }}>
                <Chip size="small" variant="outlined" color="primary" label={c.existingVersion} sx={{ fontFamily: MONO_FONT }} />
                <ArrowForwardIcon sx={{ fontSize: 14, color: "text.disabled" }} />
                <Chip size="small" variant="outlined" color="secondary" label={c.incomingVersion} sx={{ fontFamily: MONO_FONT }} />
              </Stack>
            </Paper>
          ))}
        </Stack>

        {rows.length === 0 && (
          <Typography variant="body2" color="text.secondary" sx={{ py: 4, textAlign: "center" }}>
            No conflicts match these filters.
          </Typography>
        )}
      </CardContent>
    </Card>
  );
}

function SideLabel({ color, text }: { color: string; text: string }) {
  return (
    <Box component="span" sx={{ display: "inline-flex", alignItems: "center", gap: 0.75, textTransform: "none" }}>
      <Box component="span" sx={{ width: 8, height: 8, borderRadius: "50%", bgcolor: color }} />
      {text}
    </Box>
  );
}

function SeverityChip({ severity }: { severity: ConflictSeverity }) {
  const style = SEVERITY_STYLES[severity];
  return <Chip size="small" color={style.color} variant="outlined" label={style.label} />;
}
