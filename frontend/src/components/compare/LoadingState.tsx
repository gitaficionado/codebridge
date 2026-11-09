import Box from "@mui/material/Box";
import Card from "@mui/material/Card";
import CardContent from "@mui/material/CardContent";
import CircularProgress from "@mui/material/CircularProgress";
import Paper from "@mui/material/Paper";
import Skeleton from "@mui/material/Skeleton";
import Stack from "@mui/material/Stack";
import Typography from "@mui/material/Typography";

/** Spinner banner plus skeletons shaped like the results, shown while the backend works. */
export function LoadingState() {
  return (
    <Stack spacing={3} component="section" aria-busy="true" aria-label="Loading comparison">
      <Paper elevation={1} sx={{ py: 5, px: 3, textAlign: "center" }}>
        <CircularProgress size={52} thickness={4} aria-label="Comparing repositories" />
        <Typography variant="subtitle1" sx={{ mt: 2, fontWeight: 500 }}>
          Analyzing repositories…
        </Typography>
        <Typography variant="body2" color="text.secondary">
          Fetching both pom.xml files from GitHub and comparing dependency versions.
        </Typography>
      </Paper>

      <Box sx={{ display: "grid", gap: 2, gridTemplateColumns: { xs: "1fr", md: "1fr 1fr" } }}>
        {[0, 1].map((i) => (
          <Card key={i}>
            <CardContent>
              <Skeleton width={110} height={28} />
              <Skeleton width="65%" height={36} />
              <Skeleton width="90%" />
              <Box sx={{ display: "grid", gridTemplateColumns: "repeat(3, 1fr)", gap: 1.5, mt: 2 }}>
                {[0, 1, 2].map((j) => (
                  <Skeleton key={j} variant="rounded" height={64} />
                ))}
              </Box>
            </CardContent>
          </Card>
        ))}
      </Box>

      <Box
        sx={{
          display: "grid",
          gap: 2,
          gridTemplateColumns: { xs: "repeat(2, 1fr)", sm: "repeat(3, 1fr)", md: "repeat(5, 1fr)" },
        }}
      >
        {[0, 1, 2, 3, 4].map((i) => (
          <Skeleton key={i} variant="rounded" height={104} sx={{ borderRadius: 4 }} />
        ))}
      </Box>

      <Card>
        <CardContent>
          {[0, 1, 2, 3, 4].map((i) => (
            <Skeleton key={i} height={44} />
          ))}
        </CardContent>
      </Card>
    </Stack>
  );
}
