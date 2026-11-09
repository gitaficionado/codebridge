import Alert from "@mui/material/Alert";
import AlertTitle from "@mui/material/AlertTitle";
import Box from "@mui/material/Box";
import Button from "@mui/material/Button";
import Container from "@mui/material/Container";
import Stack from "@mui/material/Stack";
import RestartAltIcon from "@mui/icons-material/RestartAlt";
import { AppHeader } from "@/components/layout/AppHeader";
import { CompareForm } from "@/components/compare/CompareForm";
import { ComparisonResults } from "@/components/compare/ComparisonResults";
import { EmptyState } from "@/components/compare/EmptyState";
import { LoadingState } from "@/components/compare/LoadingState";
import { useRepoComparison } from "@/hooks/useRepoComparison";

export default function App() {
  const { status, result, error, compare, cancel, reset } = useRepoComparison();

  return (
    <Box sx={{ minHeight: "100vh", bgcolor: "background.default" }}>
      <AppHeader />

      <Container maxWidth="lg" sx={{ py: { xs: 3, md: 4 } }}>
        <Stack spacing={3}>
          <CompareForm loading={status === "loading"} onCompare={compare} onCancel={cancel} />

          {status === "error" && error && (
            <Alert
              severity="error"
              variant="outlined"
              action={
                <Button color="inherit" size="small" startIcon={<RestartAltIcon />} onClick={reset}>
                  Start over
                </Button>
              }
            >
              <AlertTitle>Comparison failed</AlertTitle>
              {error}
            </Alert>
          )}

          {status === "loading" && <LoadingState />}
          {status === "success" && result && (
            <ComparisonResults key={result.completedAt.getTime()} result={result} />
          )}
          {status === "idle" && <EmptyState />}
        </Stack>
      </Container>
    </Box>
  );
}
