import type { ReactNode } from "react";
import Alert from "@mui/material/Alert";
import Box from "@mui/material/Box";
import Card from "@mui/material/Card";
import CardContent from "@mui/material/CardContent";
import Chip from "@mui/material/Chip";
import Link from "@mui/material/Link";
import Stack from "@mui/material/Stack";
import Typography from "@mui/material/Typography";
import CallSplitIcon from "@mui/icons-material/CallSplit";
import CodeIcon from "@mui/icons-material/Code";
import Inventory2OutlinedIcon from "@mui/icons-material/Inventory2Outlined";
import OpenInNewIcon from "@mui/icons-material/OpenInNew";
import { toGitHubUrl, toSlugString } from "@/lib/repo";
import type { RepoSide } from "@/hooks/useRepoComparison";
import { MONO_FONT } from "@/theme";

interface RepoCardProps {
  side: RepoSide;
  role: "A" | "B";
  label: string;
  dependencyCount: number;
}

export function RepoCard({ side, role, label, dependencyCount }: RepoCardProps) {
  const { slug, metadata, metadataError } = side;
  const color = role === "A" ? "primary" : "secondary";
  const name = metadata?.fullName ?? toSlugString(slug);
  const url = metadata?.htmlUrl ?? toGitHubUrl(slug);

  return (
    <Card sx={{ position: "relative", height: "100%" }}>
      <Box sx={{ position: "absolute", inset: "0 0 auto 0", height: 4, bgcolor: `${color}.main` }} />
      <CardContent sx={{ p: 3 }}>
        <Stack direction="row" sx={{ alignItems: "center", justifyContent: "space-between", mb: 1.5 }}>
          <Chip label={`${role} · ${label}`} color={color} size="small" />
          <Link
            href={url}
            target="_blank"
            rel="noreferrer"
            underline="hover"
            variant="body2"
            color="text.secondary"
            sx={{ display: "inline-flex", alignItems: "center", gap: 0.5 }}
          >
            GitHub <OpenInNewIcon sx={{ fontSize: 14 }} />
          </Link>
        </Stack>

        <Typography variant="h6" component="h3" sx={{ fontFamily: MONO_FONT, wordBreak: "break-all" }}>
          {name}
        </Typography>
        <Typography
          variant="body2"
          color="text.secondary"
          sx={{
            mt: 0.5,
            minHeight: 40,
            display: "-webkit-box",
            WebkitLineClamp: 2,
            WebkitBoxOrient: "vertical",
            overflow: "hidden",
          }}
        >
          {metadata?.description ||
            (metadataError ? "Repository details unavailable." : "No description provided.")}
        </Typography>

        <Box component="dl" sx={{ display: "grid", gridTemplateColumns: "repeat(3, 1fr)", gap: 1.5, mt: 2, mb: 0 }}>
          <Stat icon={<Inventory2OutlinedIcon />} label="Dependencies" value={String(dependencyCount)} emphasis />
          <Stat icon={<CodeIcon />} label="Language" value={metadata?.language ?? "—"} />
          <Stat icon={<CallSplitIcon />} label="Branch" value={metadata?.defaultBranch ?? "—"} mono />
        </Box>

        {metadataError && (
          <Alert severity="warning" variant="outlined" sx={{ mt: 2, py: 0 }}>
            Couldn’t load repository details: {metadataError}
          </Alert>
        )}
        {metadata?.isPrivate && <Chip label="Private repository" color="error" size="small" sx={{ mt: 2 }} />}
      </CardContent>
    </Card>
  );
}

interface StatProps {
  icon: ReactNode;
  label: string;
  value: string;
  emphasis?: boolean;
  mono?: boolean;
}

function Stat({ icon, label, value, emphasis, mono }: StatProps) {
  return (
    <Box sx={{ bgcolor: "action.hover", borderRadius: 2, px: 1.5, py: 1.25, minWidth: 0 }}>
      <Typography
        component="dt"
        variant="caption"
        color="text.secondary"
        sx={{ display: "flex", alignItems: "center", gap: 0.5, "& svg": { fontSize: 14 } }}
      >
        {icon}
        {label}
      </Typography>
      <Typography
        component="dd"
        title={value}
        noWrap
        sx={{
          m: 0,
          mt: 0.5,
          fontWeight: emphasis ? 600 : 500,
          fontSize: emphasis ? 26 : 14,
          lineHeight: emphasis ? 1.2 : 1.6,
          fontFamily: mono ? MONO_FONT : undefined,
        }}
      >
        {value}
      </Typography>
    </Box>
  );
}
