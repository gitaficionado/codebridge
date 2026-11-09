import { useState } from "react";
import Avatar from "@mui/material/Avatar";
import Box from "@mui/material/Box";
import Fade from "@mui/material/Fade";
import Stack from "@mui/material/Stack";
import Typography from "@mui/material/Typography";
import InfoOutlinedIcon from "@mui/icons-material/InfoOutlined";
import { RepoCard } from "@/components/compare/RepoCard";
import { SummaryStats } from "@/components/compare/SummaryStats";
import { ConflictTable } from "@/components/compare/ConflictTable";
import { toSlugString } from "@/lib/repo";
import type { ComparisonResult } from "@/hooks/useRepoComparison";
import type { ConflictSeverity } from "@/types/api";

interface ComparisonResultsProps {
  result: ComparisonResult;
}

export function ComparisonResults({ result }: ComparisonResultsProps) {
  const [severity, setSeverity] = useState<ConflictSeverity | null>(null);
  const { maven, existing, incoming } = result;

  const summary = maven.severitySummary;
  const total = summary.total ?? summary.high + summary.medium + summary.low + summary.unknown;

  return (
    <Fade in timeout={300}>
      <Stack spacing={3} component="section" aria-label="Comparison results">
        {/* Side-by-side entities */}
        <Box sx={{ position: "relative", display: "grid", gap: 2, gridTemplateColumns: { xs: "1fr", md: "1fr 1fr" } }}>
          <RepoCard side={existing} role="A" label="Existing" dependencyCount={maven.existingDependencyCount} />
          <Avatar
            aria-hidden="true"
            sx={{
              display: { xs: "none", md: "flex" },
              position: "absolute",
              top: "50%",
              left: "50%",
              transform: "translate(-50%, -50%)",
              zIndex: 1,
              width: 44,
              height: 44,
              fontSize: 13,
              fontWeight: 700,
              bgcolor: "background.paper",
              color: "text.secondary",
              border: 1,
              borderColor: "divider",
              boxShadow: 2,
            }}
          >
            VS
          </Avatar>
          <RepoCard side={incoming} role="B" label="Incoming" dependencyCount={maven.incomingDependencyCount} />
        </Box>

        <SummaryStats summary={summary} total={total} activeSeverity={severity} onSelectSeverity={setSeverity} />

        <ConflictTable
          conflicts={maven.conflicts}
          existingLabel={toSlugString(existing.slug)}
          incomingLabel={toSlugString(incoming.slug)}
          severity={severity}
          onSeverityChange={setSeverity}
        />

        <Typography variant="caption" color="text.secondary" sx={{ display: "flex", alignItems: "center", gap: 0.75 }}>
          <InfoOutlinedIcon sx={{ fontSize: 16 }} />
          Compared {result.completedAt.toLocaleTimeString()} · only dependencies declared in each
          repository’s root pom.xml are included.
        </Typography>
      </Stack>
    </Fade>
  );
}
