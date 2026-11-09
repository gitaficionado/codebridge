import { useState, type FormEvent } from "react";
import Box from "@mui/material/Box";
import Button from "@mui/material/Button";
import Card from "@mui/material/Card";
import CardContent from "@mui/material/CardContent";
import Chip from "@mui/material/Chip";
import CircularProgress from "@mui/material/CircularProgress";
import IconButton from "@mui/material/IconButton";
import InputAdornment from "@mui/material/InputAdornment";
import Stack from "@mui/material/Stack";
import TextField from "@mui/material/TextField";
import Tooltip from "@mui/material/Tooltip";
import Typography from "@mui/material/Typography";
import CloseIcon from "@mui/icons-material/Close";
import GitHubIcon from "@mui/icons-material/GitHub";
import SearchIcon from "@mui/icons-material/Search";
import SwapHorizIcon from "@mui/icons-material/SwapHoriz";
import { parseRepoSlug, validateRepoInput, type RepoSlug } from "@/lib/repo";
import { MONO_FONT } from "@/theme";

interface CompareFormProps {
  loading: boolean;
  onCompare: (existing: RepoSlug, incoming: RepoSlug) => void;
  onCancel: () => void;
}

/** Repos that have a pom.xml at the root, so the backend can analyse them. */
const EXAMPLES: ReadonlyArray<{ existing: string; incoming: string }> = [
  { existing: "spring-projects/spring-petclinic", incoming: "spring-petclinic/spring-petclinic-rest" },
  { existing: "apache/commons-lang", incoming: "apache/commons-text" },
];

type Field = "existing" | "incoming";

export function CompareForm({ loading, onCompare, onCancel }: CompareFormProps) {
  const [values, setValues] = useState<Record<Field, string>>({ existing: "", incoming: "" });
  const [touched, setTouched] = useState<Record<Field, boolean>>({ existing: false, incoming: false });
  const [submitted, setSubmitted] = useState(false);

  const errors: Record<Field, string | null> = {
    existing: validateRepoInput(values.existing),
    incoming: validateRepoInput(values.incoming),
  };
  const sameRepo =
    !errors.existing &&
    !errors.incoming &&
    values.existing.trim().toLowerCase() === values.incoming.trim().toLowerCase();

  const visibleError = (field: Field): string | null =>
    touched[field] || submitted ? errors[field] : null;

  const handleSubmit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setSubmitted(true);
    const existing = parseRepoSlug(values.existing);
    const incoming = parseRepoSlug(values.incoming);
    if (!existing || !incoming || sameRepo) return;
    onCompare(existing, incoming);
  };

  const swap = () => setValues((v) => ({ existing: v.incoming, incoming: v.existing }));

  const fillExample = (example: (typeof EXAMPLES)[number]) => {
    setValues({ existing: example.existing, incoming: example.incoming });
    setSubmitted(false);
    setTouched({ existing: false, incoming: false });
  };

  const renderField = (field: Field, label: string, color: "primary" | "secondary") => {
    const error = visibleError(field);
    return (
      <TextField
        id={`repo-${field}`}
        label={label}
        placeholder="owner/repo"
        value={values[field]}
        onChange={(e) => setValues((v) => ({ ...v, [field]: e.target.value }))}
        onBlur={() => setTouched((t) => ({ ...t, [field]: true }))}
        disabled={loading}
        error={Boolean(error)}
        helperText={error ?? " "}
        color={color}
        fullWidth
        slotProps={{
          input: {
            startAdornment: (
              <InputAdornment position="start">
                <GitHubIcon fontSize="small" color={error ? "error" : color} />
              </InputAdornment>
            ),
            sx: { fontFamily: MONO_FONT },
          },
          htmlInput: { autoComplete: "off", spellCheck: false },
        }}
      />
    );
  };

  return (
    <Card>
      <CardContent sx={{ p: { xs: 2.5, sm: 3 } }}>
        <Typography variant="h5" component="h2" gutterBottom>
          Compare two repositories
        </Typography>
        <Typography variant="body2" color="text.secondary" sx={{ mb: 3 }}>
          Enter two public GitHub repositories with a root{" "}
          <Box component="code" sx={{ fontFamily: MONO_FONT }}>pom.xml</Box>. CodeBridge compares their
          Maven dependencies side by side.
        </Typography>

        <Box component="form" onSubmit={handleSubmit} noValidate>
          <Stack
            direction={{ xs: "column", md: "row" }}
            spacing={{ xs: 0, md: 2 }}
            sx={{ alignItems: { md: "flex-start" } }}
          >
            {renderField("existing", "Repository A · existing", "primary")}

            <Box sx={{ display: "flex", justifyContent: "center", pt: { md: 1 }, pb: { xs: 2, md: 0 } }}>
              <Tooltip title="Swap repositories">
                <span>
                  <IconButton
                    onClick={swap}
                    disabled={loading}
                    aria-label="Swap repositories"
                    sx={{
                      border: 1,
                      borderColor: "divider",
                      transform: { xs: "rotate(90deg)", md: "none" },
                    }}
                  >
                    <SwapHorizIcon />
                  </IconButton>
                </span>
              </Tooltip>
            </Box>

            {renderField("incoming", "Repository B · incoming", "secondary")}
          </Stack>

          {submitted && sameRepo && (
            <Typography variant="caption" color="error" component="p" sx={{ mb: 1 }}>
              Pick two different repositories to compare.
            </Typography>
          )}

          <Stack
            direction={{ xs: "column-reverse", sm: "row" }}
            spacing={2}
            sx={{ mt: 1, justifyContent: "space-between", alignItems: { sm: "center" } }}
          >
            <Stack direction="row" spacing={1} useFlexGap sx={{ flexWrap: "wrap", alignItems: "center" }}>
              <Typography variant="caption" color="text.secondary">
                Try:
              </Typography>
              {EXAMPLES.map((example) => (
                <Chip
                  key={example.existing}
                  size="small"
                  variant="outlined"
                  disabled={loading}
                  onClick={() => fillExample(example)}
                  label={`${example.existing.split("/")[1]} ↔ ${example.incoming.split("/")[1]}`}
                  sx={{ fontFamily: MONO_FONT }}
                />
              ))}
            </Stack>

            <Stack direction="row" spacing={1} sx={{ flexShrink: 0 }}>
              {loading && (
                <Button variant="text" onClick={onCancel} startIcon={<CloseIcon />}>
                  Cancel
                </Button>
              )}
              <Button
                type="submit"
                variant="contained"
                size="large"
                disabled={loading}
                startIcon={loading ? <CircularProgress size={18} color="inherit" /> : <SearchIcon />}
                sx={{ minWidth: 150, flexGrow: { xs: 1, sm: 0 } }}
              >
                {loading ? "Comparing…" : "Compare"}
              </Button>
            </Stack>
          </Stack>
        </Box>
      </CardContent>
    </Card>
  );
}
